package org.gbif.common.parsers;

import org.gbif.api.vocabulary.License;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LicenseParserTest extends ParserTestBase<License> {

  public LicenseParserTest() {
    super(LicenseParser.getInstance());
  }

  /**
   * Makes sure all License enum values and their parameters are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (License l : License.values()) {
      assertParseSuccess(l, l.name());
      if (l.getLicenseUrl() != null) {
        assertParseSuccess(l, l.getLicenseUrl());
      }
      if (l.getLicenseTitle() != null) {
        assertParseSuccess(l, l.getLicenseTitle());
      }
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Not licensed");
    assertParseFailure("CC");
  }

  @Test
  public void testParse() {
    assertParseSuccess(License.CC0_1_0, "CC0");
    assertParseSuccess(License.CC0_1_0, "CC0 1.0");
    assertParseSuccess(License.CC0_1_0, "cc-zero");
    assertParseSuccess(License.CC0_1_0, "CC-ZERO");
    assertParseSuccess(License.CC0_1_0, "CCZERO");
    assertParseSuccess(License.CC0_1_0, "http://creativecommons.org/publicdomain/zero/1.0");
    assertParseSuccess(License.CC0_1_0, "creativecommons.org/publicdomain/zero/1.0");
    assertParseSuccess(License.CC0_1_0, "http://creativecommons.org/publicdomain/zero/1.0/");
    assertParseSuccess(License.CC0_1_0, "http://creativecommons.org/publicdomain/zero/1.0/legalcode");
    assertParseSuccess(License.CC0_1_0, "http://www.opendatacommons.org/licenses/pddl/1.0");
    assertParseSuccess(License.CC0_1_0, "This work is licensed under a Creative Commons CCZero 1.0 License" +
                    " http://creativecommons.org/publicdomain/zero/1.0/legalcode");
    assertParseSuccess(License.CC0_1_0, "This work is licensed under a Creative Commons CCZero 1.0 License" +
            " http://creativecommons.org/publicdomain/zero/1.0/legalcode.");

    //TO BE REVIEWED
    assertParseSuccess(License.CC0_1_0, "public domain");
    assertParseSuccess(License.CC0_1_0, "Public Domain");


    assertParseSuccess(License.CC_BY_4_0, "CC-BY");
    assertParseSuccess(License.CC_BY_4_0, "CC-BY 4.0");
    assertParseSuccess(License.CC_BY_4_0, "CC BY 4.0");
    assertParseSuccess(License.CC_BY_4_0, "http://creativecommons.org/licenses/by/4.0");
    assertParseSuccess(License.CC_BY_4_0, "http://creativecommons.org/licenses/by/4.0/");
    assertParseSuccess(License.CC_BY_4_0, "http://creativecommons.org/licenses/by/4.0/legalcode");
    assertParseSuccess(License.CC_BY_4_0, "http://www.opendatacommons.org/licenses/by/1.0");

    assertParseSuccess(License.CC_BY_NC_4_0, "CC-BY-NC");
    assertParseSuccess(License.CC_BY_NC_4_0, "CC-BY-NC 4.0");
    assertParseSuccess(License.CC_BY_NC_4_0, "CC BY-NC 4.0");
    assertParseSuccess(License.CC_BY_NC_4_0, "http://creativecommons.org/licenses/by-nc/4.0");
    assertParseSuccess(License.CC_BY_NC_4_0, "http://creativecommons.org/licenses/by-nc/4.0/");
    assertParseSuccess(License.CC_BY_NC_4_0, "http://creativecommons.org/licenses/by-nc/4.0/legalcode");
    assertParseSuccess(License.CC_BY_NC_4_0, "https://creativecommons.org/licenses/by-nc/4.0");
    assertParseSuccess(License.CC_BY_NC_4_0, "https://creativecommons.org/licenses/by-nc/4.0/");
    assertParseSuccess(License.CC_BY_NC_4_0, "https://creativecommons.org/licenses/by-nc/4.0/legalcode");

    assertParseSuccess(License.UNSUPPORTED, "http://creativecommons.org/licenses/by/1.0/legalcode");
    assertParseSuccess(License.UNSUPPORTED, "http://creativecommons.org/licenses/by/2.0/legalcode");
    assertParseSuccess(License.UNSUPPORTED, "http://creativecommons.org/licenses/by/2.5/legalcode");
    assertParseSuccess(License.UNSUPPORTED, "http://creativecommons.org/licenses/by/3.0/legalcode");
    assertParseSuccess(License.UNSUPPORTED, "http://opendatacommons.org/licenses/odbl/1.0");
  }

  @Test
  public void testParseUriThenTitle() throws URISyntaxException {
    LicenseParser parser = LicenseParser.getInstance();
    assertEquals(License.UNSPECIFIED, parser.parseUriThenTitle(null, null));

    assertEquals(License.CC0_1_0, parser.parseUriThenTitle(null, "CC0"));
    assertEquals(License.CC0_1_0, parser.parseUriThenTitle(null, "CCZero"));
    assertEquals(License.CC0_1_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/publicdomain/zero/1.0"), null));
    assertEquals(License.CC0_1_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/publicdomain/zero/1.0/"), null));
    assertEquals(License.CC0_1_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/publicdomain/zero/1.0/legalcode"), null));

    assertEquals(License.CC_BY_4_0, parser.parseUriThenTitle(null, "CC-BY"));
    assertEquals(License.CC_BY_4_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by/4.0"), null));
    assertEquals(License.CC_BY_4_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by/4.0/"), null));
    assertEquals(License.CC_BY_4_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by/4.0/legalcode"), null));

    assertEquals(License.CC_BY_NC_4_0, parser.parseUriThenTitle(null, "CC-BY-NC"));
    assertEquals(License.CC_BY_NC_4_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by-nc/4.0"), null));
    assertEquals(License.CC_BY_NC_4_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by-nc/4.0/"), null));
    assertEquals(License.CC_BY_NC_4_0, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by-nc/4.0/legalcode"), null));

    assertEquals(License.UNSUPPORTED, parser.parseUriThenTitle(new URI("http://creativecommons.org/licenses/by/3.0/legalcode"), null));
    assertEquals(License.UNSUPPORTED, parser.parseUriThenTitle(new URI("http://opendatacommons.org/licenses/odbl/1.0"), null));
  }
}
