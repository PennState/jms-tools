package edu.psu.activemq;

/*
 * Copyright (c) 2018 by The Pennsylvania State University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import edu.psu.activemq.exception.MessageHandlerException;
import edu.psu.activemq.util.PropertyUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class MessageHandler {

  public static final int MIN_THRESHOLD = 3;
  public static final int MIN_RECHECK_PERIOD = 250;

  public static final String BROKER_URL_PROP_NAME = "broker.url";
  public static final String TRANSPORT_NAME_PROP_NAME = "queue.name";
  public static final String BROKER_USERNAME_PROP_NAME = "broker.username";
  public static final String BROKER_PASSWORD_PROP_NAME = "broker.password";
  public static final String REQUEST_RETRY_THRESHOLD = "broker.retry.threshold";
  public static final String ERROR_TRANSPORT_NAME_PROP_NAME = "error.transport.name";
  public static final String ERROR_TRANSPORT_TYPE_PROP_NAME = "error.transport.type";
  public static final String ERROR_MESSAGE_CONVERT = "error.message.convert";

  public static final String QUEUE_SIZE_HEARTBEAT_LOG_COUNT = "queue.size.log.cycle.count";
  
  public static final String MESSAGE_QUEUE_OR_TOPIC_ONLY = "If provided, the " + ERROR_TRANSPORT_TYPE_PROP_NAME + " parameter must be set to either QUEUE or TOPIC";
  public static final String MESSAGE_NO_VALUE_FOR_REQUIRED_PROPERTY = BROKER_URL_PROP_NAME + ", " + TRANSPORT_NAME_PROP_NAME + ", " + ", " + BROKER_USERNAME_PROP_NAME + " and " + BROKER_PASSWORD_PROP_NAME + " are required configuration properties with no defaults";
  public static final String MESSAGE_RETRY_THRESHOLD_MUST_BE_AN_INTEGER = REQUEST_RETRY_THRESHOLD + " must be an integer";

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  List<MessageProcessor> handlerList = new ArrayList<>();

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  Class<? extends MessageProcessor> clazz;

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  Constructor<? extends MessageProcessor> constructor;

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  Thread monitorThread;

  boolean loadFromProperties = false;
  String brokerUrl;
  String transportName;
  String errorTransportName;
  String username;
  String password;
  TransportType errorTransportType = TransportType.QUEUE;
  int requestRetryThreshold = 3;
  boolean convertErrorMessage = false;

  int messageThreshold = 10;
  int recheckPeriod = 6000;
  int maxProcessorFailures = 10;
  int cores;
  int queueSizeHeartbeatCount = 100;

  public MessageHandler(Class<? extends MessageProcessor> clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    constructor = clazz.getConstructor();
  }

  public void init() throws MessageHandlerException {
    log.trace("init()");

    try {
      if (loadFromProperties) {
        parseConfigurationFromProperties();
      }
      validateConfiguration();

      if (log.isInfoEnabled()) {
        log.info("Broker URL: {}", brokerUrl);
        log.info("Queue name: {}", transportName);
        log.info("User name: {}", username);
        log.info("Password: ************");
      }

      cores = Runtime.getRuntime()
                     .availableProcessors();
      log.info("Running with {} cores", cores);
      if (cores > 8) {
        log.info("Setting max cores of 8");
        cores = 8;
      }

      log.trace("Calling start monitor");
      startMonitor();
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
    }
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

  void parseConfigurationFromProperties() {
    brokerUrl = PropertyUtil.getProperty(BROKER_URL_PROP_NAME);
    transportName = PropertyUtil.getProperty(TRANSPORT_NAME_PROP_NAME);
    errorTransportName = PropertyUtil.getProperty(ERROR_TRANSPORT_NAME_PROP_NAME);
    username = PropertyUtil.getProperty(BROKER_USERNAME_PROP_NAME);
    password = PropertyUtil.getProperty(BROKER_PASSWORD_PROP_NAME);
    if (errorTransportName != null && !errorTransportName.isEmpty()) {
      try {
        errorTransportType = TransportType.valueOf(PropertyUtil.getProperty(ERROR_TRANSPORT_TYPE_PROP_NAME));
      } catch (NullPointerException e) {
        errorTransportType = TransportType.QUEUE;
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(MESSAGE_QUEUE_OR_TOPIC_ONLY);
      }
    }

    String retryThreshold = PropertyUtil.getProperty(REQUEST_RETRY_THRESHOLD);
    if (retryThreshold != null && !retryThreshold.isEmpty()) {
      try {
        requestRetryThreshold = Integer.parseInt(retryThreshold);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(MESSAGE_RETRY_THRESHOLD_MUST_BE_AN_INTEGER);
      }
    } else {
      log.warn("Broker retry threshold was not supplied - defaulting to {}", requestRetryThreshold);
    }

    String errorMessageConvertString = PropertyUtil.getProperty(ERROR_MESSAGE_CONVERT);
    if (errorMessageConvertString != null && !errorMessageConvertString.isEmpty()) {
      convertErrorMessage = Boolean.parseBoolean(errorMessageConvertString);
    }
    
    String queueSizeHeartbeatString = PropertyUtil.getProperty(QUEUE_SIZE_HEARTBEAT_LOG_COUNT);
    if (queueSizeHeartbeatString != null && !queueSizeHeartbeatString.isEmpty()) {
      queueSizeHeartbeatCount = Integer.parseInt(queueSizeHeartbeatString);
    }
    else {
      log.warn("The queueSizeHeartbeatCount property was not supplied - defaulting to {}", queueSizeHeartbeatCount);
    }
  }

  void validateConfiguration() {
    if (isNullOrEmpty(brokerUrl) || isNullOrEmpty(transportName) || isNullOrEmpty(username) || isNullOrEmpty(password)) {
      throw new IllegalArgumentException(MESSAGE_NO_VALUE_FOR_REQUIRED_PROPERTY);
    }
  }

  boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  Connection connection = null;
  QueueBrowser browser = null;

  private int getCurrentQueueSize() {
    // try 5 times to get
    for (int i = 0; i < 5; i++) {
      try {
        if (connection == null || browser == null) {
          connection = buildActivemqConnection(brokerUrl, username, password);
          connection.start();
          Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          Queue destination = session.createQueue(transportName);
          browser = session.createBrowser(destination);
        }

        return Collections.list((Enumeration<?>) browser.getEnumeration())
                          .size();
      } catch (JMSException e) {
        // failed to connect
        log.warn("Failed to get message count", e);
        if (connection != null) {
          try {
            connection.close();
          } catch (Exception ex) {
          }
          connection = null;
        }
        try {
          Thread.sleep(30000L);
        } catch (InterruptedException ie) {
          log.warn(ie.toString());
        }
      }
    }

    throw new RuntimeException("Failed to connect to queue and get message count");
  }

  private void startMonitor() throws MessageHandlerException {
    log.info("Starting the monitor");

    try {
      int failedProcessorBuilds = 0;
      int noActionCycles = 0;
      
      boolean monitor = true;
      while (monitor) {
        //@SuppressWarnings("unchecked")
        int msgCount = this.getCurrentQueueSize();
        log.debug("Current Queue Size: " + (msgCount));
        try {
          log.trace("Checking thresholds, count = " + msgCount + " threshold = " + messageThreshold);
          if (msgCount > messageThreshold || handlerList.isEmpty()) {
            if (handlerList.size() < cores) {
              try {
                log.info("Constructing a new Message Processor, Message Count: {}, Handler List Size: {}", msgCount, handlerList.size());
                handlerList.add(buildNewMessageProcessor());
                failedProcessorBuilds = 0;
                log.trace("############# Now " + handlerList.size() + " processors");
              } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                log.error("Error building new message processor: " + e1.getMessage(), e1);
                failedProcessorBuilds++;
                // if exceeded max failures and no active processors, exit
                // handler
                if (failedProcessorBuilds >= maxProcessorFailures && handlerList.isEmpty()) {
                  String msg = "Failed processor count: " + failedProcessorBuilds + " exceeded max failures: " + maxProcessorFailures + " and handler list empty";
                  log.error(msg);
                  throw new MessageHandlerException(msg);
                }
              }
            }
          } else if (handlerList.size() > 1) {
            log.info("Removing a Message Processor, Message Count: {}, Handler List Size: {}", msgCount, handlerList.size());
            MessageProcessor mp = handlerList.remove(handlerList.size() - 1);
            mp.terminate();
            log.trace("############# Now " + handlerList.size() + " processors");
          } else {            
            if(noActionCycles >= queueSizeHeartbeatCount) {
              log.info("No action, Message Count: {}, Handler List Size: {}", msgCount, handlerList.size());
              noActionCycles = 0;
            }
            else {
              log.debug("No action, Message Count: {}, Handler List Size: {}", msgCount, handlerList.size());
              noActionCycles++;
            }            
          }

          for (int i = 0; i < handlerList.size(); i++) {
            MessageProcessor mp = handlerList.get(i);
            if (mp.isStopped()) {
              log.info("Removing stopped processor");
              handlerList.remove(i);
            }
          }

          Thread.sleep(recheckPeriod);
        } catch (InterruptedException | IllegalArgumentException e) {
          log.error("Error in message handler", e);
        }
      }
    } catch (Exception e) {
      log.error("Error in message handler, stopping all message processor", e);
      for (int i = 0; i < handlerList.size(); i++) {
        MessageProcessor mp = handlerList.get(i);
        mp.terminate();
      }
      throw e;
    }
  }

  private MessageProcessor buildNewMessageProcessor() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    MessageProcessor mp = constructor.newInstance();
    mp.setBrokerUrl(brokerUrl);
    mp.setErrorTransportName(errorTransportName);
    mp.setErrorTransportType(errorTransportType);
    mp.setTransportName(transportName);
    mp.setUsername(username);
    mp.setPassword(password);
    mp.setRequestRetryThreshold(requestRetryThreshold);
    mp.setErrorMessageConvert(convertErrorMessage);

    mp.initialize();
    return mp;
  }

  protected static Connection buildActivemqConnection(String url, String username, String password) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    if (username != null) {
      factory.setUserName(username);
    }
    if (password != null) {
      factory.setPassword(password);
    }

    return factory.createConnection();
  }
}
