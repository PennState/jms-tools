package edu.psu.activemq;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import edu.psu.activemq.exception.UnableToProcessMessageException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TypeDelegatingMessageProcessor extends MessageProcessor {

  private Map<String, MapValue<? extends Object>> mapValueMap;

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
    String type = "todo";
    
    TextMessage tm = (TextMessage) message;
    try {
      MapValue mapValue = mapValueMap.get(type);
      mapValue.getProcessorConsumer().accept(mapValue.getConvertFunction().apply(tm.getText()));
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
 
  protected <T> void register(String key, Function<String,T> function, Consumer<T> consumer) {
    MapValue<T> mapValue = new MapValue<>();
    mapValue.setConvertFunction(function);
    mapValue.setProcessorConsumer(consumer);
    
    mapValueMap.put(key, mapValue);
  }
  
  @Data
  class MapValue<T> {
    Function<String, T> convertFunction;
    Consumer<T> processorConsumer;
  }

}
