package edu.psu.activemq.services;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class MessageProcessor {

  boolean process = true;

  abstract void handleMessage(Message message);

  public MessageProcessor(String ip, String transportName) {
    log.info("In the message producer constructor with ip = " + ip + " and transport name = " + transportName);
    initialize(ip, transportName);
  }

  public void terminate() {
    process = false;
  }

  private void initialize(String ip, String transportName) {

    Thread t = new Thread(new Runnable() {

      public void run() {
        try {
          // log.info("Creating a connection at tcp://" + ip);
          Connection connection = new ActiveMQConnectionFactory("tcp://" + ip).createConnection();
          // log.info("Starting the connection");
          connection.start();
          // log.info("Creating a session");
          Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          // log.info("Creating a queue: " + transportName);

          Queue destination = session.createQueue(transportName);

          MessageConsumer consumer = session.createConsumer(destination);
          //log.info("Starting the message processing loop");
          while (process) {
            // log.info("Calling consumer.receive");
            Message message = consumer.receive();
            // log.info("Calling handleMessage with " + message);
            handleMessage(message);
          }
        } catch (JMSException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
    t.start();
  }
}
