/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers;

import org.gbif.api.vocabulary.License;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.ParseResult;

import java.net.URI;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/license.txt to lookup a
 * License by its URI or its acronym/title, e.g. a lookup by "CC-BY 4.0" returns License.CC_BY_4_0.
 * </br>
 * The dictionary file must enforce the <a href="http://www.gbif.org/terms/licences">GBIF Licensing Policy</a>.
 * Non-CC licenses that GBIF considers equal to one of its three supported CC licenses (CC0 1.0, CC-BY 4.0 and CC-BY-NC
 * 4.0) can be added to this file.
 * </br>
 * Note a lookup by license acronym/title without a version number defaults to the latest version of that license,
 * e.g. a lookup by "CC-BY" returns License.CC_BY_4_0.
 */
public class LicenseParser extends EnumParser<License> {

  private static final String COMMENT_MARKER = "#";
  private static final String LICENSE_FILEPATH = "/dictionaries/parse/license.tsv";
  //allows us to remove the protocol part for http:// and https://
  private static final Pattern REMOVE_HTTP_PATTERN = Pattern.compile("^https?:\\/\\/", Pattern.CASE_INSENSITIVE);
  private static LicenseParser singletonObject = null;

  private LicenseParser() {
    super(License.class, true);
    // also make sure we have all enum values and their parameters title and url mapped
    for (License l : License.values()) {
      add(l.name(), l);
      add(l.getLicenseTitle(), l);
      add(l.getLicenseUrl(), l);
    }
    // use dict file last
    init(LicenseParser.class.getResourceAsStream(LICENSE_FILEPATH), COMMENT_MARKER);
  }

  @Override
  protected String normalize(String value) {
    if(value == null){
      return null;
    }
    return super.normalize(REMOVE_HTTP_PATTERN.matcher(value).replaceAll(""));
  }

  public static LicenseParser getInstance() {
    synchronized (ContinentParser.class) {
      if (singletonObject == null) {
        singletonObject = new LicenseParser();
      }
    }
    return singletonObject;
  }

  /**
   * Parse license supplied in two parts: URI and title. First parse URI. Only if no URI supplied, parse title.
   * supplied.
   *
   * @param uri   optional license URI
   * @param title optional license title
   *
   * @return License corresponding to license URI or title, License.UNSPECIFIED if both URI and title not supplied,
   * otherwise defaults to License.UNSUPPORTED
   */
  public License parseUriThenTitle(@Nullable URI uri, @Nullable String title) {
    if (uri == null && StringUtils.isEmpty(title)) {
      return License.UNSPECIFIED;
    }

    if (uri != null) {
      ParseResult<License> result = singletonObject.parse(uri.toString());
      if (result.isSuccessful()) {
        return result.getPayload();
      }
    }

    if (StringUtils.isNotEmpty(title)) {
      ParseResult<License> result = singletonObject.parse(title);
      if (result.isSuccessful()) {
        return result.getPayload();
      }
    }

    return License.UNSUPPORTED;
  }
}
