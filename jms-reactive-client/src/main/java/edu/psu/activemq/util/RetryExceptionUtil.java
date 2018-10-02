package edu.psu.activemq.util;

import edu.psu.activemq.exception.UnableToProcessMessageException;

public class RetryExceptionUtil {
  
  private RetryExceptionUtil() {
    
  }
  
  public static final int DEFAULT_RETRY = 1000 * 60 * 5; //1000 millis * 60 seconds * 5 minutes ==> 5 minutes

  public static UnableToProcessMessageException createRetryException(String message) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(RetryExceptionUtil.DEFAULT_RETRY);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryException(String message, int retryAmount) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(retryAmount);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryException(String message, Exception e) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(RetryExceptionUtil.DEFAULT_RETRY);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryException(String message, Exception e, int retryAmount) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(retryAmount);
    return utpe;
  }
  
}
