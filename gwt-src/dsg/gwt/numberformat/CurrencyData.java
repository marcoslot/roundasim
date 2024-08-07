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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSO Overlay type that wraps currency data.
 * 
 * The JSO is an array with three elements:
 *   0 - ISO4217 currency code
 *   1 - currency symbol to use for this locale
 *   2 - flags & # of decimal digits:
 *       d0-d2: # of decimal digits, 0-7
 *       d3:    currency symbol goes before number
 *       d4:    currency symbol position is fixed (? need exp)
 *       d5:    space goes ?
 *       d6:    SPACE_FIXED_FLAG
 *   3 - portable currency symbol (optional)
 */
public final class CurrencyData extends JavaScriptObject {
  
  // Public so CurrencyListGenerator can get to them.  As usual with an
  // impl package, external code should not rely on these values.
  public static final int POS_FIXED_FLAG = 16;
  public static final int POS_SUFFIX_FLAG = 8;
  public static final int PRECISION_MASK = 7;
  public static final int SPACE_FIXED_FLAG = 64;
  public static final int SPACE_PREFIX_FLAG = 32;

  protected CurrencyData() {
  }
  /**
   * @return the ISO4217 code for this currency
   */
  public native String getCurrencyCode() /*-{
    return this[0];
  }-*/;

  /**
   * @return the default symbol to use for this currency
   */
  public native String getCurrencySymbol() /*-{
    return this[1];
  }-*/;

  /**
   * @return the default number of decimal positions for this currency
   */
  public int getDefaultFractionDigits() {
    return getFlagsAndPrecision() & PRECISION_MASK;
  }

  /**
   * @return the default symbol to use for this currency
   */
  public native String getPortableCurrencySymbol() /*-{
    return this[3] || this[1];
  }-*/;

  public boolean isSpaceFixed() {
    return (getFlagsAndPrecision() & SPACE_FIXED_FLAG) != 0;
  }
  
  public boolean isSpacePrefix() {
    return (getFlagsAndPrecision() & SPACE_PREFIX_FLAG) != 0;
  }
  
  public boolean isSymbolPositionFixed() {
    return (getFlagsAndPrecision() & POS_FIXED_FLAG) != 0;
  }
  
  public boolean isSymbolPrefix() {
    return (getFlagsAndPrecision() & POS_SUFFIX_FLAG) != 0;
  }
  
  private native int getFlagsAndPrecision() /*-{
    return this[2];
  }-*/;
}