package edu.psu.activemq.util;

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

import edu.psu.activemq.exception.UnableToProcessMessageException;
import edu.psu.activemq.exception.UnableToProcessMessageException.RetryStyle;

public class RetryExceptionUtil {
  
  private RetryExceptionUtil() {
    
  }
  
  public static final int DEFAULT_RETRY = 1000 * 60 * 5; //1000 millis * 60 seconds * 5 minutes ==> 5 minutes

  public static UnableToProcessMessageException createRetryException(String message) {
    return createRetryBackoffException(message);
  }
  
  public static UnableToProcessMessageException createRetryException(String message, int retryWait) {
    return createRetryBackoffException(message, retryWait);
  }
  
  public static UnableToProcessMessageException createRetryException(String message, Exception e) {
    return createRetryBackoffException(message, e);
  }
  
  public static UnableToProcessMessageException createRetryException(String message, Exception e, int retryWait) {
    return createRetryBackOffException(message, e, retryWait);
  }
  

  public static UnableToProcessMessageException createRetryBackoffException(String message) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(RetryExceptionUtil.DEFAULT_RETRY);
    utpe.setRetryStyle(RetryStyle.EXPONENTIAL);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryBackoffException(String message, int retryWait) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(retryWait);
    utpe.setRetryStyle(RetryStyle.EXPONENTIAL);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryBackoffException(String message, Exception e) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(RetryExceptionUtil.DEFAULT_RETRY);
    utpe.setRetryStyle(RetryStyle.EXPONENTIAL);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryBackOffException(String message, Exception e, int retryWait) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(retryWait);
    utpe.setRetryStyle(RetryStyle.EXPONENTIAL);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryBackOffException(String message, int retryWait, double backOffMultiplier, int numberOfRetries) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(retryWait);
    utpe.setRetryStyle(RetryStyle.EXPONENTIAL);
    utpe.setBackOffMultiplier(backOffMultiplier);
    utpe.setNumberOfRetries(numberOfRetries);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryBackOffException(String message, Exception e, int retryWait, double backOffMultiplier, int numberOfRetries) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(retryWait);
    utpe.setRetryStyle(RetryStyle.EXPONENTIAL);
    utpe.setBackOffMultiplier(backOffMultiplier);
    utpe.setNumberOfRetries(numberOfRetries);
    return utpe;
  }

  public static UnableToProcessMessageException createRetryNonBackoffException(String message) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(RetryExceptionUtil.DEFAULT_RETRY);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryNonBackoffException(String message, int retryWait) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message);
    utpe.setRetry(retryWait);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryNonBackoffException(String message, Exception e) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(RetryExceptionUtil.DEFAULT_RETRY);
    return utpe;
  }
  
  public static UnableToProcessMessageException createRetryNonBackoffException(String message, Exception e, int retryWait) {
    UnableToProcessMessageException utpe = new UnableToProcessMessageException(message, e);
    utpe.setRetry(retryWait);
    return utpe;
  }
  
}
