package edu.psu.activemq.exception;

import lombok.Getter;
import lombok.Setter;

public class UnableToProcessMessageException extends Exception {
  
  public enum HandleAction {
    ERROR,
    RETRY,
    DROP
  }
  
  int retryWait = 0;
  HandleAction handleAction = HandleAction.ERROR;
  
  @Getter
  @Setter
  String shortDescription;
  
  @Getter
  @Setter
  String sourceSystem;
  
  private static final long serialVersionUID = 1497512404523880592L;

  public UnableToProcessMessageException(String why) {
    super(why);
  }
  
  public UnableToProcessMessageException(String why, Throwable cause) {
    super(why, cause);
  }
  
  public void setRetry(int retryWait) {
    handleAction = HandleAction.RETRY;
    this.retryWait = retryWait;
  }
  
  public void setDrop() {
    handleAction = HandleAction.DROP;
    this.retryWait = 0;
  }
  
  public HandleAction getHandleAction() {
    return handleAction;
  }
  
  public int getRetryWait() {
    return retryWait;
  }
}
