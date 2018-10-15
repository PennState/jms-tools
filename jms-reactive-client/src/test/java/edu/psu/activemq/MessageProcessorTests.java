package edu.psu.activemq;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.psu.activemq.exception.UnableToProcessMessageException;
import edu.psu.activemq.exception.UnableToProcessMessageException.RetryStyle;
import edu.psu.activemq.stub.StubMessageProcessor;

public class MessageProcessorTests {

  @Test
  public void getRetryCountTest() throws JMSException {
    int expectedRetryCount = 3;
    StubMessageProcessor mp = new StubMessageProcessor();

    ActiveMQMessage message = new ActiveMQMessage();
    message.setStringProperty(StubMessageProcessor.DELIVERY_COUNT_PROP_NAME, Integer.toString(expectedRetryCount));
    int actualRetryCount = mp.getRetryCount(message);

    Assertions.assertEquals(expectedRetryCount, actualRetryCount);
  }

  @Test
  public void shouldRetryTrueTest() throws JMSException {
    boolean expectedShouldRetry = true;

    StubMessageProcessor mp = new StubMessageProcessor();

    int retryCount = 1;
    ActiveMQMessage message = new ActiveMQMessage();
    message.setStringProperty(StubMessageProcessor.DELIVERY_COUNT_PROP_NAME, Integer.toString(retryCount));

    UnableToProcessMessageException upme = new UnableToProcessMessageException("Test Exception");
    upme.setRetry(1000);
    upme.setNumberOfRetries(3);

    boolean actualShouldRetry = mp.shouldRetry(message, upme);

    Assertions.assertEquals(expectedShouldRetry, actualShouldRetry);
  }

  @Test
  public void shouldRetryFalseTest() throws JMSException {
    boolean expectedShouldRetry = false;

    StubMessageProcessor mp = new StubMessageProcessor();

    int retryCount = 3;
    ActiveMQMessage message = new ActiveMQMessage();
    message.setStringProperty(StubMessageProcessor.DELIVERY_COUNT_PROP_NAME, Integer.toString(retryCount));

    UnableToProcessMessageException upme = new UnableToProcessMessageException("Test Exception");
    upme.setRetry(1000);
    upme.setNumberOfRetries(3);

    boolean actualShouldRetry = mp.shouldRetry(message, upme);

    Assertions.assertEquals(expectedShouldRetry, actualShouldRetry);
  }

  @Test
  public void calculateRetryWaitLinearTest() throws JMSException {
    long expectedRetryWait = (long) 1000;

    StubMessageProcessor mp = new StubMessageProcessor();

    UnableToProcessMessageException upme = new UnableToProcessMessageException("Test Exception");
    upme.setRetry(1000);
    upme.setNumberOfRetries(3);

    long actualRetryWait = mp.calculateRetryWait(1, upme);

    Assertions.assertEquals(expectedRetryWait, actualRetryWait);
  }

  @Test
  public void calculateRetryWaitExponentialTest() throws JMSException {

    StubMessageProcessor mp = new StubMessageProcessor();

    UnableToProcessMessageException upme = new UnableToProcessMessageException("Test Exception");
    upme.setRetry(1000);
    upme.setRetryStyle(RetryStyle.EXPONENTIAL);
    upme.setNumberOfRetries(10);

    for (int i = 1; i < upme.getNumberOfRetries(); i++) {
      long expectedRetryWait = (long) (i * (double) 1.5 * upme.getRetryWait());
      long actualRetryWait = mp.calculateRetryWait(i, upme);
      Assertions.assertEquals(expectedRetryWait, actualRetryWait);
    }
  }

  @Test
  public void produceRetryMessageTest() throws JMSException, IOException {

    StubMessageProcessor mp = new StubMessageProcessor();

    int retryCount = 3;
    ActiveMQMessage message = new ActiveMQMessage();
    message.setStringProperty(StubMessageProcessor.DELIVERY_COUNT_PROP_NAME, Integer.toString(retryCount));

    UnableToProcessMessageException upme = new UnableToProcessMessageException("Test Exception");
    upme.setRetry(1000);

    long retryWait = mp.calculateRetryWait(1, upme);
    ActiveMQMessage actualMessage = mp.produceRetryMessage(message, upme);

    Assertions.assertEquals(null, actualMessage.getStringProperty(StubMessageProcessor.AMQ_JOB_ID_PROP_NAME));
    Assertions.assertEquals(Long.toString(retryWait), actualMessage.getStringProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY));
    Assertions.assertEquals("4", actualMessage.getStringProperty(StubMessageProcessor.DELIVERY_COUNT_PROP_NAME));
  }

}
