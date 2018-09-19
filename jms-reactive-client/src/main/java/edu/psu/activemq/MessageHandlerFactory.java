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


import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import edu.psu.activemq.exception.MessageHandlerException;

public class MessageHandlerFactory {
  
  Class<? extends MessageProcessor> handlerClass;

  @Inject
  @ConfigProperty(name = MessageHandler.BROKER_URL_PROP_NAME)
  String brokerUrl;

  @Inject
  @ConfigProperty(name = MessageHandler.TRANSPORT_NAME_PROP_NAME)
  String transportName;  

  @Inject
  @ConfigProperty(name = MessageHandler.ERROR_TRANSPORT_NAME_PROP_NAME)
  String errorTransportName;

  @Inject
  @ConfigProperty(name = MessageHandler.BROKER_USERNAME_PROP_NAME)
  String username;

  @Inject
  @ConfigProperty(name = MessageHandler.BROKER_PASSWORD_PROP_NAME)
  String password;

  @Inject
  @ConfigProperty(name = MessageHandler.ERROR_TRANSPORT_TYPE_PROP_NAME)
  TransportType errorTransportType;
  
  boolean loadFromProperties = false;
  
  public MessageHandlerFactory setHandler(Class<? extends MessageProcessor> handlerClass) throws InstantiationException, IllegalAccessException {
    this.handlerClass = handlerClass;
    return this;
  }
  
  public MessageHandlerFactory setBrokerUrl(String brokerUrl) {
    this.brokerUrl = brokerUrl;
    return this;
  }
  
  public MessageHandlerFactory setTransportName(String transportName) {
    this.transportName = transportName;
    return this;
  }
  
  public MessageHandlerFactory setErrorTransportName(String errorTransportName) {
    this.errorTransportName = errorTransportName;
    return this;
  }
  
  public MessageHandlerFactory setErrorTransportType(TransportType transportType) {
    this.errorTransportType = transportType;
    return this;
  }
  
  public MessageHandlerFactory setUsername(String username) {
    this.username = username;
    return this;
  }
  
  public MessageHandlerFactory setPassword(String password) {
    this.password = password;
    return this;
  }
  
  public MessageHandlerFactory isLoadFromProperties(boolean loadFromProperties){
    this.loadFromProperties = loadFromProperties;
    return this;
  }
  
  public MessageHandler build() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MessageHandlerException {
    MessageHandler mh = new MessageHandler(handlerClass);
    mh.setBrokerUrl(brokerUrl);
    mh.setErrorTransportName(errorTransportName);
    mh.setErrorTransportType(errorTransportType);
    mh.setTransportName(transportName);
    mh.setUsername(username);
    mh.setPassword(password);
    mh.setLoadFromProperties(loadFromProperties);
    
    mh.init();
    return mh;
  }
}
