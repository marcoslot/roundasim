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
 * NumberFormat pattern parsing utility.
 */
public class PatternInfo {

  private static final char PATTERN_SEPARATOR = ';';
  private static final char PATTERN_ZERO_DIGIT = '0';
  private static final char PATTERN_DIGIT = '#';
  private static final char PATTERN_GROUPING_SEPARATOR = ',';
  private static final char PATTERN_DECIMAL_SEPARATOR = '.';
  private static final char PATTERN_EXPONENT = 'E';
  private static final char PERCENT = '%';
  private static final char PERMILLE = '\u2030';
  private static final char QUOTE = '\'';
  
  public boolean groupingUsed;
  public int groupingSize;
  public String posPrefix;
  public String posSuffix;
  public String negPrefix;
  public String negSuffix;
  public int multiplier;
  public boolean forceDecimal;
  public boolean useExponent;
  public int minIntDigits;
  public int maxIntDigits;
  public int minFracDigits;
  public int maxFracDigits;
  public int minExpDigits;
  private int minExponentDigits;

  public PatternInfo(String pattern) {
    int pos = 0;
    multiplier = 1;
    StringBuffer affix = new StringBuffer();

    pos = parseAffix(pattern, pos, affix);
    posPrefix = affix.toString();
    pos = parseTrunk(pattern, pos);
    pos = parseAffix(pattern, pos, affix);
    posSuffix = affix.toString();

    if (pos < pattern.length() && pattern.charAt(pos) == PATTERN_SEPARATOR) {
      ++pos;
      pos = parseAffix(pattern, pos, affix);
      negPrefix = affix.toString();
      // The negative pattern is always the same as the positive one except for the
      // prefix/suffix, so we just skip it here.
      pos = skipTrunk(pattern, pos);
      pos = parseAffix(pattern, pos, affix);
      negSuffix = affix.toString();
    }
  }
  /**
   * This method parses affix part of pattern.
   * 
   * @param pattern pattern string that need to be parsed
   * @param pos start position to parse
   * @param affix store the parsed result
   * @return how many characters parsed
   */
  private int parseAffix(String pattern, int pos, StringBuffer affix) {
    affix.delete(0, affix.length());
    boolean inQuote = false;
    int len = pattern.length();

    for (; pos < len; ++pos) {
      char ch = pattern.charAt(pos);
      if (ch == QUOTE) {
        if ((pos + 1) < len && pattern.charAt(pos + 1) == QUOTE) {
          ++pos;
          affix.append("'"); // 'don''t'
        } else {
          inQuote = !inQuote;
        }
        continue;
      }

      if (inQuote) {
        affix.append(ch);
      } else {
        switch (ch) {
          case PATTERN_DIGIT:
          case PATTERN_ZERO_DIGIT:
          case PATTERN_GROUPING_SEPARATOR:
          case PATTERN_DECIMAL_SEPARATOR:
          case PATTERN_SEPARATOR:
            return pos;
          case PERCENT:
            if (multiplier != 1) {
              throw new IllegalArgumentException(
                  "Too many percent/per mille characters in pattern \""
                      + pattern + '"');
            }
            multiplier = 100;
            break;
          case PERMILLE:
            if (multiplier != 1) {
              throw new IllegalArgumentException(
                  "Too many percent/per mille characters in pattern \""
                      + pattern + '"');
            }
            multiplier = 1000;
            break;
          default:
            // The currency symbol is not treated specially here.
            break;
        }
        affix.append(ch);
      }
    }
    return pos;
  }

