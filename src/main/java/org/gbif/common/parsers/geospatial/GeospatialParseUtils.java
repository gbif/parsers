package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceValidationRule;
import org.gbif.common.parsers.Parsable;
import org.gbif.common.parsers.ParseResult;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for assisting in the parsing of latitude and longitude strings into Decimals, the handling of depth and
 * altitude ranges.
 */
public class GeospatialParseUtils {

  private GeospatialParseUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(GeospatialParseUtils.class);

  /**
   * Pattern for removing measurement denominations
   */
  private static final Pattern MEASURE_MARKER_PATTERN = Pattern.compile(".*[a-zA-Z].*");

  /**
   * Pattern to remove measurement markers (like "m")
   */
  private static final Pattern REMOVE_MEASURE_MARKER_PATTERN = Pattern.compile("[a-zA-Z\" \"\"]");

  /**
   * Pattern for recognising measurements in feet
   */
  private static final Pattern FEET_MARKER_PATTERN = Pattern.compile(".*ft.*|.*FT.*|.*'.*");

  /**
   * Pattern for recognising measurements in inches
   */
  private static final Pattern INCHES_MARKER_PATTERN = Pattern.compile(".*in.*|.*\".*");

  /**
   * Pattern for recognising a range value
   */
  private static final Pattern SEP_MARKER_PATTERN = Pattern.compile("\\d-.*");

  /**
   * The min value that will be synced with OR
   */
  private static final int MIN_RECORD_DEPTH_IN_CENTIMETRES = 0;

  /**
   * The max value that will be synced with OR - tied to the data type for occurrence_record.depth_centimetres
   */
  private static final int MAX_RECORD_DEPTH_IN_CENTIMETRES = 16777215;

  /**
   * The highest depth value recognised as valid
   */
  private static final int OUT_OF_RANGE_DEPTH = 10000;

  /**
   * The lowest altitude value recognised as valid
   */
  private static final int OUT_OF_RANGE_MIN_ALTITUDE = -100;

  /**
   * The highest altitude value recognised as valid
   */
  private static final int OUT_OF_RANGE_MAX_ALTITUDE = 10000;

  /**
   * The max value that will be synced with OR - tied to the data type for occurrence_record.altitude_metres
   */
  private static final int MAX_TO_RECORD_ALTITUDE_IN_METRES = 32767;

  /**
   * The lowest value that will be synced with OR
   */
  private static final int MIN_TO_RECORD_ALTITUDE_IN_METRES = -32768;

  /**
   * Constant factor to convert from feet to metres.
   */
  private static final float FEET_TO_METRES = 0.3048f;

  /**
   * Constant factor to convert from inches to metres.
   */
  private static final float INCHES_TO_METRES = 0.0254f;

