package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.NumberParser;

import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for parsing min/max meter measurements in general plus specific additions and validations
 * for elevation, depth and distance above surface.
 *
 * TODO: consider to use the JScience Library:
 *  http://jscience.org/api/javax/measure/unit/package-summary.html
 *  http://jscience.org/api/javax/measure/unit/Unit.html
 */
public class MeterRangeParser {
  private static final Logger LOG = LoggerFactory.getLogger(MeterRangeParser.class);

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
   * Constant factor to convert from feet to metres.
   */
  private static final float FEET_TO_METRES = 0.3048f;

  /**
   * Constant factor to convert from inches to metres.
   */
  private static final float INCHES_TO_METRES = 0.0254f;


  /**
   * The lowest elevation value recognised as valid:
   * 10,971 m (35,994 ft) Challenger Deep, Mariana Trench[32]
   */
  private static final int MIN_ELEVATION = -11000;

  /**
   * The highest elevation value recognised as valid, Mount Everest 8,848 m (29,029 ft).
   * Highest point in the Troposphere: 17km
   * Highest point in the Stratosphere: 55km
   * Max altitude of airline cruises: 13km
   * Max altitude of weather ballons: 34km
   *
   *
   * @see <a href="http://en.wikipedia.org/wiki/Atmosphere_of_Earth">Atmosphere in wikipedia</a>
   */
  private static final int MAX_ELEVATION = 17000;

  /**
   * The lowest elevation value recognised as valid:
   * 10,971 m (35,994 ft) Challenger Deep, Mariana Trench[32]
   */
  private static final int MAX_DEPTH = Math.abs(MIN_ELEVATION);

  /**
   * Largest holes digged into the earth are ~4km.
   */
  private static final int MIN_DISTANCE = -5000;

  /**
   * Same as elevation, we use the upper end of the Troposphere.
   */
  private static final int MAX_DISTANCE = MAX_ELEVATION;

  static class MeasurementWrapper<T> {
    private T measurement;
    private boolean isInFeet;
    private boolean isInInches;
    private boolean containsNonNumeric;
    private boolean minMaxSwapped;
    private boolean tooLarge;

    public T getMeasurement() {
      return measurement;
    }

    public boolean isInFeet() {
      return isInFeet;
    }

    public boolean isInInches() {
      return isInInches;
    }

    public boolean containsNonNumeric() {
      return containsNonNumeric;
    }

    public boolean isMinMaxSwapped() {
      return minMaxSwapped;
    }

    public boolean isTooLarge() {
      return tooLarge;
    }

    public void addIssues(MeasurementWrapper<?> issues) {
      isInFeet = isInFeet || issues.isInFeet;
      isInInches = isInInches || issues.isInInches;
      containsNonNumeric = containsNonNumeric || issues.containsNonNumeric;
      minMaxSwapped = minMaxSwapped || issues.minMaxSwapped;
      tooLarge = tooLarge || issues.tooLarge;
    }
  }

  /**
   * Takes min and max values in meters and a known precision and comes up woth a single mean value and its accuracy.
   * This method tries also to parse common measurements given in feet or inch and converts them to meters.
   */
  public static MeasurementWrapper<DoubleAccuracy> parseMeterRange(String minRaw, @Nullable String maxRaw, @Nullable String precisionRaw) {
    MeasurementWrapper<DoubleAccuracy> result = new MeasurementWrapper<DoubleAccuracy>();

    MeasurementWrapper<Double> min = parseInMeter(minRaw);
    MeasurementWrapper<Double> max = parseInMeter(maxRaw);
    MeasurementWrapper<Double> prec = parseInMeter(precisionRaw);

    result.addIssues(min);
    result.addIssues(max);
    result.addIssues(prec);

    if (min.measurement == null && max.measurement == null) {
      // both are null, return issues only
      return result;
    }

    // final result vars
    Double value;
    Double accuracy;

    // check for swapped values and apply precision if min & max exist
    if (min.measurement != null && max.measurement != null) {
      // flag swapped min/max
      if (min.measurement > max.measurement) {
        result.minMaxSwapped = true;
        Double oldMin = min.measurement;
        min.measurement = max.measurement;
        max.measurement = oldMin;
      }
      // apply precision to min max if we have it
      if (prec.measurement != null) {
        min.measurement -= prec.measurement;
        max.measurement += prec.measurement;
      }
      // build the arithmetic mean and set accuracy
      value = (min.measurement + max.measurement) / 2d;
      accuracy = (max.measurement - min.measurement) / 2d;

    } else {
      // use the only value and precision for accuracy
      value = min.measurement == null ? max.measurement : min.measurement;
      accuracy = prec.measurement;
    }

    if (value != null) {
      result.measurement = new DoubleAccuracy(value, accuracy);
    }

    // finally a result, bye bye!
    return result;
  }

