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
 * Implementation detail of LocaleInfo -- not a public API and subject to
 * change.
 * 
 * Generated interface for locale information.  The default implementation
 * returns null, which is used if the i18n module is not imported.
 * 
 * @see dsg.gwt.numberformat.LocaleInfo
 */
public class LocaleInfoImpl {

  /**
   * @return an array of available locale names
   */
  public String[] getAvailableLocaleNames() {
    return null;
  }

  /**
   * @return the current locale name, such as "default, "en_US", etc.
   */
  public String getLocaleName() {
    return null;
  }
  
  /**
   * Return the display name of the requested locale in its native locale, if
   * possible. If no native localization is available, the English name will
   * be returned, or as a last resort just the locale name will be returned.  If
   * the locale name is unknown (including an user overrides), null is returned.
   * 
   * @param localeName the name of the locale to lookup.
   * @return the name of the locale in its native locale
   */
  public String getLocaleNativeDisplayName(String localeName) {
    return null;
  }
}
