package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Month;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;

import static org.junit.Assert.assertEquals;

/**
 * Unit testing for ThreeTenNumericalDateParser.
 *
 */
public class ThreeTenNumericalDateParserTest {

  @Test
  public void testParseAsLocalDateTime(){
    ThreeTenNumericalDateParser parser = new ThreeTenNumericalDateParser();

    //d-m-y
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21-12-1978").getPayload()));
    assertEquals(LocalDate.of(1808, Month.DECEMBER, 21), LocalDate.from(parser.parse("21-12-1808").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21-12-78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21.12.78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21.12.1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21/12/1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21121978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21_12_78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21\\12\\78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21\\12\\1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21-12-78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21_12_1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("21/12/78").getPayload()));

    //y-m-d
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("1978-12-21").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 1), LocalDate.from(parser.parse("1978-12-01").getPayload()));
    //assertEquals(LocalDate.of(1978, Month.DECEMBER, 1), LocalDate.from(parser.parse("1978-12-1").getPayload()));

    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21 02:12:43").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 0, 0), LocalDateTime.from(parser.parse("1978-12-21T02").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 0), LocalDateTime.from(parser.parse("1978-12-21T02:12").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21T02:12:43").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 0), LocalDateTime.from(parser.parse("1978-12-21T0212").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21T021243").getPayload()));

    //Z means 0 offset
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21T02:12:43Z").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21T02:12:43+0100").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21T02:12:43+01:00").getPayload()));
    assertEquals(LocalDateTime.of(1978, Month.DECEMBER, 21, 2, 12, 43), LocalDateTime.from(parser.parse("1978-12-21T02:12:43").getPayload()));

    //month first :(
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12/21/1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12211978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12\\21\\1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12.21.1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12-21-1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12_21_1978").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12/21/1978").getPayload()));

    //month first with 2 digits years >_<
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("122178").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12/21/78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12\\21\\78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12.21.78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12-21-78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12_21_78").getPayload()));

    //month/year alone
    assertEquals(YearMonth.of(1978,12), YearMonth.from(parser.parse("1978-12").getPayload()));

    //year alone
    assertEquals(Year.of(1978), Year.from(parser.parse("1978").getPayload()));
    assertEquals(Year.of(1978), Year.from(parser.parse("78").getPayload()));
  }

  @Test
  public void testParseAsLocalDateByDateParts(){
    ThreeTenNumericalDateParser parser = new ThreeTenNumericalDateParser();
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("1978","12", "21").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 1), LocalDate.from(parser.parse("1978","12", "1").getPayload()));
    assertEquals(YearMonth.of(1978,12), YearMonth.from(parser.parse("1978","12", null).getPayload()));
    assertEquals(Year.of(1978), Year.from(parser.parse("1978","",null).getPayload()));
  }

  @Test
  public void testParseUndefinedLocalDateTime(){
    ThreeTenNumericalDateParser parser = new ThreeTenNumericalDateParser();
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("120578").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("1978","2", "31").getStatus());
  }

  @Test
  public void testBadDates(){
    ThreeTenNumericalDateParser parser = new ThreeTenNumericalDateParser();
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("fsfgr/12/78").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("fsfgr").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("//").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("0-0-0").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("%$-s2").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("as-fds-sdf").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("88/88/88").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse(" ").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse(null).getStatus());
  }

}
