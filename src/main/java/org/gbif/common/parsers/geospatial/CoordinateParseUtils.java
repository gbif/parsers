package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.NumberParser;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for assisting in the parsing of latitude and longitude strings into Decimals.
 */
public class CoordinateParseUtils {
  private final static String DMS = "\\s*(\\d{1,3})\\s*(?:°|d|º| |g|o)"  // The degrees
                                  + "\\s*([0-6]?\\d)\\s*(?:'|m| |´|’|′)" // The minutes
                                  + "\\s*(?:"                            // Non-capturing group
                                  + "([0-6]?\\d(?:[,.]\\d+)?)"           // Seconds and optional decimal
                                  + "\\s*(?:\"|''|s|´´|″)?"
                                  + ")?\\s*";
  private final static String DM = "\\s*(\\d{1,3})\\s*(?:°|d|º| |g|o)" // The degrees
                                 + "\\s*(?:"                           // Non-capturing group
                                 + "([0-6]?\\d(?:[,.]\\d+)?)"          // Minutes and optional decimal
                                 + "\\s*(?:'|m| |´|’|′)?"
                                 + ")?\\s*";
  private final static String D = "\\s*(\\d{1,3}(?:[,.]\\d+)?)\\s*(?:°|d|º| |g|o|)\\s*"; // The degrees and optional decimal
  private final static Pattern DMS_SINGLE = Pattern.compile("^" + DMS + "$", Pattern.CASE_INSENSITIVE);
  private final static Pattern DM_SINGLE = Pattern.compile("^" + DM + "$", Pattern.CASE_INSENSITIVE);
  private final static Pattern D_SINGLE = Pattern.compile("^" + D + "$", Pattern.CASE_INSENSITIVE);
  private final static Pattern DMS_COORD = Pattern.compile("^" + DMS + "([NSEOW])" + "[ ,;/]?" + DMS + "([NSEOW])$", Pattern.CASE_INSENSITIVE);
  //private final static Pattern DM_COORD = Pattern.compile("^" + DM + "([NSEOW])" + "[ ,;/]?" + DM + "([NSEOW])$", Pattern.CASE_INSENSITIVE);
  //private final static Pattern D_COORD = Pattern.compile("^" + D + "([NSEOW])" + "[ ,;/]?" + D + "([NSEOW])$", Pattern.CASE_INSENSITIVE);
  private final static String POSITIVE = "NEO";
  private CoordinateParseUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  private static final Logger LOG = LoggerFactory.getLogger(CoordinateParseUtils.class);

  /**
   * This parses string representations of latitude and longitude values. It tries its best to interpret the values and
   * indicates any problems in its result as {@link org.gbif.api.vocabulary.OccurrenceIssue}.
   * When the {@link ParseResult.STATUS} is FAIL the payload will be null and one or more issues should be set
   * in {@link org.gbif.common.parsers.core.OccurrenceParseResult#getIssues()}.
   *
   * Coordinate precision will be 6 decimals at most, any more precise values will be rounded.
   *
   * Supported standard formats are the following, with dots or optionally a comma as the decimal marker, and variations
   * on the units also accepted e.g. °, d, º, g, o.
   * <ul>
   *   <li>43.63871944444445</li>
   *   <li>N43°38'19.39"</li>
   *   <li>43°38'19.39"N</li>
   *   <li>43°38.3232'N</li>
   *   <li>43d 38m 19.39s N</li>
   *   <li>43 38 19.39</li>
   *   <li>433819N</li>
   * </ul>
   *
   * @param latitude  The decimal latitude
   * @param longitude The decimal longitude
   *
   * @return The parse result
   */
  public static OccurrenceParseResult<LatLng> parseLatLng(final String latitude, final String longitude) {
    if (StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) {
      return OccurrenceParseResult.fail();
    }
    Double lat = NumberParser.parseDouble(latitude);
    Double lng = NumberParser.parseDouble(longitude);
    if (lat == null || lng == null) {
      // try degree minute seconds
      try {
        lat = parseDMS(latitude, true);
        lng = parseDMS(longitude, false);
      } catch (IllegalArgumentException e) {
        return OccurrenceParseResult.fail(OccurrenceIssue.COORDINATE_INVALID);
      }
    }

    return validateAndRound(lat, lng);
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
  public static OccurrenceParseResult<LatLng> parseVerbatimCoordinates(final String coordinates) {
    if (StringUtils.isEmpty(coordinates)) {
      return OccurrenceParseResult.fail();
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
        return OccurrenceParseResult.fail(OccurrenceIssue.COORDINATE_INVALID);
      }

    } else if(coordinates.length() > 4) {
      // try to split and then use lat/lon parsing
      for (final char delim : ",;/ ".toCharArray()) {
        int cnt = StringUtils.countMatches(coordinates, String.valueOf(delim));
        if (cnt == 1) {
          String[] latlon = StringUtils.split(coordinates, delim);
          if (latlon.length == 2) {
            return parseLatLng(latlon[0], latlon[1]);
          }
        }
      }
    }
    return OccurrenceParseResult.fail(OccurrenceIssue.COORDINATE_INVALID);
  }

