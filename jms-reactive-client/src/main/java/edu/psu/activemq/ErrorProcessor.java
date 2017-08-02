package edu.psu.activemq;

import javax.jms.Message;

interface ErrorProcessor {

  void handleError(Message message);
  void handleError(Message message, Throwable exception);
}
