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


import dsg.gwt.numberformat.PatternInfo;
import dsg.gwt.numberformat.Util;


import java.util.Currency;

/**
 * A concrete implementation of NumberFormat that understands decimal numbers.
 */
public class DecimalFormat extends NumberFormat {

  private static final char CURRENCY_SYMBOL = '\u00A4';

  protected DecimalFormatSymbols formatSymbols;
  protected String posPrefix;
  protected String posSuffix;
  protected String negPrefix;
  protected String negSuffix;
  
  // original versions of the above before currency symbol substitution
  protected String curPosPrefix;
  protected String curPosSuffix;
  protected String curNegPrefix;
  protected String curNegSuffix;
  
  protected int multiplier;
  protected int groupingSize;
  protected boolean useExponential;
  protected int minExponentDigits;
  protected boolean forceDecimalSeparator;
  protected boolean monetary;
  protected boolean useCurrencyCode;
  protected String pattern;
  
  // Used to keep track of prefix/suffix monetary flags separately;
  // since they are set separately, we wouldn't be able to clear
  // the monetary flag otherwise.
  private boolean monetaryPrefix;
  private boolean monetarySuffix;

  public DecimalFormat() {
    this(new DecimalFormatSymbols());
  }
  
  public DecimalFormat(String pattern) {
    this();
    applyPattern(pattern);
  }
  
  public DecimalFormat(String pattern, DecimalFormatSymbols formatSymbols) {
    this(formatSymbols);
    applyPattern(pattern);
  }

  protected DecimalFormat(DecimalFormatSymbols formatSymbols) {
    this.formatSymbols = formatSymbols;
    curPosPrefix = curPosSuffix = curNegSuffix = posPrefix = posSuffix = negSuffix = "";
    curNegPrefix = negPrefix = String.valueOf(formatSymbols.getMinusSign());
    multiplier = 1;
  }

  public void applyLocalizedPattern(String pattern) {
    pattern = pattern.replace(formatSymbols.getCurrencySymbol(), String.valueOf(CURRENCY_SYMBOL));
    pattern = pattern.replace(formatSymbols.getDecimalSeparator(), '.');
    pattern = pattern.replace(formatSymbols.getGroupingSeparator(), ',');
    pattern = pattern.replace(formatSymbols.getDigit(), '#');
    pattern = pattern.replace(formatSymbols.getExponentSeparator(), "E");
    pattern = pattern.replace(formatSymbols.getPercent(), '%');
    pattern = pattern.replace(formatSymbols.getPerMill(), '\u2030');
    pattern = pattern.replace(formatSymbols.getZeroDigit(), '0');
    pattern = pattern.replace(formatSymbols.getMinusSign(), '-');
    pattern = pattern.replace(formatSymbols.getInfinity(), "\u221E");
    applyPattern(pattern);
  }

