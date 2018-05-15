package edu.psu.activemq.util;

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
    
    String envVar = System.getenv(name.toUpperCase().replaceAll("\\.", "_"));
    if(envVar != null){
      return envVar;
    }
    
    return null;
  }
  
}
