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
package dsg.gwt.numberformat;

/**
 * Utility functions for GWT library code.
 */
public class Util {

  /**
   * Performs an equals test on two objects which may be null.
   * 
   * @param obj1
   * @param obj2
   * @return true if both objects are null or obj1.equals(obj2)
   */
  public static boolean nullEquals(Object obj1, Object obj2) {
    return obj1 == null ? obj2 == null : obj1.equals(obj2);
  }

  /**
   * Computes the hash code of an object which may be null.
   * 
   * @param obj
   * @return 0 if obj is null, obj.hashCode() otherwise.
   */
  public static int nullHash(Object obj) {
    return obj == null ? 0 : obj.hashCode();
  }
}
