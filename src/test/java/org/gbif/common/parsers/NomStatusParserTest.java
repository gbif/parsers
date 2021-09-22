package org.gbif.common.parsers;

import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;
import org.gbif.api.vocabulary.NomenclaturalStatus;
import org.gbif.common.parsers.core.ParseResult;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;

/**
 * Parses a single nomenclatural status out of a given string.
 * TODO: Improve parser and return a status set instead, see nomStatus.txt file with comma separated values
 */
public class NomStatusParserTest extends ParserTestBase<NomenclaturalStatus> {

    public NomStatusParserTest() {
        super(NomStatusParser.getInstance());
    }

    /**
     * This ensures that ALL enum values are at least parsable by the name they
     * are created with.
     */
    @Test
    public void testCompleteness() {
        for (NomenclaturalStatus t : NomenclaturalStatus.values()) {
            assertParseSuccess(t, t.name());
            assertParseSuccess(t, t.name().toLowerCase());
            assertParseSuccess(t, t.name().replace("_", "").toLowerCase());
            if (StringUtils.isNotEmpty(t.getLatinLabel())) {
                assertParseSuccess(t, t.getLatinLabel());
            }
            if (StringUtils.isNotEmpty(t.getAbbreviatedLabel())) {
                assertParseSuccess(t, t.getAbbreviatedLabel());
            }
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
        assertParseSuccess(NomenclaturalStatus.NUDUM, "nom nud");
        assertParseSuccess(NomenclaturalStatus.ILLEGITIMATE, "later HOmonym");
        assertParseSuccess(NomenclaturalStatus.NEW_COMBINATION, ParseResult.CONFIDENCE.PROBABLE, "comb nov , rejected name, homotypic syn\tcomb nov , rejected name, homotypic syn");
        assertParseSuccess(NomenclaturalStatus.CONSERVED, "nom cons , see art 14");
        assertParseSuccess(NomenclaturalStatus.ILLEGITIMATE, ParseResult.CONFIDENCE.PROBABLE, "nom illeg ; (1775), non mill (1768)");
        assertParseSuccess(NomenclaturalStatus.ILLEGITIMATE, "nom illegit , arts 53 1, 53 3");
        assertParseSuccess(NomenclaturalStatus.INVALID, ParseResult.CONFIDENCE.PROBABLE, "nom inval , art 32 1(b), see art 18 3");
        assertParseSuccess(NomenclaturalStatus.NEW_SPECIES, "sp nov , heterotypic syn");
    }

    @Test
    public void testFileCoverage() throws IOException {
        // parses all values in our test file (generated from real occurrence data) and verifies we never get worse at parsing
        Resources.readLines(Resources.getResource("parse/nom_status.txt"), Charset.forName("UTF8"), this);
        BatchParseResult result = getResult();
        System.out.println(String.format("%s out of %s lines failed to parse", result.failed, result.total));
        assertTrue(result.failed <= 166); // out of 747
    }
}
