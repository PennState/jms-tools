package edu.psu.activemq;

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


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.psu.activemq.stub.StubMessageProcessor;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTests {

    @Spy
    MessageProcessor messageProcessor;

    MessageHandler messageHandler;

    @BeforeEach
    void beforeEach() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        messageHandler = new MessageHandler(StubMessageProcessor.class);
    }

    @AfterEach
    void afterEach() {
        System.setProperty(MessageHandler.BROKER_URL_PROP_NAME, "");
        System.setProperty(MessageHandler.BROKER_USERNAME_PROP_NAME, "");
        System.setProperty(MessageHandler.BROKER_PASSWORD_PROP_NAME, "");
        System.setProperty(MessageHandler.TRANSPORT_NAME_PROP_NAME, "");

        System.setProperty(MessageHandler.ERROR_TRANSPORT_NAME_PROP_NAME, "");
        System.setProperty(MessageHandler.ERROR_TRANSPORT_TYPE_PROP_NAME, "");
        System.setProperty(MessageHandler.REQUEST_RETRY_THRESHOLD, "");
    }

    @Test
    void noErrorTransportTypeIsSetIfTheErrorTransportNameIsNull() {
        System.setProperty(MessageHandler.ERROR_TRANSPORT_TYPE_PROP_NAME, TransportType.TOPIC.name());
        messageHandler.parseConfigurationFromProperties();
        
        assertThat(messageHandler.errorTransportType).isEqualTo(TransportType.QUEUE);
    }

    @Test
    void noErrorTransportTypeIsSetIfTheErrorTransportNameIsEmpty() {
        System.setProperty(MessageHandler.ERROR_TRANSPORT_NAME_PROP_NAME, "");
        System.setProperty(MessageHandler.ERROR_TRANSPORT_TYPE_PROP_NAME, TransportType.TOPIC.name());
        messageHandler.parseConfigurationFromProperties();

        assertThat(messageHandler.errorTransportType).isEqualTo(TransportType.QUEUE);
    }

    @Test
    void errorTransportTypeIsSetIfTheErrorTransportNameIsSet() {
        System.setProperty(MessageHandler.ERROR_TRANSPORT_NAME_PROP_NAME, "any.name.will.do");
        System.setProperty(MessageHandler.ERROR_TRANSPORT_TYPE_PROP_NAME, TransportType.TOPIC.name());
        messageHandler.parseConfigurationFromProperties();

        assertThat(messageHandler.errorTransportType).isEqualTo(TransportType.TOPIC);
    }

    @Test
    void errorTransportTypeIsQueueOrTopicIfSet() {
        System.setProperty(MessageHandler.ERROR_TRANSPORT_NAME_PROP_NAME, "any.name.will.do");
        System.setProperty(MessageHandler.ERROR_TRANSPORT_TYPE_PROP_NAME, "NOT_QUEUE_OR_TOPIC");

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> messageHandler.parseConfigurationFromProperties());

        assertThat(throwable.getMessage()).isEqualTo(MessageHandler.MESSAGE_QUEUE_OR_TOPIC_ONLY);
    }

    @Test
    void exitsIfRetryThresholdIsNotAnInteger() {
        System.setProperty(MessageHandler.REQUEST_RETRY_THRESHOLD, "not-an-integer");

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> messageHandler.parseConfigurationFromProperties());

        assertThat(throwable.getMessage()).isEqualTo(MessageHandler.MESSAGE_RETRY_THRESHOLD_MUST_BE_AN_INTEGER);
    }

    @Test
    void exitsIfRequiredBrokerUrlParameterIsMissing() {
        System.setProperty(MessageHandler.TRANSPORT_NAME_PROP_NAME, "transport.name");
        System.setProperty(MessageHandler.BROKER_USERNAME_PROP_NAME, "broker.username");
        System.setProperty(MessageHandler.BROKER_PASSWORD_PROP_NAME, "broker.password");

        messageHandler.parseConfigurationFromProperties();
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> messageHandler.validateConfiguration());

        assertThat(throwable.getMessage()).isEqualTo(MessageHandler.MESSAGE_NO_VALUE_FOR_REQUIRED_PROPERTY);
    }

    @Test
    void exitsIfRequiredTransportNameParameterIsMissing() {
        System.setProperty(MessageHandler.BROKER_URL_PROP_NAME, "broker.url");
        System.setProperty(MessageHandler.BROKER_USERNAME_PROP_NAME, "broker.username");
        System.setProperty(MessageHandler.BROKER_PASSWORD_PROP_NAME, "broker.password");

        messageHandler.parseConfigurationFromProperties();
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> messageHandler.validateConfiguration());

        assertThat(throwable.getMessage()).isEqualTo(MessageHandler.MESSAGE_NO_VALUE_FOR_REQUIRED_PROPERTY);
    }

    @Test
    void exitsIfRequiredUsernameParameterIsMissing() {
        System.setProperty(MessageHandler.BROKER_URL_PROP_NAME, "broker.url");
        System.setProperty(MessageHandler.TRANSPORT_NAME_PROP_NAME, "transport.name");
        System.setProperty(MessageHandler.BROKER_PASSWORD_PROP_NAME, "broker.password");

        messageHandler.parseConfigurationFromProperties();
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> messageHandler.validateConfiguration());

        assertThat(throwable.getMessage()).isEqualTo(MessageHandler.MESSAGE_NO_VALUE_FOR_REQUIRED_PROPERTY);
    }

    @Test
    void exitsIfRequiredPasswordParameterIsMissing() {
        System.setProperty(MessageHandler.BROKER_URL_PROP_NAME, "broker.url");
        System.setProperty(MessageHandler.TRANSPORT_NAME_PROP_NAME, "transport.name");
        System.setProperty(MessageHandler.BROKER_USERNAME_PROP_NAME, "broker.username");

        messageHandler.parseConfigurationFromProperties();
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> messageHandler.validateConfiguration());

        assertThat(throwable.getMessage()).isEqualTo(MessageHandler.MESSAGE_NO_VALUE_FOR_REQUIRED_PROPERTY);
    }

    @Test
    void succeedsIfRequiredParametersAreProvided() {
        System.setProperty(MessageHandler.BROKER_URL_PROP_NAME, "broker.url");
        System.setProperty(MessageHandler.BROKER_USERNAME_PROP_NAME, "broker.username");
        System.setProperty(MessageHandler.BROKER_PASSWORD_PROP_NAME, "broker.password");
        System.setProperty(MessageHandler.TRANSPORT_NAME_PROP_NAME, "transport.name");

        messageHandler.parseConfigurationFromProperties();
        messageHandler.validateConfiguration();
    }

}