  /**
   * This method parses the trunk part of a pattern.
   * 
   * @param pattern pattern string that need to be parsed
   * @param start where parse started
   * @return how many characters parsed
   */
  private int parseTrunk(String pattern, int pos) {
    int decimalPos = -1;
    int digitLeftCount = 0, zeroDigitCount = 0, digitRightCount = 0;
    int groupingCount = -1;

    int len = pattern.length();
    boolean loop = true;
    for (; (pos < len) && loop; ++pos) {
      char ch = pattern.charAt(pos);
      switch (ch) {
        case PATTERN_DIGIT:
          if (zeroDigitCount > 0) {
            ++digitRightCount;
          } else {
            ++digitLeftCount;
          }
          if (groupingUsed && decimalPos < 0) {
            ++groupingCount;
          }
          break;
        case PATTERN_ZERO_DIGIT:
          if (digitRightCount > 0) {
            throw new IllegalArgumentException("Unexpected '0' in pattern \""
                + pattern + '"');
          }
          ++zeroDigitCount;
          if (groupingUsed && decimalPos < 0) {
            ++groupingCount;
          }
          break;
        case PATTERN_GROUPING_SEPARATOR:
          groupingUsed = true;
          groupingCount = 0;
          break;
        case PATTERN_DECIMAL_SEPARATOR:
          if (decimalPos >= 0) {
            throw new IllegalArgumentException(
                "Multiple decimal separators in pattern \"" + pattern + '"');
          }
          decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
          break;
        case PATTERN_EXPONENT:
          if (useExponent) {
            throw new IllegalArgumentException("Multiple exponential "
                + "symbols in pattern \"" + pattern + '"');
          }
          useExponent = true;
          minExponentDigits = 0;

          // Use lookahead to parse out the exponential part
          // of the pattern, then jump into phase 2.
          while ((pos + 1) < len && pattern.charAt(pos + 1) == PATTERN_ZERO_DIGIT) {
            ++pos;
            ++minExponentDigits;
          }

          if ((digitLeftCount + zeroDigitCount) < 1 || minExponentDigits < 1) {
            throw new IllegalArgumentException("Malformed exponential "
                + "pattern \"" + pattern + '"');
          }
          loop = false;
          break;
        default:
          --pos;
          loop = false;
          break;
      }
    }

    if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
      // Handle "###.###" and "###." and ".###".
      int n = decimalPos;
      if (n == 0) { // Handle ".###"
        ++n;
      }
      digitRightCount = digitLeftCount - n;
      digitLeftCount = n - 1;
      zeroDigitCount = 1;
    }

    // Do syntax checking on the digits.
    if ((decimalPos < 0 && digitRightCount > 0)
        || (decimalPos >= 0 && (decimalPos < digitLeftCount || decimalPos > (digitLeftCount + zeroDigitCount)))
        || groupingCount == 0) {
      throw new IllegalArgumentException("Malformed pattern \"" + pattern + '"');
    }
    int totalDigits = digitLeftCount + zeroDigitCount + digitRightCount;

    maxFracDigits = (decimalPos >= 0 ? (totalDigits - decimalPos) : 0);
    if (decimalPos >= 0) {
      minFracDigits = digitLeftCount + zeroDigitCount - decimalPos;
      if (minFracDigits < 0) {
        minFracDigits = 0;
      }
    }

    /*
     * The effectiveDecimalPos is the position the decimal is at or would be at
     * if there is no decimal. Note that if decimalPos<0, then digitTotalCount ==
     * digitLeftCount + zeroDigitCount.
     */
    int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : totalDigits;
    minIntDigits = effectiveDecimalPos - digitLeftCount;
    if (useExponent) {
      maxIntDigits = digitLeftCount + minIntDigits;

      // In exponential display, integer part can't be empty.
      if (maxFracDigits == 0 && minIntDigits == 0) {
        minIntDigits = 1;
      }
    }

    this.groupingSize = (groupingCount > 0) ? groupingCount : 0;
    forceDecimal = (decimalPos == 0 || decimalPos == totalDigits);

    return pos;
  }
  
  private int skipTrunk(String pattern, int pos) {
    int len = pattern.length();
    while (pos < len) {
      int cp = pattern.codePointAt(pos);
      if (cp != PATTERN_DIGIT && cp != PATTERN_ZERO_DIGIT && cp != PATTERN_GROUPING_SEPARATOR
          && cp != PATTERN_DECIMAL_SEPARATOR && cp != PATTERN_EXPONENT) {
        break;
      }
      pos += Character.charCount(cp);
    }
    return pos;
  }
}