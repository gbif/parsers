package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.utils.NumberParser;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for assisting in the parsing of latitude and longitude strings into Decimals.
 */
public class CoordinateParseUtils {
  private final static String DMS = "\\s*(\\d{1,3})\\s*[°d ]"
                                  + "\\s*([0-6]?\\d)\\s*['m ]"
                                  + "\\s*(?:"
                                    + "([0-6]?\\d(?:[,.]\\d+)?)"
                                    + "\\s*(?:\"|''|s)?"
                                  + ")?\\s*";
  private final static Pattern DMS_SINGLE = Pattern.compile("^" + DMS + "$", Pattern.CASE_INSENSITIVE);
  private final static Pattern DMS_COORD = Pattern.compile("^" + DMS + "([NSEOW])" + "[ ,;/]?" + DMS + "([NSEOW])$", Pattern.CASE_INSENSITIVE);
  private final static String POSITIVE = "NEO";
  private CoordinateParseUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  private static final Logger LOG = LoggerFactory.getLogger(CoordinateParseUtils.class);

  /**
   * This parses string representations of latitude and longitude values. It tries its best to interpret the values and
   * indicates any problems in its result as {@link org.gbif.api.vocabulary.OccurrenceIssue}.
   * When the {@link ParseResult.STATUS} is FAIL the payload will be null and one or more issues should be set
   * in {@link ParseResult#getIssues()}.
   *
   * Coordinate precision will be 5 decimals at most, any more precise values will be rounded.
   *
   * Supported standard formats are the following, with dots or optionally a comma as the decimal marker:
   * <ul>
   *   <li>43.63871944444445</li>
   *   <li>N43°38'19.39"</li>
   *   <li>43°38'19.39"N</li>
   *   <li>43d 38m 19.39s N</li>
   *   <li>43 38 19.39</li>
   * </ul>
   *
   * @param latitude  The decimal latitude
   * @param longitude The decimal longitude
   *
   * @return The parse result
   */
  public static ParseResult<LatLng> parseLatLng(final String latitude, final String longitude) {
    return new Parsable<LatLng>() {
      @Override
      public ParseResult<LatLng> parse(String v) {
        if (Strings.isNullOrEmpty(latitude) || Strings.isNullOrEmpty(longitude)) {
          return ParseResult.fail();
        }
        Double lat = NumberParser.parseDouble(latitude);
        Double lng = NumberParser.parseDouble(longitude);

        if (lat == null || lng == null) {
          // try degree minute seconds
          try {
            lat = parseDMS(latitude, true);
            lng = parseDMS(longitude, false);
          } catch (IllegalArgumentException e) {
            return ParseResult.fail(OccurrenceIssue.COORDINATE_INVALID);
          }
        }

        return validateAndRound(lat, lng);
      }
    }.parse(null);
  }

  private static boolean inRange(double lat, double lon) {
    if (Double.compare(lat, 90) <= 0 && Double.compare(lat, -90) >= 0 && Double.compare(lon, 180) <= 0 && Double.compare(lon, -180) >= 0) {
      return true;
    }
    return false;
  }

  private static boolean isLat(String direction) {
    if ("NS".contains(direction.toUpperCase())) {
      return true;
    }
    return false;
  }

  private static int coordSign(String direction) {
    return POSITIVE.contains(direction.toUpperCase()) ? 1 : -1;
  }

  // 02° 49' 52" N	131° 47' 03" E
  public static ParseResult<LatLng> parseVerbatimCoordinates(final String coordinates) {
    if (Strings.isNullOrEmpty(coordinates)) {
      return ParseResult.fail();
    }
    Matcher m = DMS_COORD.matcher(coordinates);
    if (m.find()) {
      final String dir1 = m.group(4);
      final String dir2 = m.group(8);
      // first parse coords regardless whether they are lat or lon
      double c1 = coordFromMatcher(m, 1,2,3, dir1);
      double c2 = coordFromMatcher(m, 5,6,7, dir2);
      // now see what order the coords are in:
      if (isLat(dir1) && !isLat(dir2)) {
        return validateAndRound(c1, c2);

      } else if (!isLat(dir1) && isLat(dir2)) {
        return validateAndRound(c2, c1);

      } else {
        return ParseResult.fail(OccurrenceIssue.COORDINATE_INVALID);
      }

    } else if(coordinates.length() > 4) {
      // try to split and then use lat/lon parsing
      for (final char delim : ",;/ ".toCharArray()) {
        int cnt = StringUtils.countMatches(coordinates, String.valueOf(delim));
        if (cnt == 1) {
          String[] latlon = StringUtils.split(coordinates, delim);
          return parseLatLng(latlon[0], latlon[1]);
        }
      }
    }
    return ParseResult.fail(OccurrenceIssue.COORDINATE_INVALID);
  }

