package edu.psu.activemq.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Arrays;

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


public class PropertyUtil {

  public static String getProperty(String name){
    String property = System.getProperty(name);
    if(property != null){
      return property;
    }
    
    String envVar = getSystenEnv(name);
    if(envVar != null){
      return envVar;
    }
    
    return null;
  }
  
  public static String[] getPropertyArray(String name, String separator) {
    String value = getProperty(name);
    return separateValues(value, separator);
  }
  
  public static List<String> getPropertyList(String name, String separator) {
    List<String> list = new ArrayList<>();
    String[] arrays = getPropertyArray(name, separator);
    Collections.addAll(list, arrays);
    return list;
  }
  
  private static String getSystenEnv(String name) {
    String normalized = name.toUpperCase().replaceAll("\\.", "_");
    return System.getenv(normalized);
  }
  
  private static String[] separateValues(String value, String separator) {
    if (value == null) {
      return new String[]{};
    }
    return value.split(separator);
  }
}
