package edu.psu.activemq;

import edu.psu.activemq.TypeDelegatingMessageProcessor;
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

import lombok.Getter;
import lombok.Setter;

public class StubTypeDelegateProcessor extends TypeDelegatingMessageProcessor {

  @Getter
  @Setter
  ExampleDelegate delegate = new ExampleDelegate();
  
  @Getter
  @Setter
  AlternateDelegate altDelegate = new AlternateDelegate();

  @Override
  protected void registerDelegatedMap() {
    register(TestMessage.TYPE, delegate);
    register(AlternateMessage.TYPE, altDelegate);
  }

}

