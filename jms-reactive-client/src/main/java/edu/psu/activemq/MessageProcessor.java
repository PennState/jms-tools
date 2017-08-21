package edu.psu.activemq;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;

import edu.psu.activemq.exception.UnableToProcessMessageException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageProcessor {

  boolean process = true;
  boolean handleErrors = false;
  boolean stopped = false;

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
    log.info("Initializing message processor...");
    
    Thread t = new Thread(new Runnable() {

      public void run() {        
        ActiveMQMessageConsumer consumer = null;
        ActiveMQConnection connection = null;
        
        try {
          connection = (ActiveMQConnection) new ActiveMQConnectionFactory("tcp://" + ip).createConnection();
          
          RedeliveryPolicy rd = new RedeliveryPolicy();          
          rd.setMaximumRedeliveries(2);
          connection.getRedeliveryPolicyMap().put(new ActiveMQQueue(transportName), rd);
          
          connection.start();
          Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

          Queue destination = session.createQueue(transportName);
          consumer = (ActiveMQMessageConsumer) session.createConsumer(destination);
          
        } catch (JMSException e) {
          log.info("Error creating message consumer", e);
          stopped = true;
          throw new RuntimeException("Failed to initialize processing queue");
        }
        
        Connection errorConnection = null;
        MessageProducer errorProducer = null;
        if (handleErrors) {
          try {
            errorConnection = new ActiveMQConnectionFactory("tcp://" + errorIp).createConnection();
            errorConnection.start();
            Session errorSession = connection.createSession(false,  Session.AUTO_ACKNOWLEDGE);
            
            Destination errorDestination = null;
            if (TransportType.TOPIC.equals(errorTransportType)) {
              errorDestination = errorSession.createTopic(errorTransportName);
            } else {
              errorDestination = errorSession.createQueue(errorTransportName);
            }
                        
            errorProducer = errorSession.createProducer(errorDestination);
          } catch (JMSException e) {            
            log.error("Error creating error producer", e);
            stopped = true;
            throw new RuntimeException("Failed to initialize the error endpoint");
          }
        }
        
        try {
          Message message = null;
          while (process) {
            message = consumer.receive();
            try {
              handleMessage(message);
              consumer.acknowledge();
            } catch (Exception e) {
              log.warn("Error processing message", e);                                          
              if (errorProducer != null) {
                log.info("Sending to error queue");                
                ActiveMQMessage msg = (ActiveMQMessage)message;     
                msg.setReadOnlyProperties(false);
                msg.setStringProperty("error", e.getMessage());
                msg.setStringProperty("errorStackTrace", getStackTrace(e));                
                errorProducer.send(msg);
                consumer.acknowledge();
              }
              else{
                consumer.rollback();
              }
            }
          }

        } catch (Exception e) {
          stopped = true;          
          log.error("Processor exception processing message.", e);
          
          try{
            consumer.rollback();          
          }
          catch(JMSException e1) {}          
        }
        finally{
          try {
            consumer.close();
          } catch (JMSException e) {}  
          try {
            connection.close();
          } catch (JMSException e) {}        
        }
      }
    });
    t.start();
  }
  
  private String getStackTrace(Exception e){
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }
  
  public boolean isStopped(){
    return stopped;
  }
}
