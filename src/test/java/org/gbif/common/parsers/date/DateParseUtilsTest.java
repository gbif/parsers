package org.gbif.common.parsers.date;

import org.apache.commons.lang3.time.DateUtils;
import org.gbif.common.parsers.core.ParseResult;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests for date parsing
 *
 * @author tim
 */
public class DateParseUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateParseUtilsTest.class);


    @Test
    public void testParseString() {
        // expected dates all in dd/MM/yyyy
        assertEquivalent("01/01/2010", DateParseUtils.parse("01/01/2010"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/01/2010", DateParseUtils.parse("20100101"), ParseResult.CONFIDENCE.PROBABLE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("21/12/1978"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("21/12/78"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("10/12/1978", DateParseUtils.parse("10/12/78"), ParseResult.CONFIDENCE.POSSIBLE);
        assertEquivalent("12/12/1978", DateParseUtils.parse("12/12/78"), ParseResult.CONFIDENCE.DEFINITE);
// TODO: really needed?
//    assertEquivalent("01/01/2010", DateParseUtils.parse("00/00/2010"), ParseResult.CONFIDENCE.POSSIBLE);
//    assertEquivalent("01/01/1804", DateParseUtils.parse("1804-00-00"), ParseResult.CONFIDENCE.POSSIBLE);
//    assertEquivalent("01/09/1807", DateParseUtils.parse("1807-09-00"), ParseResult.CONFIDENCE.POSSIBLE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("211278"), ParseResult.CONFIDENCE.PROBABLE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("122178"), ParseResult.CONFIDENCE.PROBABLE);
        assertEquivalent("01/02/1978", DateParseUtils.parse("010278"), ParseResult.CONFIDENCE.POSSIBLE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("21121978"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/07/1891", DateParseUtils.parse("1891-07"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("11/12/2004", DateParseUtils.parse("11/12/04"), ParseResult.CONFIDENCE.POSSIBLE);

        assertEquivalent("31/01/1973", DateParseUtils.parse("31.01.1973"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("31/01/1973", DateParseUtils.parse("31.1.1973"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("31/01/1973", DateParseUtils.parse("31.01.73"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("31/01/1973", DateParseUtils.parse("31.1.73"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("12/01/1973", DateParseUtils.parse("1973-01-12"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("07/01/1973", DateParseUtils.parse("7/1/73"), ParseResult.CONFIDENCE.POSSIBLE);
        assertEquivalent("01/07/1973", DateParseUtils.parse("1.7.73"), ParseResult.CONFIDENCE.POSSIBLE);
        assertEquivalent("21/07/1973", DateParseUtils.parse("7/21/73"), ParseResult.CONFIDENCE.DEFINITE);

        assertEquivalent("01/01/1973", DateParseUtils.parse("1973"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/01/1973", DateParseUtils.parse("73"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/05/1973", DateParseUtils.parse("1973-05"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/05/1973", DateParseUtils.parse("1973-5"), ParseResult.CONFIDENCE.DEFINITE);

        assertEquivalent("21/12/1978", DateParseUtils.parse("1978-12-21T00:00:00"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("1978-12-21T00:00"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("1978-12-21T00"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("1978-12-21T000000"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("21/12/1978", DateParseUtils.parse("1978-12-21T0000"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquals(ParseResult.STATUS.FAIL, DateParseUtils.parse("1978-13-32T00:00:00").getStatus());

        assertNotNull(DateParseUtils.parse("0000-00-00 00:00:00"));
        assertEquals(ParseResult.STATUS.FAIL, DateParseUtils.parse("0000-00-00 00:00:00").getStatus());

    }

    @Test
    public void testParseStringString() {
        assertEquivalent("01/01/2010", DateParseUtils.parse("01/01/2010", "dd/MM/yyyy"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/01/2010", DateParseUtils.parse("1/1/2010", "dd/MM/yyyy"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/11/2010", DateParseUtils.parse("1/11/2010", "dd/MM/yyyy"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/11/2010", DateParseUtils.parse("2010111", "yyyyMMdd"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/11/2010", DateParseUtils.parse("20101101", "yyyyMMdd"), ParseResult.CONFIDENCE.DEFINITE);
    }

    @Test
    public void testParseStringStringString() {
        assertEquivalent("21/12/1978", DateParseUtils.parse("1978", "12", "21"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/01/1978", DateParseUtils.parse("1978", "01", "01"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/11/1978", DateParseUtils.parse("1978", "11", "01"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquivalent("01/01/0001", DateParseUtils.parse("1", "1", "1"), ParseResult.CONFIDENCE.DEFINITE);
        assertEquals(ParseResult.STATUS.FAIL, DateParseUtils.parse("-1", "1", "1").getStatus());
        assertEquals(ParseResult.STATUS.FAIL, DateParseUtils.parse(null, null, null).getStatus());
    }

    @Test
    public void testAtomizeString() {
        String input = "21/12/2010";
        ParseResult<YearMonthDay> result = DateParseUtils.atomize(input);
        LOGGER.info("{} atomized to {}", input, result);
        assertEquals(ParseResult.STATUS.SUCCESS, result.getStatus());
        assertEquals(ParseResult.CONFIDENCE.DEFINITE, result.getConfidence());
        assertNotNull(result.getPayload());
        assertEquals(new YearMonthDay("2010", "12", "21"), result.getPayload());

        input = "16/07/99";
        result = DateParseUtils.atomize(input);
        LOGGER.info("{} atomized to {}", input, result);
        assertEquals(ParseResult.STATUS.SUCCESS, result.getStatus());
        assertEquals(ParseResult.CONFIDENCE.DEFINITE, result.getConfidence());
        assertNotNull(result.getPayload());
        assertEquals(new YearMonthDay("1999", "7", "16"), result.getPayload());
    }

    @Test
    public void testAtomizeDate() {
        try {
            Date input = DateUtils.parseDateStrictly("21/12/2010", new String[]{"dd/MM/yyyy"});
            YearMonthDay result = DateParseUtils.atomize(input);
            LOGGER.info("{} atomized to {}", input, result);
            assertEquals(new YearMonthDay("2010", "12", "21"), result);

            input = DateUtils.parseDateStrictly("01/11/2010", new String[]{"dd/MM/yyyy"});
            result = DateParseUtils.atomize(input);
            LOGGER.info("{} atomized to {}", input, result);
            assertEquals(new YearMonthDay("2010", "11", "1"), result);

        } catch (ParseException e) {
            fail("Test is in error.  Input dates are not in correct format");
        }
    }

    @Test
    public void testAtomUse() {
        assertFalse(DateParseUtils.isValidUse(null, DateParseUtils.DATE_FIELD.DAY));
        assertFalse(DateParseUtils.isValidUse(" ", DateParseUtils.DATE_FIELD.DAY));
        assertFalse(DateParseUtils.isValidUse("-", DateParseUtils.DATE_FIELD.DAY));

        assertTrue(DateParseUtils.isValidUse("1", DateParseUtils.DATE_FIELD.DAY));
        assertTrue(DateParseUtils.isValidUse("31", DateParseUtils.DATE_FIELD.DAY));
        assertFalse(DateParseUtils.isValidUse("0", DateParseUtils.DATE_FIELD.DAY));
        assertFalse(DateParseUtils.isValidUse("32", DateParseUtils.DATE_FIELD.DAY));
        assertTrue(DateParseUtils.isValidUse("1", DateParseUtils.DATE_FIELD.MONTH));
        assertTrue(DateParseUtils.isValidUse("12", DateParseUtils.DATE_FIELD.MONTH));
        assertFalse(DateParseUtils.isValidUse("0", DateParseUtils.DATE_FIELD.MONTH));
        assertFalse(DateParseUtils.isValidUse("13", DateParseUtils.DATE_FIELD.MONTH));
        assertTrue(DateParseUtils.isValidUse("1", DateParseUtils.DATE_FIELD.YEAR));
        assertTrue(DateParseUtils.isValidUse("2010", DateParseUtils.DATE_FIELD.YEAR));
        // would be pretty remarkable if this project is still in use in 20 years
        assertFalse(DateParseUtils.isValidUse("2020", DateParseUtils.DATE_FIELD.YEAR));
        assertFalse(DateParseUtils.isValidUse("-1", DateParseUtils.DATE_FIELD.YEAR));
        assertFalse(DateParseUtils.isValidUse("0", DateParseUtils.DATE_FIELD.YEAR));
    }

    @Test
    public void testNormalizeMonth() {
        assertEquals("01", DateParseUtils.normalizeMonth("January"));
        assertEquals("01", DateParseUtils.normalizeMonth("JANUARY"));
        assertEquals("01", DateParseUtils.normalizeMonth("Jan"));
        assertEquals("01", DateParseUtils.normalizeMonth("JAN."));
        assertEquals("12", DateParseUtils.normalizeMonth("DEC"));
        assertEquals("04", DateParseUtils.normalizeMonth("April"));
        assertEquals("09", DateParseUtils.normalizeMonth("Sept"));
        assertEquals("06", DateParseUtils.normalizeMonth("June"));
        assertEquals("06", DateParseUtils.normalizeMonth("Jun"));
        assertEquals("06", DateParseUtils.normalizeMonth("JUNE"));
    }

    @Test
    public void testNormalizeStringStringString() {
        assertEquals(new YearMonthDay("1978", "12", "21"), DateParseUtils.normalize("1978", "DEC", "21"));
        assertEquals(new YearMonthDay("1850", null, null), DateParseUtils.normalize("1850", "\\N", "\\N"));
        assertEquals(new YearMonthDay("1800", "06", null), DateParseUtils.normalize("1800", "June", "\\N"));
        // test that the year inference works ok
        assertEquals(new YearMonthDay("1974", "03", "12"), DateParseUtils.normalize("12/3/74", null, null));
        assertEquals(new YearMonthDay("2004", "12", "11"), DateParseUtils.normalize("11/12/04", null, null));
        // test that 2 digit years are handled sensibly
        assertEquals(new YearMonthDay("1997", "08", "03"), DateParseUtils.normalize("97", "8", "3"));
        // when we see an erroneous date, try to make most sense out of it
        assertEquals(new YearMonthDay("1929", "07", "01"), DateParseUtils.normalize("1929-06-31", null, null));

        assertEquals(new YearMonthDay("1965", "12", "02"), DateParseUtils.normalize("1965", "12", "02"));
        assertEquals(new YearMonthDay("1992", null, null), DateParseUtils.normalize("1992", "00", "00"));
        assertEquals(new YearMonthDay("1999", "02", "01"), DateParseUtils.normalize("1999-02-01", null, null));

        assertEquals(new YearMonthDay(null, "08", "26"), DateParseUtils.normalize("32767-08-26", null, null));

    }

    @Test
    public void testNormalizeFloat() {
        assertEquals("1978", DateParseUtils.normalizeFloat("1978"));
        assertEquals("1978", DateParseUtils.normalizeFloat("1978.00"));
        assertEquals("1978.1", DateParseUtils.normalizeFloat("1978.1"));
        assertEquals("Nonsense", DateParseUtils.normalizeFloat("Nonsense"));
        assertEquals("0197", DateParseUtils.normalizeFloat("0197"));
        assertEquals("197", DateParseUtils.normalizeFloat("197.0"));
    }

    @Test
    public void testInferCentury() {
        assertEquals("2000", DateParseUtils.inferCentury("00"));
        assertEquals("2001", DateParseUtils.inferCentury("01"));
        assertEquals("2010", DateParseUtils.inferCentury("10"));
        assertEquals("1920", DateParseUtils.inferCentury("20"));
        assertEquals("1980", DateParseUtils.inferCentury("80"));
        assertEquals("1997", DateParseUtils.inferCentury("97"));
    }

    /**
     * One test commented out because behavior is broken at the moment. This now relies on the user calling normalize
     * first otherwise this'll fail. We should refactor the DateParseUtils.
     */
    @Test
    public void testIssue42() {
        assertEquals(new YearMonthDay(null, null, null), DateParseUtils.normalize("2007", "02", "29"));
        //assertEquals(STATUS.FAIL, DateParseUtils.parse("2007", "02", "29").getIssues());
    }


    @Test
    public void testBadDates() {
        assertFalse(DateParseUtils.parse("Unknown date").isSuccessful());
        assertFalse(DateParseUtils.parse("12545,12").isSuccessful());
        assertFalse(DateParseUtils.parse(";:.,").isSuccessful());
        assertFalse(DateParseUtils.parse("//").isSuccessful());
        assertFalse(DateParseUtils.parse(":").isSuccessful());
    }

    private void assertEquivalent(String expected, ParseResult<Date> found, ParseResult.CONFIDENCE c) {
        if (ParseResult.STATUS.ERROR == found.getStatus()) {
            LOGGER.warn("Unexpected error found in parsing", found.getError());
        }
        assertEquals(ParseResult.STATUS.SUCCESS, found.getStatus());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        LOGGER.info("Expected [{}]: {}", expected, sdf.format(found.getPayload()));
        assertEquals(c, found.getConfidence());
        assertNotNull(found.getPayload());
        try {
            assertEquals(DateUtils.parseDateStrictly(expected, new String[]{"dd/MM/yyyy"}), found.getPayload());
        } catch (ParseException e) {
            fail("Error in the test class.  Expected date is not in the correct format of dd/MM/yyyy:" + expected);
        }
    }
}
