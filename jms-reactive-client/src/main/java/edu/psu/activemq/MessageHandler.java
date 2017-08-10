package edu.psu.activemq;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandler {

  public static final int MIN_THRESHOLD = 3;
  public static final int MIN_RECHECK_PERIOD = 250;

  List<MessageProcessor> handlerList = new ArrayList<>();

  Class<? extends MessageProcessor> clazz;
  String ip;
  String transportName;
  String errorIp;
  String errorTransportName;
  TransportType errorTransportType;
  Constructor<? extends MessageProcessor> constructor;
  Thread monitorThread;
  int messageThreshold = 10;
  int recheckPeriod = 3000;
  int cores;

  public MessageHandler(Class<? extends MessageProcessor> clazz, String ip, String transportName) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    this.clazz = clazz;
    this.ip = ip;
    this.transportName = transportName;

    constructor = clazz.getConstructor(ip.getClass(), transportName.getClass());
    log.trace("Calling start monitor");
    cores = Runtime.getRuntime().availableProcessors();
    startMonitor();
  }

  public MessageHandler(Class<? extends MessageProcessor> clazz, String ip, String transportName, String errorIp, String errorTransportName, TransportType errorTransportType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    this.clazz = clazz;
    this.ip = ip;
    this.transportName = transportName;
    this.errorIp = errorIp;
    this.errorTransportName = errorTransportName;
    this.errorTransportType = errorTransportType;

    constructor = clazz.getConstructor(ip.getClass(), transportName.getClass(), errorIp.getClass(), errorTransportName.getClass(), errorTransportType.getClass());
    log.trace("Calling start monitor");

    startMonitor();
  }

  public void setMessageThreshold(int threshold) throws IllegalStateException {
    if (threshold <= MIN_THRESHOLD) {
      throw new IllegalStateException("Threshold must be greater that 3");
    }

    messageThreshold = threshold;
  }

  public void setRecheckPeriod(int millis) throws IllegalStateException {
    if (millis <= MIN_RECHECK_PERIOD) {
      throw new IllegalStateException("Recheck period must be at least 250 milliseconds");
    }

    recheckPeriod = millis;
  }

  public void terminate() {
    for (MessageProcessor mp : handlerList) {
      mp.terminate();
    }
  }

  private void startMonitor() {
    log.info("Starting the monitor");

    try {
      log.trace("Creating a connection");
      Connection connection = new ActiveMQConnectionFactory("tcp://" + ip).createConnection();
      log.trace("Starting the connection");
      connection.start();
      log.trace("Creating a session");
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      Queue destination = session.createQueue(transportName);

      QueueBrowser browser;
      try {
        browser = session.createBrowser(destination);
      } catch (JMSException e1) {
        throw new RuntimeException("Boom");
      }

      @SuppressWarnings("unchecked")
      int startingMessageCount = Collections.list(browser.getEnumeration())
                                            .size();

      while (true) {
        @SuppressWarnings("unchecked")
        int msgCount = Collections.list(browser.getEnumeration())
                                  .size();
        log.info("Processed: " + (startingMessageCount - msgCount));
        startingMessageCount = msgCount;
        try {
          log.trace("Checking thresholds, count = " + msgCount + " threshold = " + messageThreshold);
          if (msgCount > messageThreshold && handlerList.size() < cores) {
            log.trace("Constructing a new Message Processor");
            MessageProcessor mp = buildNewMessageProcessor();
            handlerList.add(mp);
            log.trace("############# Now " + handlerList.size() + " processors");
          } else {
            if (handlerList.size() > 1) {
              log.trace("Removing a Message Processor");
              MessageProcessor mp = handlerList.remove(handlerList.size() - 1);
              mp.terminate();
              log.trace("############# Now " + handlerList.size() + " processors");
            }
          }

          for (int i = 0; i < handlerList.size(); i++) {
            MessageProcessor mp = handlerList.get(i);
            if (mp.isStopped()) {
              log.trace("Removing stopped processor");
              handlerList.remove(i);
            }
          }

          Thread.sleep(recheckPeriod);
        } catch (InterruptedException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          log.error("Error in message handler", e);
        }
      }
    } catch (JMSException e) {
      log.error("Error connecting to queue", e);
      // try reconnecting after 30 seconds
      try {
        Thread.sleep(30000L);
      } catch (InterruptedException ie) {
        log.warn(ie.toString());
      }
      this.startMonitor();
      
      if (handlerList.isEmpty()) {
        log.trace("Seeding the processing pool with a single instance");
        MessageProcessor mp;
        try {
          mp = buildNewMessageProcessor();
          handlerList.add(mp);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
          log.error(e1.getMessage());
        }
      }
    }
  }

  private MessageProcessor buildNewMessageProcessor() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (errorIp == null) {
      return constructor.newInstance(ip, transportName);
    } else {
      return constructor.newInstance(ip, transportName, errorIp, errorTransportName, errorTransportType);
    }
  }
}
