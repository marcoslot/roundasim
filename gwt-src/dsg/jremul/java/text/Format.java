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
 * Abstract base class for locale-sensitive formatting.
 * 
 * Intentionally not serializable because we would have no way of
 * properly building objects on the other side of the wire.
 */
public abstract class Format {
  
  public final String format(Object obj) {
    return format(obj, new StringBuffer()).toString();
  }

  public Object parseObject(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object parsedObject = parseObject(source, pos);
    if (pos.getIndex() == 0) {
      throw new ParseException("Unable to parse \"" + source + "\"",
          pos.getErrorIndex());
    }
    return parsedObject;
  }

  public abstract Object parseObject(String source, ParsePosition pos);

  /**
   * This is an implementation method that is not exposed publicly, as we do not
   * want to support the complexities of FieldPosition, at least not with the
   * current state of compiler optimizations.
   * 
   * Thus, the format(Object, StringBuffer, FieldPosition) method is not
   * present.
   */
  protected abstract StringBuffer format(Object obj, StringBuffer buf);
}