  private static ParseResult<LatLng> validateAndRound(double lat, double lon) {
    // collecting issues for result
    Set<OccurrenceIssue> issues = EnumSet.noneOf(OccurrenceIssue.class);

    // round to 5 decimals
    final double latOrig = lat;
    final double lngOrig = lon;
    lat = roundTo5decimals(lat);
    lon = roundTo5decimals(lon);
    if (Double.compare(lat, latOrig) != 0 || Double.compare(lon, lngOrig) != 0) {
      issues.add(OccurrenceIssue.COORDINATE_ROUNDED);
    }

    // 0,0 is too suspicious
    if (Double.compare(lat, 0) == 0 && Double.compare(lon, 0) == 0) {
      issues.add(OccurrenceIssue.ZERO_COORDINATE);
      return ParseResult.success(ParseResult.CONFIDENCE.POSSIBLE, new LatLng(0, 0), issues);
    }

    // if everything falls in range
    if (inRange(lat, lon)) {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new LatLng(lat, lon), issues);
    }

    // if lat is out of range, but in range of the lng,
    // assume swapped coordinates.
    // note that should we desire to trust the following records, we would need to clear the flag for the records to
    // appear in
    // search results and maps etc. however, this is logic decision, that goes above the capabilities of this method
    if (Double.compare(lat, 90) > 0 || Double.compare(lat, -90) < 0) {
      // try and swap
      if (inRange(lon, lat)) {
        issues.add(OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);
        return ParseResult.fail(new LatLng(lat, lon), issues);
      }
    }

    // then something is out of range
    issues.add(OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    return ParseResult.fail(issues);

  }

  /**
   * Parses a single DMS coordinate
   * @param coord
   * @param lat
   * @return the converted decimal
   */
  @VisibleForTesting
  protected static double parseDMS(String coord, boolean lat) {
    final String DIRS = lat ? "NS" : "EOW";
    coord = coord.trim().toUpperCase();

    if (coord.length() > 3) {
      // preparse the direction and remove it from the string to avoid a very complex regex
      char dir = 'n';
      if (DIRS.contains(String.valueOf(coord.charAt(0)))) {
        dir = coord.charAt(0);
        coord = coord.substring(1);
      } else if (DIRS.contains(String.valueOf(coord.charAt(coord.length()-1)))) {
        dir = coord.charAt(coord.length()-1);
        coord = coord.substring(0, coord.length()-1);
      }
      // without the direction chuck it at the regex
      Matcher m = DMS_SINGLE.matcher(coord);
      if (m.find()) {
        return coordFromMatcher(m, 1,2,3, String.valueOf(dir));
      }
    }
    throw new IllegalArgumentException();
  }

  private static double coordFromMatcher(Matcher m, int idx1, int idx2, int idx3, String sign) {
    return coordSign(sign)
         * dmsToDecimal( NumberParser.parseDouble(m.group(idx1)), NumberParser.parseDouble(m.group(idx2)), NumberParser.parseDouble(m.group(idx3)) );
  }
  private static double dmsToDecimal(double degree, Double minutes, Double seconds) {
    minutes = minutes == null ? 0 : minutes;
    seconds = seconds == null ? 0 : seconds;
    return degree + (minutes / 60) + (seconds / 3600);
  }

  // round to 5 decimals (~1m precision) since no way we're getting anything legitimately more precise
  private static Double roundTo5decimals(Double x) {
    return x == null ? null : Math.round(x * Math.pow(10, 5)) / Math.pow(10, 5);
  }
}
