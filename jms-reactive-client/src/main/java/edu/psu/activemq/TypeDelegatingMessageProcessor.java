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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import edu.psu.activemq.exception.DelegateException;
import edu.psu.activemq.exception.UnableToProcessMessageException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TypeDelegatingMessageProcessor extends MessageProcessor {
  
  //TODO: figure out what the type property should be actuall called
  public static final String KEY_PROPERTY = "processor_key";
  public static final String DEFAULT_TYPE_PROPERTY = "type";

  private Map<String, MapValue<? extends Object>> mapValueMap = new HashMap<>();

  protected abstract void registerDelegatedMap();

  @Override
  protected void initialize() {
    log.info("Initializing TypeDelegatingMessageProcessor...");
    registerDelegatedMap();
    
    super.initialize();
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void handleMessage(Message message) throws UnableToProcessMessageException {
    TextMessage tm = (TextMessage) message;
    
    try {
      String type = extractKey(tm).orElseThrow(()-> new UnableToProcessMessageException("Unable to determine message type"));
      MapValue mapValue = mapValueMap.get(type);
      mapValue.getProcessorConsumer().accept(mapValue.getConvertFunction().apply(tm.getText()));
    } catch (JMSException | DelegateException e) {
      throw new UnableToProcessMessageException(e.getMessage(), e);
    }
  }
 
  protected <T> void register(String key, Function<String,T> function, Consumer<T> consumer) {
    MapValue<T> mapValue = new MapValue<>();
    mapValue.setConvertFunction(function);
    mapValue.setProcessorConsumer(consumer);
    
    mapValueMap.put(key, mapValue);
  }
  
  protected <T> void register(String key, TypeProcessor<T> delegate) {
    MapValue<T> mapValue = new MapValue<>();
    mapValue.setConvertFunction(delegate::parseMessage);
    mapValue.setProcessorConsumer(delegate::processMessage);
    
    mapValueMap.put(key, mapValue);
  }
  
  public static Optional<String> extractKey(TextMessage message) throws JMSException {
    String key = extractTypeFromProperty(message).orElse(null);
    if (key != null) {
      return Optional.of(key);
    }
    
    return extractTypeFromJson(message.getText());
  }
  
  public static Optional<String> extractTypeFromProperty(Message message) throws JMSException {
    return Optional.ofNullable(message.getStringProperty(KEY_PROPERTY));
  }
  
  public static Optional<String> extractTypeFromJson(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(json);
      if (node == null) {
        log.warn("JSON String returned null node");
        return Optional.empty();
      }
      JsonNode typeNode = node.path("type");
      if (typeNode == null) {
        log.warn("type node is null");
        return Optional.empty();
      }
      if (typeNode instanceof TextNode) {
        return Optional.ofNullable(typeNode.asText());
      }
      log.warn("TypeNode was not an instance of TextNode");
      return Optional.empty();
    } catch (IOException e) {
      log.warn("Unable to determine to convert JSON String to JsonNode");
      return Optional.empty();
    }
  }
  
  @Data
  private class MapValue<T> {
    Function<String, T> convertFunction;
    Consumer<T> processorConsumer;
  }


}
