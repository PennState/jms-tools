package edu.psu.activemq.exception;

public class UnableToProcessMessageException extends Exception {
  
  private static final long serialVersionUID = 1497512404523880592L;

  public UnableToProcessMessageException(String why) {
    super(why);
  }
  
  public UnableToProcessMessageException(String why, Throwable cause) {
    super(why, cause);
  }

}
