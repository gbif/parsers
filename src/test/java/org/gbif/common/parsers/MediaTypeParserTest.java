package org.gbif.common.parsers;

import org.gbif.api.vocabulary.MediaType;

import org.junit.jupiter.api.Test;

public class MediaTypeParserTest extends ParserTestBase<MediaType> {

  public MediaTypeParserTest() {
    super(MediaTypeParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (MediaType c : MediaType.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great britain");
    assertParseFailure("Padua");
    assertParseFailure("Southern Ocean");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(MediaType.Sound, "audio");
    assertParseSuccess(MediaType.MovingImage, "movie");
    assertParseSuccess(MediaType.StillImage, "image");
    assertParseSuccess(MediaType.StillImage, "http://purl.org/dc/dcmitype/StillImage");
  }
}
