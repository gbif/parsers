package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Kingdom;

import org.junit.Test;

public class KingdomParserTest extends ParserTestBase<Kingdom> {

    public KingdomParserTest() {
        super(KingdomParser.getInstance());
    }

    @Test
    public void testParseFail() {
        assertParseFailure("[West Indian Ocean]");
    }

    @Test
    public void testParseSuccess() {
        assertParseSuccess(Kingdom.ANIMALIA, "animal");
        assertParseSuccess(Kingdom.ANIMALIA, "metazoa");
    }

}
