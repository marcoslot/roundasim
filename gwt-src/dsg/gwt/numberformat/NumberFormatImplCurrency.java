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
 * Default implementation of NumberFormat.getCurrencyInstance().
 */
public class NumberFormatImplCurrency extends NumberFormatImpl {

  protected NumberFormatImplCurrency() {
    super();
    int digits = getCurrency().getDefaultFractionDigits();
    setMinimumFractionDigits(digits);
    setMaximumFractionDigits(digits);
    setGroupingUsed(true);
    setGroupingSize(3);
    setPositivePrefix("\u00a4");
    setNegativePrefix('(' + getPositivePrefix());
    setNegativeSuffix(")");
  }
}