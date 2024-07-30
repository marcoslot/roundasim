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

package java.util;

import java.io.Serializable;


import dsg.gwt.numberformat.CurrencyData;
import dsg.gwt.numberformat.CurrencyList;
import dsg.gwt.numberformat.Util;

/**
 * Represents a currency using ISO4217 codes.
 */
public final class Currency implements Serializable {

  private static Map<String, Currency> currencies = new HashMap<String, Currency>();

  public static Currency getInstance(CurrencyData cdata) {
    String code = cdata.getCurrencyCode();
    Currency currency = currencies.get(code);
    if (currency == null) {
      currency = new Currency(code, cdata.getDefaultFractionDigits(),
          cdata.getCurrencySymbol());
      currencies.put(code, currency);
    }
    return currency;
  }

  public static Currency getInstance(String currencyCode) {
    Currency currency = currencies.get(currencyCode);
    if (currency == null) {
      CurrencyData cdata = CurrencyList.get().lookup(currencyCode);
      currency = new Currency(currencyCode, cdata.getDefaultFractionDigits(),
          cdata.getCurrencySymbol());
      currencies.put(currencyCode, currency);
    }
    return currency;
  }

  // TODO: implement the following methods:
  // static Currency getInstance(Locale)

  private String code;

  private int defaultFractionDigits;

  private String symbol;

  private Currency(String currencyCode, int defaultFractionDigits,
      String defaultSymbol) {
    this.code = currencyCode;
    this.defaultFractionDigits = defaultFractionDigits;
    this.symbol = defaultSymbol;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Currency)) {
      return false;
    }
    Currency cur = (Currency) other;
    return Util.nullEquals(code, cur.code) && Util.nullEquals(symbol, cur.symbol)
        && defaultFractionDigits == cur.defaultFractionDigits;
  }

  public String getCurrencyCode() {
    return code;
  }

  public int getDefaultFractionDigits() {
    return defaultFractionDigits;
  }

  public String getSymbol() {
    return symbol;
  }

  @Override
  public int hashCode() {
    return Util.nullHash(code) + Util.nullHash(symbol) * 2 + defaultFractionDigits * 3;
  }

  @Override
  public String toString() {
    return code;
  }
}