  /**
   * This parses string representations of latitude and longitude values. It tries its best to interpret the values and
   * indicates any problems in its result as {@link org.gbif.api.vocabulary.OccurrenceValidationRule}.
   * When the {@link ParseResult.STATUS} is either SUCCESS or FAIL the payload will always be non-null and the
   * {@link LatLngIssue#getIssue()} method should return any issues there were. In case the issue is ERROR the payload
   * will usually be {@code null}.
   *
   * @param latitude  The verbatim latitude
   * @param longitude The verbatim longitude
   *
   * @return The parse result
   */
  public static ParseResult<LatLngIssue> parseLatLng(final String latitude, final String longitude) {
    return new Parsable<Void, LatLngIssue>() {
      @Override
      public ParseResult<LatLngIssue> parse(Void v) {
        Double lat;
        Double lng;
        try {
          lat = Double.parseDouble(latitude);
          lng = Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
          return ParseResult.error(e);
        }

        // 0,0 is too suspicious
        if (Double.compare(lat, 0) == 0 && Double.compare(lng, 0) == 0) {
          return ParseResult
            .success(ParseResult.CONFIDENCE.POSSIBLE, new LatLngIssue(0, 0, OccurrenceValidationRule.ZERO_COORDINATE));
        }

        // if everything falls in range
        if (Double.compare(lat, 90) <= 0 && Double.compare(lat, -90) >= 0 && Double.compare(lng, 180) <= 0
            && Double.compare(lng, -180) >= 0) {
          return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new LatLngIssue(lat, lng));
        }

        // if lat is out of range, but in range of the lng,
        // assume swapped coordinates.
        // note that should we desire to trust the following records, we would need to clear the flag for the records to
        // appear in
        // search results and maps etc. however, this is logic decision, that goes above the capabilities of this method
        if (Double.compare(lat, 90) > 0 || Double.compare(lat, -90) < 0) {

          // try and swap
          if (Double.compare(lng, 90) <= 0 && Double.compare(lng, -90) >= 0 && Double.compare(lat, 180) <= 0
              && Double.compare(lat, -180) >= 0) {
            return ParseResult.fail(new LatLngIssue(lat, lng, OccurrenceValidationRule.PRESUMED_SWAPPED_COORDINATE));
          }
        }

        // then something is out of range
        return ParseResult.fail(new LatLngIssue(OccurrenceValidationRule.COORDINATES_OUT_OF_RANGE));
      }
    }.parse(null);
  }

  /**
   * Attempts to parse the depth information provided, and return the depth in centimetres. This unit (centimetres) is
   * defined by the previous implementation of the portal and this implementation is a port of the existing code
   *
   * @param minimum           verbatim minimum depth
   * @param maximum           verbatim maximum depth
   * @param precisionAsString verbatim precision
   *
   * @return The parse result
   */
  public static ParseResult<LongPrecisionIssue> parseDepth(final String minimum, final String maximum,
    final String precisionAsString) {
    return new Parsable<Void, LongPrecisionIssue>() {
      @Override
      public ParseResult<LongPrecisionIssue> parse(Void v) {

        Set<OccurrenceValidationRule> issues = Sets.newHashSet();

        ParseStringToDouble source = new ParseStringToDouble(precisionAsString);
        getDepthMeasurement(source);
        Double precision = source.parsed;
        issues.addAll(source.issues);

        source = new ParseStringToDouble(minimum);
        getDepthMeasurement(source);
        Double min = source.parsed;
        issues.addAll(source.issues);

        source = new ParseStringToDouble(maximum);
        getDepthMeasurement(source);
        Double max = source.parsed;
        issues.addAll(source.issues);

        LOGGER.debug("minVerbatim[{}], minParsed[{}], maxVerbatim[{}], maxParsed[{}], precisionVerbatim[{}], "
                     + "precisionParsed[{}]", new Object[] {minimum, min, maximum, max, precisionAsString, precision});

        if (min == null && max == null) {
          // both are null
          return ParseResult.fail();
        }

        min = min == null ? max : min;
        max = max == null ? min : max;

        if (precision != null) {
          min -= precision;
          max += precision;
        }

        Double depth = max + min == 0 ? 0.0d : (max + min) / 2;

        // record the number of record with depth 0
        if (min > max) {
          issues.add(OccurrenceValidationRule.DEPTH_MIN_MAX_SWAPPED);
        }

        // record the number of record with depth 0
        if (depth > OUT_OF_RANGE_DEPTH) {
          issues.add(OccurrenceValidationRule.DEPTH_OUT_OF_RANGE);
        }

        long depthInCentimetres = Math.round(depth * 100);
        if (depthInCentimetres <= MAX_RECORD_DEPTH_IN_CENTIMETRES
            && depthInCentimetres >= MIN_RECORD_DEPTH_IN_CENTIMETRES) {
          if (precision != null) {
            return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE,
              new LongPrecisionIssue(Math.round(depth * 100), Math.round(precision * 100), issues));
          } else {
            return ParseResult
              .success(ParseResult.CONFIDENCE.DEFINITE, new LongPrecisionIssue(Math.round(depth * 100), null, issues));
          }
        }

        // we get no value during parsing
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new LongPrecisionIssue(null, null, issues));
      }

    }.parse(null);
  }

  /**
   * Attempts to parse the depth information provided, and return the altitude in meters. This unit (metres) is defined
   * by the previous implementation of the portal and this implementation is a port of the existing code
   *
   * @param minimum           verbatim minimum altitude
   * @param maximum           verbatim maximum altitude
   * @param precisionAsString verbatim precision
   *
   * @return The parse result
   */
  public static ParseResult<LongPrecisionIssue> parseAltitude(final String minimum, final String maximum,
    final String precisionAsString) {
    return new Parsable<Void, LongPrecisionIssue>() {
      @Override
      public ParseResult<LongPrecisionIssue> parse(Void v) {

        Set<OccurrenceValidationRule> issues = Sets.newHashSet();

        ParseStringToDouble source = new ParseStringToDouble(minimum);
        getAltitudeMeasurement(source);
        Double min = source.parsed;
        issues.addAll(source.issues);

        source = new ParseStringToDouble(maximum);
        getAltitudeMeasurement(source);
        Double max = source.parsed;
        issues.addAll(source.issues);

        // parse the altitude precision
        Double precision = null;
        if (precisionAsString != null) {
          String altitudePrecision = removeMeasurementMarkers(precisionAsString);

          source = new ParseStringToDouble(altitudePrecision);
          getAltitudeMeasurement(source);
          precision = source.parsed;
          issues.addAll(source.issues);

          try {
            if (precision != null && min != null && max != null) {
              min -= precision;
              max += precision;
            }
          } catch (NumberFormatException e) {
            // TODO: Log invalid or unparsable altitude
          }
        }
        LOGGER.debug("minVerbatim[{}], minParsed[{}], maxVerbatim[{}], maxParsed[{}], precisionVerbatim[{}], "
                     + "precisionParsed[{}]", new Object[] {minimum, min, maximum, max, precisionAsString, precision});

        // determine the altitude
        Long altitude = null;
        if (max != null && min != null) {
          // avoid divide by 0
          if (max + min == 0) {
            altitude = 0L;

          } else if (max == 0 && min > 0) {
            // if max is supplied as 0 and min is above
            altitude = Math.round(min);
          } else {
            // average of the 2
            altitude = Math.round((max + min) / 2);
          }
        } else if (min != null) {
          // if only min altitude supplied, use this
          altitude = Math.round(min);

        } else if (max != null) {
          // if only max altitude supplied, use this
          altitude = Math.round(max);
        }

        // record the number of records with altitude 0
        if (altitude != null && altitude == 0) {
          // TODO: Log "number of records marker with altitude 0"
        }

        // record the number of records with altitude out of range
        if (altitude != null && (altitude > OUT_OF_RANGE_MAX_ALTITUDE || altitude < OUT_OF_RANGE_MIN_ALTITUDE)) {
          issues.add(OccurrenceValidationRule.ALTITUDE_OUT_OF_RANGE);
        }

        // record the number of records with erroneous altitudes
        if (altitude != null && (altitude == -9999 || altitude == 9999)) {
          issues.add(OccurrenceValidationRule.ALTITUDE_UNLIKELY);
        }

        // record the number of records with min/max altitude transposed
        if (min != null && max != null && min > max && max != 0) {
          issues.add(OccurrenceValidationRule.ALTITUDE_MIN_MAX_SWAPPED);
        }

        Long precisionAsLong = null;
        if (precision != null) {
          precisionAsLong = Math.round(precision);
        }
        if (altitude != null && altitude <= MAX_TO_RECORD_ALTITUDE_IN_METRES && altitude >= MIN_TO_RECORD_ALTITUDE_IN_METRES) {
          return ParseResult
            .success(ParseResult.CONFIDENCE.DEFINITE, new LongPrecisionIssue(altitude, precisionAsLong, issues));
        } else if (altitude != null) {
          return ParseResult
            .success(ParseResult.CONFIDENCE.DEFINITE, new LongPrecisionIssue(null, precisionAsLong, issues));
        } else {
          return ParseResult.fail();
        }
      }
    }.parse(null);
  }

  /**
   * Retrieve an altitude measurement, reporting any faults in the data. Handles ranges and converts feet to metres.
   *
   * @param source String to parse
   */
  private static void getAltitudeMeasurement(ParseStringToDouble source) {
    if (source.verbatim == null) {
      source.parsed = null;
      return;
    }

    Double altitudeAsDouble = null;
    try {
      boolean containsNonnumeric = MEASURE_MARKER_PATTERN.matcher(source.verbatim).matches();

      // if contains non numeric chars, check for range, remove chars and try to parse number
      if (containsNonnumeric) {
        source.addIssue(OccurrenceValidationRule.ALTITUDE_NON_NUMERIC);
        boolean isInFeet = FEET_MARKER_PATTERN.matcher(source.verbatim).matches();
        boolean isInInches = INCHES_MARKER_PATTERN.matcher(source.verbatim).matches();

        // TODO: Log there is a problem with this value

        // handle 6-7m (range) values
        if (SEP_MARKER_PATTERN.matcher(source.verbatim).matches()) {
          // we have been given a range
          try {
            String min = source.verbatim.substring(0, source.verbatim.indexOf('-')).trim();
            min = removeMeasurementMarkers(min);
            String max = source.verbatim.substring(source.verbatim.indexOf('-') + 1).trim();
            max = removeMeasurementMarkers(max);

            // TODO Log that field contains range in single field

            Double minDouble = Double.parseDouble(min);
            Double maxDouble = Double.parseDouble(max);

            if (minDouble != 0 && maxDouble != 0 && maxDouble - minDouble != 0) {
              altitudeAsDouble = (maxDouble + minDouble) / 2;
            }
          } catch (NumberFormatException ignored) {
          }
        } else {
          source.verbatim = removeMeasurementMarkers(source.verbatim);
          altitudeAsDouble = Double.parseDouble(source.verbatim);
        }

        if (altitudeAsDouble != null) {
          // convert to metric
          if (isInFeet || isInInches) {
            source.addIssue(OccurrenceValidationRule.ALTITUDE_PRESUMED_IN_FEET);
          }
          if (isInFeet) {
            altitudeAsDouble = convertFeetToMetres(altitudeAsDouble);
          } else if (isInInches) {
            altitudeAsDouble = convertInchesToMetres(altitudeAsDouble);
          }
        }
      } else {
        altitudeAsDouble = Double.parseDouble(source.verbatim);
      }
    } catch (NumberFormatException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    source.parsed = altitudeAsDouble;
  }

  /**
   * Retrieve a depth measurement, reporting any faults in the data. Handles ranges and converts feet to metres.
   *
   * @param source String to parse
   *
   * @return Double value or null
   */
  private static void getDepthMeasurement(ParseStringToDouble source) {
    if (source.verbatim == null) {
      source.parsed = null;
      return;
    }

    Double depthAsDouble = null;
    try {
      boolean containsNonnumeric = MEASURE_MARKER_PATTERN.matcher(source.verbatim).matches();

      if (containsNonnumeric) {
        source.addIssue(OccurrenceValidationRule.DEPTH_NON_NUMERIC);
        boolean isInFeet = FEET_MARKER_PATTERN.matcher(source.verbatim).matches();
        boolean isInInches = INCHES_MARKER_PATTERN.matcher(source.verbatim).matches();

        // TODO:Log "<field> contains non-numeric characters"

        // handle 6-7m values
        if (SEP_MARKER_PATTERN.matcher(source.verbatim).matches()) {
          // we have been given a range
          try {
            String min = source.verbatim.substring(0, source.verbatim.indexOf('-')).trim();
            min = removeMeasurementMarkers(min);
            String max = source.verbatim.substring(source.verbatim.indexOf('-') + 1).trim();
            max = removeMeasurementMarkers(max);

            // TODO: Log "<field> contains range supplied in single field"

            Double minDouble = Double.parseDouble(min);
            Double maxDouble = Double.parseDouble(max);

            if (minDouble != 0 && maxDouble != 0 && maxDouble - minDouble != 0) {
              depthAsDouble = (maxDouble + minDouble) / 2;
            }
          } catch (NumberFormatException ignored) {
          }
        } else {
          source.verbatim = removeMeasurementMarkers(source.verbatim);
          depthAsDouble = Double.parseDouble(source.verbatim);
        }

        if (depthAsDouble != null) {
          // convert to metric
          if (isInFeet || isInInches) {
            source.addIssue(OccurrenceValidationRule.DEPTH_PRESUMED_IN_FEET);
          }
          if (isInFeet) {
            depthAsDouble = convertFeetToMetres(depthAsDouble);
          } else if (isInInches) {
            depthAsDouble = convertInchesToMetres(depthAsDouble);
          }
        }
      } else {
        depthAsDouble = Double.parseDouble(source.verbatim);
      }
    } catch (NumberFormatException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    source.parsed = depthAsDouble;
  }

  /**
   * Remove "m" etc.
   *
   * @param s to remove measurement markers from.
   *
   * @return a new string with all measurements removed (i.e. replaced by the empty string)
   */
  static String removeMeasurementMarkers(String s) {
    if (s == null) return null;
    return REMOVE_MEASURE_MARKER_PATTERN.matcher(s).replaceAll("");
  }

  static double convertInchesToMetres(double inches) {
    return inches * INCHES_TO_METRES;
  }

  static double convertFeetToMetres(double feet) {
    return feet * FEET_TO_METRES;
  }

  /**
   * Utility container
   */
  static class ParseStringToDouble {

    String verbatim;
    Double parsed;
    Set<OccurrenceValidationRule> issues = Sets.newHashSet();

    ParseStringToDouble(String verbatim) {
      this.verbatim = verbatim;
      this.issues = issues;
    }

    void addIssue(OccurrenceValidationRule rule) {
      issues.add(rule);
    }
  }
}
