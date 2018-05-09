package edu.psu.activemq.exception;

public class MessageHandlerException extends Exception {
  
  private static final long serialVersionUID = 1497512404523880592L;

  public MessageHandlerException(String why) {
    super(why);
  }
  
  public MessageHandlerException(String why, Throwable cause) {
    super(why, cause);
  }

}
