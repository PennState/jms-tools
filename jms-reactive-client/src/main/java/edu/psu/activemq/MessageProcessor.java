package edu.psu.activemq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import edu.psu.activemq.exception.UnableToProcessMessageException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageProcessor {

  boolean process = true;
  boolean handleErrors = false;

  protected abstract void handleMessage(Message message) throws UnableToProcessMessageException;

  public MessageProcessor(String ip, String transportName) {
    log.info("In the message producer constructor with ip = " + ip + " and transport name = " + transportName);
    initialize(ip, transportName, null, null, null);
  }

  public MessageProcessor(String ip, String transportName, String errorIp, String errorTransportName, TransportType errorTransportType) {
    log.info("In the message producer constructor with ip = " + ip + " and transport name = " + transportName);
    handleErrors = true;
    initialize(ip, transportName, errorIp, errorTransportName, errorTransportType);
  }

  public void terminate() {
    process = false;
  }

  private void initialize(String ip, String transportName, String errorIp, String errorTransportName, TransportType errorTransportType) {

    Thread t = new Thread(new Runnable() {

      public void run() {
        
        MessageConsumer consumer = null;
        Connection connection = null;
        
        try {
          connection = new ActiveMQConnectionFactory("tcp://" + ip).createConnection();
          connection.start();
          Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

          Queue destination = session.createQueue(transportName);
          consumer = session.createConsumer(destination);
        } catch (JMSException e) {
          // TODO - revisit this
          throw new RuntimeException("Failed to initialize processing queue");
        }
        
        Connection errorConnection = null;
        MessageProducer errorProducer = null;
        if (handleErrors) {
          try {
            errorConnection = new ActiveMQConnectionFactory("tcp://" + ip).createConnection();
            errorConnection.start();
            Session errorSession = connection.createSession(false,  Session.AUTO_ACKNOWLEDGE);
            
            Destination errorDestination = null;
            if (TransportType.TOPIC.equals(errorTransportType)) {
              errorDestination = errorSession.createTopic(transportName);
            } else {
              errorDestination = errorSession.createQueue(transportName);
            }
            
            
            errorProducer = errorSession.createProducer(errorDestination);
          } catch (JMSException e) {
            // TODO - Revisit this
            throw new RuntimeException("Failed to initialize the error endpoint");
          }
        }
        
        try {

          Message message = null;
          while (process) {
            message = consumer.receive();
            try {
              handleMessage(message);
            } catch (UnableToProcessMessageException e) {
              if (errorProducer != null) {
                //TODO - How do we format the message?
              }
            }
          }

          connection.close();
          consumer.close();
        } catch (JMSException e) {

          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
    t.start();
  }
}
