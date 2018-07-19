package edu.psu.activemq;

import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.psu.activemq.exception.TypeProcessorException;
import edu.psu.activemq.exception.UnableToProcessMessageException;
import edu.psu.activemq.stub.AlternateDelegate;
import edu.psu.activemq.stub.AlternateMessage;
import edu.psu.activemq.stub.ExampleDelegate;
import edu.psu.activemq.stub.TestMessage;

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

@ExtendWith(MockitoExtension.class)
public class TypeDelegateMesageProcessorTest {

  @Spy
  private StubTypeDelegateProcessor processor;

  @Spy
  private ExampleDelegate delegate;
  
  @Spy
  private AlternateDelegate altDelegate;

  @BeforeEach
  public void init() {
    processor.setDelegate(delegate);
    processor.setAltDelegate(altDelegate);
    processor.registerDelegatedMap();
  }
  
  @Test
  public void testMessageProcessing() throws Exception {
    final int loop = 3;
    //process message multiple times to verify that the parse and process methods of delegate are executed
    TextMessage msg = createDefaultMessage();
    
    for(int i = 1; i <= loop; i++) {
      processor.handleMessage(msg);
      Mockito.verify(delegate, Mockito.times(i)).parseMessage(Mockito.anyString());
      Mockito.verify(delegate, Mockito.times(i)).processMessage(Mockito.any(TestMessage.class));
      Assertions.assertEquals(i, delegate.getMessageCount());
    }
    
    TextMessage altMsg = createAlternateMessage();
    for(int i = 1; i <= loop; i++) {
      processor.handleMessage(altMsg);
      Mockito.verify(altDelegate, Mockito.times(i)).parseMessage(Mockito.anyString());
      Mockito.verify(altDelegate, Mockito.times(i)).processMessage(Mockito.any(AlternateMessage.class));
      Assertions.assertEquals(i, altDelegate.getMessageCount());
    }
  }

  
  @Test
  public void testDelegateParseMessageThrowingError() throws Exception {
    String error = "Forcing Error";
    Mockito.doThrow(new TypeProcessorException(error)).when(delegate).parseMessage(Mockito.anyString());
    TextMessage msg = createDefaultMessage();
    UnableToProcessMessageException ex = Assertions.assertThrows(UnableToProcessMessageException.class, () -> processor.handleMessage(msg));
    Assertions.assertEquals(error, ex.getMessage());
  }

  @Test
  public void testDelegateProcessMessageThrowingError() throws Exception {
    String error = "Forcing Error";
    Mockito.doThrow(new TypeProcessorException(error)).when(delegate).processMessage(Mockito.any(TestMessage.class));
    TextMessage msg = createDefaultMessage();
    UnableToProcessMessageException ex = Assertions.assertThrows(UnableToProcessMessageException.class, () -> processor.handleMessage(msg));
    Assertions.assertEquals(error, ex.getMessage());
  }

  @Test
  public void testExtractTypeFromJson() {
    String json = TestMessage.defaultJson();
    String type = TypeDelegatingMessageProcessor.extractTypeFromJson(json)
      .orElseThrow(()-> new IllegalArgumentException("Did not extract type from JSON"));
    Assertions.assertEquals(TestMessage.TYPE, type);
  }

  @Test
  public void testextractTypeFromProperty() throws Exception {
    TextMessage msg = createDefaultMessage();
    String type = TypeDelegatingMessageProcessor.extractTypeFromProperty(msg)
      .orElseThrow(()-> new IllegalArgumentException("Did not extract type from JMS Property"));
    Assertions.assertEquals(TestMessage.TYPE, type);
  }

  private static TextMessage createDefaultMessage() throws Exception {
    TextMessage msg = new ActiveMQTextMessage();
    msg.setStringProperty(TypeDelegatingMessageProcessor.KEY_PROPERTY, TestMessage.TYPE);
    msg.setText(TestMessage.defaultJson());
    return msg;
  }
  
  private static TextMessage createAlternateMessage() throws Exception {
    TextMessage msg = new ActiveMQTextMessage();
    msg.setStringProperty(TypeDelegatingMessageProcessor.KEY_PROPERTY, AlternateMessage.TYPE);
    msg.setText(AlternateMessage.defaultJson());
    return msg;
  }
}
