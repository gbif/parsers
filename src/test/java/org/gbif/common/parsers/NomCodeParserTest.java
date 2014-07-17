package org.gbif.common.parsers;

import org.gbif.api.vocabulary.NomenclaturalCode;
import org.junit.Test;

public class NomCodeParserTest extends ParserTestBase<NomenclaturalCode> {

    public NomCodeParserTest() {
        super(NomCodeParser.getInstance());
    }

    /**
     * This ensures that ALL enum values are at least parsable by the name they
     * are created with.
     */
    @Test
    public void testCompleteness() {
        for (NomenclaturalCode t : NomenclaturalCode.values()) {
            assertParseSuccess(t, t.name());
            assertParseSuccess(t, t.name().toLowerCase());
            assertParseSuccess(t, t.name().replace("_", "").toLowerCase());
            assertParseSuccess(t, t.getAcronym());
            assertParseSuccess(t, t.getTitle());
        }
    }

    @Test
    public void testFailures() {
        assertParseFailure(null);
        assertParseFailure("");
        assertParseFailure("Tim");
    }


    @Test
    public void testParse() {
        // run a few basic tests to check it bootstraps and appears to work
        assertParseSuccess(NomenclaturalCode.BOTANICAL, "ICBN");
        assertParseSuccess(NomenclaturalCode.BOTANICAL, "botany");
        assertParseSuccess(NomenclaturalCode.ZOOLOGICAL, "zoo");
    }

}