  public void applyPattern(String pattern) {
    this.pattern = pattern;
    PatternInfo patternInfo = new PatternInfo(pattern);
    multiplier = patternInfo.multiplier;
    curPosPrefix = patternInfo.posPrefix;
    curPosSuffix = patternInfo.posSuffix;
    useCurrencyCode = false;
    int idx = curPosPrefix.indexOf(CURRENCY_SYMBOL);
    monetaryPrefix = (idx >= 0);
    if (monetaryPrefix) {
      useCurrencyCode |= (idx < curPosPrefix.length()
          && curPosPrefix.charAt(idx + 1) == CURRENCY_SYMBOL);
    }
    idx = curPosSuffix.indexOf(CURRENCY_SYMBOL);
    monetarySuffix = (idx >= 0);
    if (monetarySuffix) {
      useCurrencyCode |= (idx < curPosSuffix.length()
          && curPosSuffix.charAt(idx + 1) == CURRENCY_SYMBOL);
    }
    monetary = monetaryPrefix | monetarySuffix;
    curNegPrefix = patternInfo.negPrefix;
    curNegSuffix = patternInfo.negSuffix;
    if ((curNegPrefix == null || curNegPrefix.length() == 0)
        && (curNegSuffix == null || curNegSuffix.length() == 0)) {
      curNegPrefix = String.valueOf(formatSymbols.getMinusSign()) + curPosPrefix;
      curNegSuffix = "";
    }
    posPrefix = currencySubstitute(curPosPrefix);
    posSuffix = currencySubstitute(curPosSuffix);
    negPrefix = currencySubstitute(curNegPrefix);
    negSuffix = currencySubstitute(curNegSuffix);
    groupingSize = patternInfo.groupingSize;
    groupingUsed = patternInfo.groupingUsed;
    useExponential = patternInfo.useExponent;
    minExponentDigits = patternInfo.minExpDigits;
    forceDecimalSeparator = patternInfo.forceDecimal;
    maxFractionDigits = patternInfo.maxFracDigits;
    minFractionDigits = patternInfo.minFracDigits;
    minIntegerDigits = patternInfo.minIntDigits;
    maxIntegerDigits = patternInfo.maxIntDigits;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DecimalFormat)) {
      return false;
    }
    DecimalFormat df = (DecimalFormat) other;
    return super.equals(df)
        && Util.nullEquals(formatSymbols, df.formatSymbols)
        && Util.nullEquals(posPrefix, df.posPrefix)
        && Util.nullEquals(negPrefix, df.negPrefix)
        && Util.nullEquals(negSuffix, df.negSuffix)
        && Util.nullEquals(posSuffix, df.posSuffix)
        && multiplier == df.multiplier
        && groupingSize == df.groupingSize
        && useExponential == df.useExponential
        && minExponentDigits == df.minExponentDigits
        && useCurrencyCode == df.useCurrencyCode
        && monetary == df.monetary
        && forceDecimalSeparator == df.forceDecimalSeparator;
  }

  public DecimalFormatSymbols getDecimalFormatSymbols() {
    return formatSymbols;
  }
  
  public int getGroupingSize() {
    return groupingSize;
  }

  public int getMultiplier() {
    return multiplier;
  }
  
  public String getNegativePrefix() {
    return negPrefix;
  }
  
  public String getNegativeSuffix() {
    return negSuffix;
  }
  
  public String getPositivePrefix() {
    return posPrefix;
  }
  
  public String getPositiveSuffix() {
    return posSuffix;
  }
  
  @Override
  public int hashCode() {
    return super.hashCode() * 2
        + Util.nullHash(formatSymbols) * 3
        + Util.nullHash(posPrefix) * 5
        + Util.nullHash(negPrefix) * 7
        + Util.nullHash(negSuffix) * 11
        + Util.nullHash(posSuffix) * 13
        + multiplier * 17
        + groupingSize * 19
        + (useExponential ? 23 : 0)
        + minExponentDigits * 29
        + (useCurrencyCode ? 31 : 0)
        + (monetary ? 37 : 0)
        + (forceDecimalSeparator ? 41 : 0);
  }
  
  public boolean isDecimalSeparatorAlwaysShown() {
    return forceDecimalSeparator;
  }
  
  @Override
  public Number parse(String text, ParsePosition pos) {
    int idx = pos.getIndex();
    boolean gotPositive, gotNegative;
    Number ret = 0;

    gotPositive = (text.indexOf(posPrefix, idx) == idx);
    gotNegative = (text.indexOf(negPrefix, idx) == idx);

    if (gotPositive && gotNegative) {
      if (posPrefix.length() > negPrefix.length()) {
        gotNegative = false;
      } else if (posPrefix.length() < negPrefix.length()) {
        gotPositive = false;
      }
    }

    if (gotPositive) {
      idx += posPrefix.length();
    } else if (gotNegative) {
      idx += negPrefix.length();
    } else {
      // TODO(jat): handle case where number distinguishable only by suffix.
      
      // Must get either positive or negative prefix for a valid parse.
      pos.setErrorIndex(idx);
      return null;
    }

    // Process digits or Inf, and find decimal position.
    if (text.indexOf(formatSymbols.getInfinity(), idx) == idx) {
      idx += formatSymbols.getInfinity().length();
      ret = Double.POSITIVE_INFINITY;
    } else if (text.indexOf(formatSymbols.getNaN(), idx) == idx) {
      idx += formatSymbols.getNaN().length();
      ret = Double.NaN;
    } else {
      int[] inOutPos = new int[] { idx };
      try {
        ret = parseNumber(text, inOutPos);
      } catch (NumberFormatException nfe) {
        pos.setErrorIndex(inOutPos[0]);
        return null;
      }
      idx = inOutPos[0];
    }

    // Check for suffix.
    if (gotPositive) {
      if (!(text.indexOf(posSuffix, idx) == idx)) {
        pos.setErrorIndex(idx);
        return null;
      }
      idx += posSuffix.length();
    } else if (gotNegative) {
      if (!(text.indexOf(negSuffix, idx) == idx)) {
        pos.setErrorIndex(idx);
        return null;
      }
      idx += negSuffix.length();
    }

    if (gotNegative) {
      // TODO(jat): BigDecimal/BigInteger support
      if (ret instanceof Long) {
        ret = Long.valueOf(-ret.longValue());
      } else {
        ret = Double.valueOf(-ret.doubleValue());
      }
    }
    pos.setIndex(idx);
    return ret;
  }

  @Override
  public void setCurrency(Currency currency) {
    super.setCurrency(currency);
    formatSymbols.setCurrency(currency);
    this.posPrefix = currencySubstitute(curPosPrefix);
    this.posSuffix = currencySubstitute(curPosSuffix);
    this.negPrefix = currencySubstitute(curNegPrefix);
    this.negSuffix = currencySubstitute(curNegSuffix);
  }

  public void setDecimalFormatSymbols(DecimalFormatSymbols formatSymbols) {
    this.formatSymbols = formatSymbols;
  }

  public void setDecimalSeparatorAlwaysShown(boolean forceDecimalSeparator) {
    this.forceDecimalSeparator = forceDecimalSeparator;
  }
  
  public void setGroupingSize(int groupingSize) {
    this.groupingSize = groupingSize;
  }
  
  public void setMultiplier(int multiplier) {
    this.multiplier = multiplier;
  }
  
  public void setNegativePrefix(String negPrefix) {
    this.curNegPrefix = negPrefix;
    this.negPrefix = currencySubstitute(negPrefix);
  }
  
  public void setNegativeSuffix(String negSuffix) {
    this.curNegSuffix = negSuffix;
    this.negSuffix = currencySubstitute(negSuffix);
  }
  
  public void setPositivePrefix(String posPrefix) {
    this.curPosPrefix = posPrefix;
    int idx = posPrefix.indexOf(CURRENCY_SYMBOL);
    monetaryPrefix = (idx >= 0);
    if (monetaryPrefix) {
      useCurrencyCode = (idx < posPrefix.length() && posPrefix.charAt(idx + 1) == CURRENCY_SYMBOL);
    }
    monetary = monetaryPrefix | monetarySuffix;
    this.posPrefix = currencySubstitute(posPrefix);
  }
  
  public void setPositiveSuffix(String posSuffix) {
    this.curPosSuffix = posSuffix;
    int idx = posSuffix.indexOf(CURRENCY_SYMBOL);
    monetarySuffix = idx >= 0;
    if (monetarySuffix) {
      useCurrencyCode = (idx < posSuffix.length() && posSuffix.charAt(idx + 1) == CURRENCY_SYMBOL);
    }
    monetary = monetaryPrefix | monetarySuffix;
    this.posSuffix = currencySubstitute(posSuffix);
  }
  
  // TODO(jat): BigDecimal support
  //    isParseBigDecimal
  //    setParseBigDecimal
  
  public String toLocalizedPattern() {
    String locPattern = toPattern();
    locPattern = locPattern.replace(String.valueOf(CURRENCY_SYMBOL),
        formatSymbols.getCurrencySymbol());
    locPattern = locPattern.replace('.', formatSymbols.getDecimalSeparator());
    locPattern = locPattern.replace(',', formatSymbols.getGroupingSeparator());
    locPattern = locPattern.replace('#', formatSymbols.getDigit());
    locPattern = locPattern.replace("E", formatSymbols.getExponentSeparator());
    locPattern = locPattern.replace('%', formatSymbols.getPercent());
    locPattern = locPattern.replace('\u2030', formatSymbols.getPerMill());
    locPattern = locPattern.replace('0', formatSymbols.getZeroDigit());
    locPattern = locPattern.replace('-', formatSymbols.getMinusSign());
    locPattern = locPattern.replace("\u221E", formatSymbols.getInfinity());
    return locPattern;
  }
  
  public String toPattern() {
    if (pattern != null) {
      return pattern;
    }
    // TODO(jat): implement
    return null;
  }
  
  @Override
  protected StringBuffer format(double number, StringBuffer buf) {
    if (Double.isNaN(number)) {
      buf.append(formatSymbols.getNaN());
      return buf;
    }
    boolean isNegative = ((number < 0.0) || (number == 0.0 && 1 / number < 0.0));
    buf.append(isNegative ? negPrefix : posPrefix);
    if (Double.isInfinite(number)) {
      buf.append(formatSymbols.getInfinity());
    } else {
      if (isNegative) {
        number = -number;
      }
      number *= multiplier;
      if (useExponential) {
        subformatExponential(number, buf);
      } else {
        subformatFixed(number, buf, minIntegerDigits);
      }
    }
    buf.append(isNegative ? negSuffix : posSuffix);
    return buf;
  }
  
  @Override
  protected StringBuffer format(long number, StringBuffer buf) {
    boolean isNegative = number < 0;
    buf.append(isNegative ? negPrefix : posPrefix);
    if (isNegative) {
      number = -number;
    }
    number *= multiplier;
    if (useExponential) {
      subformatExponential(number, buf);
    } else {
      subformatFixed(number, buf, minIntegerDigits);
    }

    buf.append(isNegative ? negSuffix : posSuffix);
    return buf;
  }
  
  /**
   * This method formats the exponent part of a double.
   * 
   * @param exponent exponential value
   * @param result formatted exponential part will be append to it
   */
  private void addExponentPart(int exponent, StringBuffer result) {
    result.append(formatSymbols.getExponentSeparator());

    if (exponent < 0) {
      exponent = -exponent;
      result.append(formatSymbols.getMinusSign());
    }

    String exponentDigits = String.valueOf(exponent);
    for (int i = exponentDigits.length(); i < minExponentDigits; ++i) {
      result.append(formatSymbols.getZeroDigit());
    }
    result.append(exponentDigits);
  }
  
  private String currencySubstitute(String str) {
    if (monetary) {
      str = str.replace("\u00A4\u00A4", formatSymbols.getCurrency().getCurrencyCode());
      str = str.replace("\u00A4", formatSymbols.getCurrencySymbol());
    }
    return str;
  }

  private int getDigit(char ch) {
    char zero = formatSymbols.getZeroDigit();
    if (ch >= zero && ch <= zero + 9) {
      return ch - zero;
    }
    return Character.digit(ch, 10);
  }

  /**
   * This does the work of String.valueOf(long), but given a double as input
   * and avoiding our emulated longs.  Contrasted with String.valueOf(double),
   * it ensures (a) there will be no trailing .0, and (b) unwinds E-notation.
   *  
   * @param number the integral value to convert
   * @return the string representing that integer
   */
  private String makeIntString(double number) {
    return String.valueOf(number);
  }

  /**
   * This function parses a "localized" text into a <code>double</code>. It
   * needs to handle locale specific decimal, grouping, exponent and digit.
   * 
   * @param text the text that need to be parsed
   * @param pos in/out parsing position. in case of failure, this shouldn't be
   *          changed
   * @return double value, could be 0.0 if nothing can be parsed
   */
  private Number parseNumber(String text, int[] pos) {
    // TODO(jat): support for long and BigDecimal
    double ret;
    boolean sawDecimal = false;
    boolean sawExponent = false;
    int scale = 1;
    char decimal = monetary ? formatSymbols.getMonetaryDecimalSeparator()
        : formatSymbols.getDecimalSeparator();
    char grouping = formatSymbols.getGroupingSeparator();
    String exponentChar = formatSymbols.getExponentSeparator();

    StringBuffer normalizedText = new StringBuffer();
    int len = text.length();
    for (; pos[0] < len; ++pos[0]) {
      char ch = text.charAt(pos[0]);
      int digit = getDigit(ch);
      if (digit >= 0 && digit <= 9) {
        normalizedText.append((char) (digit + '0'));
      } else if (ch == decimal) {
        if (sawDecimal || sawExponent) {
          throw new NumberFormatException("Decimal separator repeated or in exponent");
        }
        normalizedText.append('.');
        sawDecimal = true;
      } else if (ch == grouping) {
        if (sawDecimal || sawExponent) {
          throw new NumberFormatException("Grouping separator outside mantissa");
        }
        continue;
      } else if (ch == exponentChar.charAt(0)) {
        if (sawExponent) {
          throw new NumberFormatException("Multiple exponents");
        }
        normalizedText.append('E');
        sawExponent = true;
      } else if (ch == '+' || ch == '-') {
        normalizedText.append(ch);
      } else if (ch == formatSymbols.getPercent()) {
        if (scale != 1) {
          throw new NumberFormatException("Multiple scaling characters");
        }
        scale = 100;
        break;
      } else if (ch == formatSymbols.getPerMill()) {
        if (scale != 1) {
          throw new NumberFormatException("Multiple scaling characters");
        }
        scale = 1000;
        break;
      } else {
        throw new NumberFormatException("Unexpected character '" + ch + "'");
      }
    }
    String nText = normalizedText.toString();
    ret = Double.parseDouble(nText);
    return Double.valueOf(ret / scale);
  }

  /**
   * This method formats a <code>double</code> in exponential format.
   * 
   * @param number value need to be formated
   * @param result where the formatted string goes
   */
  private void subformatExponential(double number, StringBuffer result) {
    if (number == 0.0) {
      subformatFixed(number, result, minIntegerDigits);
      addExponentPart(0, result);
      return;
    }

    int exponent = (int) Math.floor(Math.log(number) / Math.log(10));
    number /= Math.pow(10, exponent);

    int minIntDigits = minIntegerDigits;
    if (maxIntegerDigits > 1 && maxIntegerDigits > minIntegerDigits) {
      // A repeating range is defined; adjust to it as follows.
      // If repeat == 3, we have 6,5,4=>3; 3,2,1=>0; 0,-1,-2=>-3;
      // -3,-4,-5=>-6, etc. This takes into account that the
      // exponent we have here is off by one from what we expect;
      // it is for the format 0.MMMMMx10^n.
      while ((exponent % maxIntegerDigits) != 0) {
        number *= 10;
        exponent--;
      }
      minIntDigits = 1;
    } else {
      // No repeating range is defined; use minimum integer digits.
      if (minIntegerDigits < 1) {
        exponent++;
        number /= 10;
      } else {
        for (int i = 1; i < minIntegerDigits; i++) {
          exponent--;
          number *= 10;
        }
      }
    }

    subformatFixed(number, result, minIntDigits);
    addExponentPart(exponent, result);
  }

  /**
   * This method formats a <code>long</code> in exponential format.
   * 
   * @param number value need to be formated
   * @param result where the formatted string goes
   */
  private void subformatExponential(long number, StringBuffer result) {
    if (number == 0) {
      subformatFixed(number, result, minIntegerDigits);
      addExponentPart(0, result);
      return;
    }

    // TODO(jat): roundoff issues converting long to double?
    int exponent = (int) Math.floor(Math.log(number) / Math.log(10));
    number /= Math.pow(10, exponent);

    int minIntDigits = minIntegerDigits;
    if (maxIntegerDigits > 1 && maxIntegerDigits > minIntegerDigits) {
      // A repeating range is defined; adjust to it as follows.
      // If repeat == 3, we have 6,5,4=>3; 3,2,1=>0; 0,-1,-2=>-3;
      // -3,-4,-5=>-6, etc. This takes into account that the
      // exponent we have here is off by one from what we expect;
      // it is for the format 0.MMMMMx10^n.
      while ((exponent % maxIntegerDigits) != 0) {
        number *= 10;
        exponent--;
      }
      minIntDigits = 1;
    } else {
      // No repeating range is defined; use minimum integer digits.
      if (minIntegerDigits < 1) {
        exponent++;
        number /= 10;
      } else {
        for (int i = 1; i < minIntegerDigits; i++) {
          exponent--;
          number *= 10;
        }
      }
    }

    subformatFixed(number, result, minIntDigits);
    addExponentPart(exponent, result);
  }

  /**
   * This method formats a <code>double</code> into a fractional
   * representation.
   * 
   * @param number value need to be formated
   * @param result result will be written here
   * @param minIntDigits minimum integer digits
   */
  private void subformatFixed(double number, StringBuffer result,
      int minIntDigits) {
    // Round the number.
    double power = Math.pow(10, maxFractionDigits);
    double intValue = Math.floor(number);
    // we don't want to use Math.round, 'cause that returns a long which JS
    // then has to emulate... Math.floor(x + 0.5d) is defined to be equivalent
    double fracValue = Math.floor((number - intValue) * power + 0.5d);
    if (fracValue >= power) {
      intValue += 1.0;
      fracValue -= power;
    }

    boolean fractionPresent = (minFractionDigits > 0) || (fracValue > 0);

    String intPart = makeIntString(intValue);
    // TODO(jat): do we need a monetary grouping separator?
    char grouping = formatSymbols.getGroupingSeparator();
    char decimal = monetary ? formatSymbols.getMonetaryDecimalSeparator()
        : formatSymbols.getDecimalSeparator();

    int zeroDelta = formatSymbols.getZeroDigit() - '0';
    int digitLen = intPart.length();
    
    if (intValue > 0 || minIntDigits > 0) {
      for (int i = digitLen; i < minIntDigits; i++) {
        result.append(formatSymbols.getZeroDigit());
      }

      for (int i = 0; i < digitLen; i++) {
        result.append((char) (intPart.charAt(i) + zeroDelta));

        if (digitLen - i > 1 && groupingSize > 0
            && ((digitLen - i) % groupingSize == 1)) {
          result.append(grouping);
        }
      }
    } else if (!fractionPresent) {
      // If there is no fraction present, and we haven't printed any
      // integer digits, then print a zero.
      result.append(formatSymbols.getZeroDigit());
    }

    // Output the decimal separator if necessary.
    if (forceDecimalSeparator || fractionPresent) {
      result.append(decimal);
    }

    // To make sure a leading zero will be kept.
    String fracPart = makeIntString(Math.floor(fracValue + power + 0.5d));
    int fracLen = fracPart.length();
    while (fracPart.charAt(fracLen - 1) == '0' && fracLen > minFractionDigits + 1) {
      fracLen--;
    }

    for (int i = 1; i < fracLen; i++) {
      result.append((char) (fracPart.charAt(i) + zeroDelta));
    }
  }

  /**
   * This method formats a non-negative <code>long</code> into a fractional
   * representation.
   * 
   * @param number value need to be formated
   * @param result result will be written here
   * @param minIntDigits minimum integer digits
   */
  private void subformatFixed(long number, StringBuffer result,
      int minIntDigits) {
    boolean fractionPresent = minFractionDigits > 0;

    String intPart = String.valueOf(number);
    // TODO(jat): do we need a monetary grouping separator?
    char grouping = formatSymbols.getGroupingSeparator();
    char decimal = monetary ? formatSymbols.getMonetaryDecimalSeparator()
        : formatSymbols.getDecimalSeparator();

    int zeroDelta = formatSymbols.getZeroDigit() - '0';
    int digitLen = intPart.length();
    
    if (number > 0 || minIntDigits > 0) {
      for (int i = digitLen; i < minIntDigits; i++) {
        result.append(formatSymbols.getZeroDigit());
      }

      for (int i = 0; i < digitLen; i++) {
        result.append((char) (intPart.charAt(i) + zeroDelta));

        if (digitLen - i > 1 && groupingSize > 0
            && ((digitLen - i) % groupingSize == 1)) {
          result.append(grouping);
        }
      }
    } else if (!fractionPresent) {
      // If there is no fraction present, and we haven't printed any
      // integer digits, then print a zero.
      result.append(formatSymbols.getZeroDigit());
    }

    // Output the decimal separator if we always do so.
    if (forceDecimalSeparator || fractionPresent) {
      result.append(decimal);
    }
    for (int i = 0; i < minFractionDigits; i++) {
      result.append(formatSymbols.getZeroDigit());
    }
  }
}
