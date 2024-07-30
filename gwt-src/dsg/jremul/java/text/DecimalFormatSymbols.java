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



import java.util.Currency;

import dsg.gwt.numberformat.LocaleInfo;

/**
 * TODO(jat): Class description here
 * 
 */
public class DecimalFormatSymbols {

  private static boolean nullEquals(Object obj1, Object obj2) {
    return obj1 == null ? obj2 == null : obj1.equals(obj2);
  }

  private static int nullHash(Object obj) {
    return obj == null ? 0 : obj.hashCode();
  }
  private Currency currency;
  private String currencySymbol;

  private String internationalCurrencySymbol;
  private char decimalSeparator;
  private char digit;
  private String exponentSeparator;
  private char groupingSeparator;
  private String infinity;
  private char minusSign;
  private char monetaryDecimalSeparator;
  private String nan;
  private char patternSeparator;
  private char percent;
  private char perMill;

  private char zeroDigit;

  public DecimalFormatSymbols() {
    exponentSeparator = "E"; // added in JRE 1.6, so may not get set
    LocaleInfo.getCurrentLocale().getDecimalFormatSymbols(this);
  }

  DecimalFormatSymbols(Currency currency, String currencySymbol,
      String internationalCurrencySymbol, char decimalSeparator, char digit,
      String exponentSeparator, char groupingSeparator, String infinity,
      char minusSign, char monetaryDecimalSeparator, String nan,
      char patternSeparator, char percent, char perMill, char zeroDigit) {
    this.currency = currency;
    this.currencySymbol = currencySymbol;
    this.internationalCurrencySymbol = internationalCurrencySymbol;
    this.decimalSeparator = decimalSeparator;
    this.digit = digit;
    this.exponentSeparator = exponentSeparator;
    this.groupingSeparator = groupingSeparator;
    this.infinity = infinity;
    this.minusSign = minusSign;
    this.monetaryDecimalSeparator = monetaryDecimalSeparator;
    this.nan = nan;
    this.patternSeparator = patternSeparator;
    this.percent = percent;
    this.perMill = perMill;
    this.zeroDigit = zeroDigit;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DecimalFormatSymbols)) {
      return false;
    }
    DecimalFormatSymbols dfs = (DecimalFormatSymbols) other;
    return currency == dfs.currency // only one instance of each Currency
        && nullEquals(currencySymbol, dfs.currencySymbol)
        && nullEquals(internationalCurrencySymbol, dfs.internationalCurrencySymbol)
        && decimalSeparator == dfs.decimalSeparator
        && digit == dfs.digit
        && nullEquals(exponentSeparator, dfs.exponentSeparator)
        && groupingSeparator == dfs.groupingSeparator
        && nullEquals(infinity, dfs.infinity)
        && minusSign == dfs.minusSign
        && monetaryDecimalSeparator == dfs.monetaryDecimalSeparator
        && nullEquals(nan, dfs.nan)
        && patternSeparator == dfs.patternSeparator
        && percent == dfs.percent
        && perMill == dfs.perMill
        && zeroDigit == dfs.zeroDigit;
  }

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return currency;
  }

  /**
   * @return the currencySymbol
   */
  public String getCurrencySymbol() {
    return currencySymbol;
  }

  /**
   * @return the decimalSeparator
   */
  public char getDecimalSeparator() {
    return decimalSeparator;
  }

  /**
   * @return the digit
   */
  public char getDigit() {
    return digit;
  }

  /**
   * @return the exponentSeparator
   */
  public String getExponentSeparator() {
    return exponentSeparator;
  }

  /**
   * @return the groupingSeparator
   */
  public char getGroupingSeparator() {
    return groupingSeparator;
  }

  /**
   * @return the infinity
   */
  public String getInfinity() {
    return infinity;
  }

  /**
   * @return the internationalCurrencySymbol
   */
  public String getInternationalCurrencySymbol() {
    return internationalCurrencySymbol;
  }

  /**
   * @return the minusSign
   */
  public char getMinusSign() {
    return minusSign;
  }

  /**
   * @return the monetaryDecimalSeparator
   */
  public char getMonetaryDecimalSeparator() {
    return monetaryDecimalSeparator;
  }

  /**
   * @return the naN
   */
  public String getNaN() {
    return nan;
  }

  /**
   * @return the patternSeparator
   */
  public char getPatternSeparator() {
    return patternSeparator;
  }

  /**
   * @return the percent
   */
  public char getPercent() {
    return percent;
  }

  /**
   * @return the perMill
   */
  public char getPerMill() {
    return perMill;
  }

  /**
   * @return the zeroDigit
   */
  public char getZeroDigit() {
    return zeroDigit;
  }

  @Override
  public int hashCode() {
    return nullHash(currency) * 2
        + nullHash(currencySymbol) * 3
        + nullHash(internationalCurrencySymbol) * 5
        + decimalSeparator * 7
        + digit * 11
        + nullHash(exponentSeparator) * 13
        + groupingSeparator * 17
        + nullHash(infinity) * 19
        + minusSign * 23
        + monetaryDecimalSeparator * 29
        + nullHash(nan) * 31
        + patternSeparator * 37
        + percent * 41
        + perMill * 43
        + zeroDigit * 53;
  }

  /**
   * @param currency the currency to set
   */
  public void setCurrency(Currency currency) {
    this.currency = currency;
    this.currencySymbol = currency.getSymbol();
    this.internationalCurrencySymbol = currency.getCurrencyCode();
  }

  /**
   * @param currencySymbol the currencySymbol to set
   */
  public void setCurrencySymbol(String currencySymbol) {
    this.currencySymbol = currencySymbol;
  }

  /**
   * @param decimalSeparator the decimalSeparator to set
   */
  public void setDecimalSeparator(char decimalSeparator) {
    this.decimalSeparator = decimalSeparator;
  }

  /**
   * @param digit the digit to set
   */
  public void setDigit(char digit) {
    this.digit = digit;
  }

  /**
   * @param exponentSeparator the exponentSeparator to set
   */
  public void setExponentSeparator(String exponentSeparator) {
    this.exponentSeparator = exponentSeparator;
  }

  /**
   * @param groupingSeparator the groupingSeparator to set
   */
  public void setGroupingSeparator(char groupingSeparator) {
    this.groupingSeparator = groupingSeparator;
  }

  /**
   * @param infinity the infinity to set
   */
  public void setInfinity(String infinity) {
    this.infinity = infinity;
  }

  /**
   * @param internationalCurrencySymbol the internationalCurrencySymbol to set
   */
  public void setInternationalCurrencySymbol(String internationalCurrencySymbol) {
    // TODO(jat): add lookup when Currency.getInstance(String currencyCode) is added.
    this.internationalCurrencySymbol = internationalCurrencySymbol;
  }

  /**
   * @param minusSign the minusSign to set
   */
  public void setMinusSign(char minusSign) {
    this.minusSign = minusSign;
  }

  /**
   * @param monetaryDecimalSeparator the monetaryDecimalSeparator to set
   */
  public void setMonetaryDecimalSeparator(char monetaryDecimalSeparator) {
    this.monetaryDecimalSeparator = monetaryDecimalSeparator;
  }

  /**
   * @param naN the naN to set
   */
  public void setNaN(String nan) {
    this.nan = nan;
  }

  /**
   * @param patternSeparator the patternSeparator to set
   */
  public void setPatternSeparator(char patternSeparator) {
    this.patternSeparator = patternSeparator;
  }

  /**
   * @param percent the percent to set
   */
  public void setPercent(char percent) {
    this.percent = percent;
  }

  /**
   * @param perMill the perMill to set
   */
  public void setPerMill(char perMill) {
    this.perMill = perMill;
  }

  /**
   * @param zeroDigit the zeroDigit to set
   */
  public void setZeroDigit(char zeroDigit) {
    this.zeroDigit = zeroDigit;
  }
}