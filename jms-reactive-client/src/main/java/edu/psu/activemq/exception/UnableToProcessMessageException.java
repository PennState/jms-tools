package edu.psu.activemq.exception;

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


import lombok.Getter;
import lombok.Setter;

public class UnableToProcessMessageException extends RuntimeException {
  
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
