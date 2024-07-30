/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.text;

/**
 * Keeps track of position in parsing input.
 */
public class ParsePosition {
  private int index;
  private int errorIndex;

  ParsePosition(int index) {
    this.index = index;
    errorIndex = -1;
  }
  
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ParsePosition)) {
      return false;
    }
    ParsePosition pos = (ParsePosition) other;
    return index == pos.index && errorIndex == pos.errorIndex;
  }
  
  public int getErrorIndex() {
    return errorIndex;
  }
  
  public int getIndex() {
    return index;
  }
  
  @Override
  public int hashCode() {
    return index ^ errorIndex;
  }
  
  public void setErrorIndex(int errorIndex) {
    this.errorIndex = errorIndex;
  }
  
  public void setIndex(int index) {
    this.index = index;
  }
  
  @Override
  public String toString() {
    return "idx=" + String.valueOf(index) + ", ei=" + errorIndex;
  }
}