package edu.psu.activemq;

import java.lang.reflect.InvocationTargetException;

public class MessageHandlerFactory {
  
  Class<? extends MessageProcessor> handlerClass;
  String ip;
  String transportName;
  String errorIp;
  String errorTransportName;
  TransportType errorTransportType;
  
  public MessageHandlerFactory setHandler(Class<? extends MessageProcessor> handlerClass) throws InstantiationException, IllegalAccessException {
    this.handlerClass = handlerClass;
    return this;
  }
  
  public MessageHandlerFactory setIp(String ip) {
    this.ip = ip;
    return this;
  }
  
  public MessageHandlerFactory setTransportName(String transportName) {
    this.transportName = transportName;
    return this;
  }
  
  public MessageHandlerFactory setErrorIp(String ip) {
    this.errorIp = ip;
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
  
  public MessageHandler build() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    return new MessageHandler(handlerClass, ip, transportName);
  }
}
