package edu.psu.activemq;

import java.lang.reflect.InvocationTargetException;

import edu.psu.activemq.exception.MessageHandlerException;

public class MessageHandlerFactory {
  
  Class<? extends MessageProcessor> handlerClass;
  String brokerUrl;
  String transportName;  
  String errorTransportName;
  String username;
  String password;
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