  private static OccurrenceParseResult<LatLng> validateAndRound(double lat, double lon) {
    // collecting issues for result
    Set<OccurrenceIssue> issues = EnumSet.noneOf(OccurrenceIssue.class);

    // round to 6 decimals
    final double latOrig = lat;
    final double lngOrig = lon;
    lat = roundTo6decimals(lat);
    lon = roundTo6decimals(lon);
    if (Double.compare(lat, latOrig) != 0 || Double.compare(lon, lngOrig) != 0) {
      issues.add(OccurrenceIssue.COORDINATE_ROUNDED);
    }

    // 0,0 is too suspicious
    if (Double.compare(lat, 0) == 0 && Double.compare(lon, 0) == 0) {
      issues.add(OccurrenceIssue.ZERO_COORDINATE);
      return OccurrenceParseResult.success(ParseResult.CONFIDENCE.POSSIBLE, new LatLng(0, 0), issues);
    }

    // if everything falls in range
    if (inRange(lat, lon)) {
      return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new LatLng(lat, lon), issues);
    }

    // if lat is out of range, but in range of the lng, assume swapped coordinates.
    // note that should we desire to trust the following records, we would need to clear the flag for the records to
    // appear in search results and maps etc. however, this is logic decision, that goes above the capabilities of this method
    if (Double.compare(lat, 90) > 0 || Double.compare(lat, -90) < 0) {
      // try and swap
      if (inRange(lon, lat)) {
        issues.add(OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);
        return OccurrenceParseResult.success(ParseResult.CONFIDENCE.PROBABLE, new LatLng(lon, lat), issues);
      }
    }

    // then something is out of range
    issues.add(OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    return OccurrenceParseResult.fail(issues);

  }

  /**
   * Parses a single DMS coordinate
   * @param coord
   * @param lat
   * @return the converted decimal up to 6 decimals accuracy
   */
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
      } else {
        m = DM_SINGLE.matcher(coord);
        if (m.find()) {
          return coordFromMatcher(m, 1, 2, String.valueOf(dir));
        } else {
          m = D_SINGLE.matcher(coord);
          if (m.find()) {
            return coordFromMatcher(m, 1, String.valueOf(dir));
          }
        }
      }
    }
    throw new IllegalArgumentException();
  }

  private static double coordFromMatcher(Matcher m, int idx1, int idx2, int idx3, String sign) {
    return roundTo6decimals(coordSign(sign) *
      dmsToDecimal( NumberParser.parseDouble(m.group(idx1)), NumberParser.parseDouble(m.group(idx2)), NumberParser.parseDouble(m.group(idx3)) ));
  }

  private static double coordFromMatcher(Matcher m, int idx1, int idx2, String sign) {
    return roundTo6decimals(coordSign(sign) *
      dmsToDecimal( NumberParser.parseDouble(m.group(idx1)), NumberParser.parseDouble(m.group(idx2)), 0.0));
  }

  private static double coordFromMatcher(Matcher m, int idx1, String sign) {
    return roundTo6decimals(coordSign(sign) *
      dmsToDecimal( NumberParser.parseDouble(m.group(idx1)), 0.0, 0.0));
  }

  private static double dmsToDecimal(double degree, Double minutes, Double seconds) {
    minutes = minutes == null ? 0 : minutes;
    seconds = seconds == null ? 0 : seconds;
    return degree + (minutes / 60) + (seconds / 3600);
  }

  // round to 6 decimals (~1m precision) since no way we're getting anything legitimately more precise
  private static Double roundTo6decimals(Double x) {
    return x == null ? null : Math.round(x * Math.pow(10, 6)) / Math.pow(10, 6);
  }
}
