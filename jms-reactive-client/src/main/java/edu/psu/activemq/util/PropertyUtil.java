package edu.psu.activemq.util;

public class PropertyUtil {

  public static String getProperty(String name){
    String property = System.getProperty(name);
    if(property != null){
      return property;
    }
    
    String envVar = System.getenv(name.toUpperCase().replaceAll("\\.", "_"));
    if(envVar != null){
      return envVar;
    }
    
    return null;
  }
  
}
