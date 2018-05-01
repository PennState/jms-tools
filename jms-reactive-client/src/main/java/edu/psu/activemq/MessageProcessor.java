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
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;

import edu.psu.activemq.exception.UnableToProcessMessageException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class MessageProcessor {

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  boolean process = true;

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  boolean stopped = false;

  String brokerUrl = null;
  String transportName = null;
  String errorTransportName = null;
  TransportType errorTransportType = null;
  String username;
  String password;
  int requestRetryThreshold;

  ActiveMQMessageConsumer consumer = null;
  ActiveMQMessageProducer producer = null;
  MessageProducer errorProducer = null;

  protected abstract void handleMessage(Message message) throws UnableToProcessMessageException;

  public MessageProcessor() {

  }

  public void terminate() {
    process = false;
  }

  void initialize() {
    log.info("Initializing message processor...");

    Thread t = new Thread(new Runnable() {

      public void run() {

        ActiveMQConnection connection = null;

        try {
          connection = (ActiveMQConnection) MessageHandler.buildActivemqConnection(brokerUrl, username, password);

          RedeliveryPolicy rd = new RedeliveryPolicy();
          rd.setMaximumRedeliveries(2);
          connection.getRedeliveryPolicyMap()
                    .put(new ActiveMQQueue(transportName), rd);

          connection.start();
          Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

          Queue destination = session.createQueue(transportName);
          consumer = (ActiveMQMessageConsumer) session.createConsumer(destination);
          // Used for re-queuing messages at the users requests
          producer = (ActiveMQMessageProducer) session.createProducer(destination);

        } catch (JMSException e) {
          log.info("Error creating message consumer", e);
          stopped = true;
          throw new RuntimeException("Failed to initialize processing queue");
        }

        Connection errorConnection = null;
        if (errorTransportName != null) {
          try {
            errorConnection = MessageHandler.buildActivemqConnection(brokerUrl, username, password);
            errorConnection.start();
            Session errorSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

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
            } catch (UnableToProcessMessageException upme) {
              if (UnableToProcessMessageException.HandleAction.RETRY.equals(upme.getHandleAction())) {
                ActiveMQMessage msg = (ActiveMQMessage) message;
                msg.setReadOnlyProperties(false);
                msg.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, upme.getRetryWait());

                int retryCount = message.getIntProperty("JMSXDeliveryCount");
                if (retryCount >= requestRetryThreshold) {
                  processFailureMessage(message, upme);
                } else {
                  msg.setIntProperty("JMSXDeliveryCount", ++retryCount);
                }

                producer.send(msg);
              } else if (UnableToProcessMessageException.HandleAction.DROP.equals(upme.getHandleAction())) {
                continue;
              } else {
                processFailureMessage(message, upme);
              }
            }
          }
        } catch (Exception e) {
          stopped = true;
          log.error("Processor exception processing message.", e);

          try {
            consumer.rollback();
          } catch (JMSException e1) {
          }
        } finally {
          try {
            consumer.close();
          } catch (JMSException e) {
          }
          try {
            connection.close();
          } catch (JMSException e) {
          }
        }
      }
    });
    t.start();
  }

  private void processFailureMessage(Message message, Exception e) {
    log.warn("Error processing message", e);
    if (errorProducer != null) {
      log.info("Sending to error queue");
      ActiveMQMessage msg = (ActiveMQMessage) message;
      msg.setReadOnlyProperties(false);
      try {
        if (e instanceof UnableToProcessMessageException) {
          msg.setStringProperty("shortDescription", ((UnableToProcessMessageException) e).getShortDescription());
          msg.setStringProperty("sourceSystem", ((UnableToProcessMessageException) e).getSourceSystem());
        } else {
          // Try to grab something for the short Message
          String shortMessage = e.getMessage();
          int shortMessageLength = shortMessage.length() > 256 ? 256 : shortMessage.length();
          msg.setStringProperty("shortDescription", e.getMessage()
                                                     .substring(0, shortMessageLength));
        }

        msg.setStringProperty("error", e.getMessage());
        msg.setStringProperty("errorStackTrace", getStackTrace(e));
        errorProducer.send(msg);
        consumer.acknowledge();
      } catch (JMSException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    } else {
      try {
        consumer.rollback();
      } catch (JMSException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  private String getStackTrace(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  public boolean isStopped() {
    return stopped;
  }
}