  public static OccurrenceParseResult<DoubleAccuracy> parseElevation(@Nullable String min, @Nullable String max, @Nullable String precision) {

    MeasurementWrapper<DoubleAccuracy> elevation = parseMeterRange(min, max, precision);

    Set<OccurrenceIssue> issues = Sets.newHashSet();
    if(elevation.containsNonNumeric) {
      issues.add(OccurrenceIssue.ELEVATION_NON_NUMERIC);
    }
    if(elevation.isInFeet || elevation.isInInches) {
      issues.add(OccurrenceIssue.ELEVATION_NOT_METRIC);
    }
    if(elevation.minMaxSwapped) {
      issues.add(OccurrenceIssue.ELEVATION_MIN_MAX_SWAPPED);
    }
    if(elevation.tooLarge) {
      issues.add(OccurrenceIssue.ELEVATION_UNLIKELY);
    }

    if (elevation.measurement == null || elevation.measurement.getValue() == null) {
      return OccurrenceParseResult.fail(issues);
    }

    DoubleAccuracy result = elevation.measurement;
    // record the number of records with altitude out of range
    if (result.getValue() > MAX_ELEVATION || result.getValue() < MIN_ELEVATION) {
      issues.add(OccurrenceIssue.ELEVATION_UNLIKELY);
      return OccurrenceParseResult.fail(issues);
    }

    return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, result, issues);
  }

  public static OccurrenceParseResult<DoubleAccuracy> parseDepth(@Nullable String min, @Nullable String max, @Nullable String precision) {
    MeasurementWrapper<DoubleAccuracy> depth = parseMeterRange(min, max, precision);

    Set<OccurrenceIssue> issues = Sets.newHashSet();
    if(depth.containsNonNumeric) {
      issues.add(OccurrenceIssue.DEPTH_NON_NUMERIC);
    }
    if(depth.isInFeet || depth.isInInches) {
      issues.add(OccurrenceIssue.DEPTH_NOT_METRIC);
    }
    if(depth.minMaxSwapped) {
      issues.add(OccurrenceIssue.DEPTH_MIN_MAX_SWAPPED);
    }
    if(depth.tooLarge) {
      issues.add(OccurrenceIssue.DEPTH_UNLIKELY);
    }

    if (depth.measurement == null || depth.measurement.getValue() == null) {
      return OccurrenceParseResult.fail(issues);
    }

    DoubleAccuracy result = depth.measurement;

    // negate depth if its negative
    if (result.getValue() < 0) {
      result = new DoubleAccuracy(-1 * result.getValue(), result.getAccuracy());
      issues.add(OccurrenceIssue.DEPTH_UNLIKELY);
    }

    // record the number of records with depth out of range
    if (result.getValue() > MAX_DEPTH) {
      issues.add(OccurrenceIssue.DEPTH_UNLIKELY);
      return OccurrenceParseResult.fail(issues);
    }

    return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, result, issues);
  }

  private static MeasurementWrapper<Double> parseInMeter(String meter) {
    MeasurementWrapper<Double> iMeter = new MeasurementWrapper<Double>();

    if (Strings.isNullOrEmpty(meter)) {
      return iMeter;
    }

    try {
      iMeter.containsNonNumeric = MEASURE_MARKER_PATTERN.matcher(meter).matches();

      if (!iMeter.containsNonNumeric()) {
        iMeter.measurement = NumberParser.parseDouble(meter);

      } else {
        iMeter.isInFeet = FEET_MARKER_PATTERN.matcher(meter).matches();
        iMeter.isInInches = INCHES_MARKER_PATTERN.matcher(meter).matches();

        // handle 6-7m values
        if (SEP_MARKER_PATTERN.matcher(meter).matches()) {
          // we have been given a range
          try {
            String min = meter.substring(0, meter.indexOf('-')).trim();
            min = removeMeasurementMarkers(min);
            String max = meter.substring(meter.indexOf('-') + 1).trim();
            max = removeMeasurementMarkers(max);

            Double minDouble = NumberParser.parseDouble(min);
            Double maxDouble = NumberParser.parseDouble(max);

            if (minDouble != 0 && maxDouble != 0 && maxDouble - minDouble != 0) {
              iMeter.measurement = (maxDouble + minDouble) / 2;
            }
          } catch (NumberFormatException ignored) {
          }

        } else {
          iMeter.measurement = NumberParser.parseDouble(removeMeasurementMarkers(meter));
        }

        if (iMeter.measurement != null) {
          // convert to metric
          if (iMeter.isInFeet) {
            iMeter.measurement = convertFeetToMetres(iMeter.measurement);
          } else if (iMeter.isInInches) {
            iMeter.measurement = convertInchesToMetres(iMeter.measurement);
          }
        }
      }
    } catch (NumberFormatException e) {
      LOG.debug("Unparsable meter measurement: {}", meter, e.getMessage());
    }

    // round to centimeters
    if (iMeter.measurement != null) {
      iMeter.measurement = Math.round(iMeter.measurement * 100.0) / 100.0;
    }
    return iMeter;
  }

  /**
   * Remove "m" etc.
   *
   * @param s to remove measurement markers from.
   *
   * @return a new string with all measurements removed (i.e. replaced by the empty string)
   */
  private static String removeMeasurementMarkers(String s) {
    if (s == null) return null;
    return REMOVE_MEASURE_MARKER_PATTERN.matcher(s).replaceAll("");
  }

  private static double convertInchesToMetres(double inches) {
    return inches * INCHES_TO_METRES;
  }

  private static double convertFeetToMetres(double feet) {
    return feet * FEET_TO_METRES;
  }

  /**
   * @return rounded int value or null if it was null or exceeds the maximum an int can hold
   */
  private static Integer roundedInt(Double x) {
    if (x == null) return null;

    Long xl = Math.round(x);
    if (xl > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Long too big for an integer");
    }
    return xl.intValue();
  }

}
