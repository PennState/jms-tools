package edu.psu.activemq.stub;

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

import edu.psu.activemq.TypeDelegate;
import edu.psu.activemq.exception.DelegateException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlternateDelegate implements TypeDelegate<AlternateMessage> {

  @Getter
  private int messageCount = 0;

  @Override
  public AlternateMessage parseMessage(String json) throws DelegateException {
    try {
      return TestMessage.createJsonMapper().readValue(json, AlternateMessage.class);
    } catch (IOException e) {
      throw new DelegateException(e);
    }
  }

  @Override
  public void processMessage(AlternateMessage t) throws DelegateException {
    messageCount++;
  }

}

