package org.gbif.common.parsers;

import org.gbif.api.vocabulary.OccurrenceStatus;

import org.junit.Test;

public class OccurrenceStatusParserTest extends ParserTestBase<OccurrenceStatus> {

  public OccurrenceStatusParserTest() {
    super(OccurrenceStatusParser.getInstance());
  }

  @Test
  public void testParseAllEnumValues() {
    for (OccurrenceStatus c : OccurrenceStatus.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testParsing() {
    // IUCN Red List statuses seem inappropriate to accept.  They are likely to be saying the taxon is rare, rather
    // than whether the occurrence was present or absent.
    assertParseFailure("EX");
    assertParseFailure("EW");
    assertParseFailure("CR");
    assertParseFailure("EN");
    assertParseFailure("VU");
    assertParseFailure("NT");
    assertParseFailure("LC");
    assertParseFailure("DD");
    assertParseFailure("NE");
    assertParseFailure("LR/cd");
    assertParseFailure("LR");
    assertParseFailure("cd");
    assertParseFailure("LR/nt");
    assertParseFailure("LR/lc");
    assertParseFailure("PE");

    // Top 100 verbatim occurrence status values seen on 2020-05-06.
    // (Duplicates due to different case and punctuation removed.)
    assertParseFailure("1");
    assertParseFailure("Damaged");
    assertParseFailure("Defoliated");
    assertParseFailure("doubtful");
    assertParseFailure("Dudoso");
    assertParseFailure("E");
    assertParseFailure("incomplet");
    assertParseFailure("NA");
    assertParseFailure("Ne Sait Pas");
    assertParseFailure("Q");
    assertParseFailure("Sanitation fellings");
    assertParseFailure("unknown");
    assertParseSuccess(OccurrenceStatus.ABSENT, "Absent");
    assertParseSuccess(OccurrenceStatus.ABSENT, "Ausente");
    assertParseSuccess(OccurrenceStatus.ABSENT, "Non observé");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Песня, голос");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Визуально");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Летящие");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Abundant.");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Abundant (10% <= p < 20%)");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Abundant 20-99");
    assertParseSuccess(OccurrenceStatus.PRESENT, "abundante");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Average Cover: 1-5% Maximum Cover: 1-5%");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Average Cover: 26-50% Maximum Cover: 26-50%");
    assertParseSuccess(OccurrenceStatus.PRESENT, "collected");
    assertParseSuccess(OccurrenceStatus.PRESENT, "common");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Common 5-19");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Common (5% <= p < 10%)");
    assertParseSuccess(OccurrenceStatus.PRESENT, "común");
    assertParseSuccess(OccurrenceStatus.PRESENT, "complet");
    assertParseSuccess(OccurrenceStatus.PRESENT, "confirmed breeding");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Confirmed Present");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Dominant (20% <= p)");
    assertParseSuccess(OccurrenceStatus.PRESENT, "established");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Frequent");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Infested");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Irregular");
    assertParseSuccess(OccurrenceStatus.PRESENT, "locally established");
    assertParseSuccess(OccurrenceStatus.PRESENT, "muito abundante");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Muy común");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Observed in Breeding Season");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Occasional.");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Occasional");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Outbreak");
    assertParseSuccess(OccurrenceStatus.PRESENT, "P");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Poco común");
    assertParseSuccess(OccurrenceStatus.PRESENT, "possible breeding");
    assertParseSuccess(OccurrenceStatus.PRESENT, "possibly breeding");
    assertParseSuccess(OccurrenceStatus.PRESENT, "presence");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Present");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Present (1% <= p < 5%)");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Presente");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Presente en el area");
    assertParseSuccess(OccurrenceStatus.PRESENT, "probable breeding");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Probably Breeding");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Présent");
    assertParseSuccess(OccurrenceStatus.PRESENT, "rare");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Rare 1-4");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Rare (p < 1%)");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Raro");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Reported");
    assertParseSuccess(OccurrenceStatus.PRESENT, "stocked");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Treated");
    assertParseSuccess(OccurrenceStatus.PRESENT, "unclear breeding certaint");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Uncommon");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Very abundant 100-499");
    assertParseSuccess(OccurrenceStatus.PRESENT, "Very very abundant > 500");
  }
}
