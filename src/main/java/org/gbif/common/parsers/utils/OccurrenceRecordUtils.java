package org.gbif.common.parsers.utils;


/**
 * Utilities for dealing with OccurrenceRecord.
 * TODO i18n for the formatting of issues
 */
public class OccurrenceRecordUtils {

  /**
   * Constant values for identifier types
   */

  public static final int IDENTIFIERTYPE_GUID = 1;
  public static final int IDENTIFIERTYPE_FIELDNUMBER = 2;
  public static final int IDENTIFIERTYPE_COLLECTORNUMBER = 3;
  public static final int IDENTIFIERTYPE_ACCESSIONNUMBER = 4;
  public static final int IDENTIFIERTYPE_SEQUENCENUMBER = 5;
  public static final int IDENTIFIERTYPE_OTHERCATALOGNUMBER = 6;

  /**
   * Constant values for image types
   */

  public static final int IMAGETYPE_UNKNOWN = 0;
  public static final int IMAGETYPE_PRODUCT = 1;
  public static final int IMAGETYPE_UNKNOWNIMAGE = 2;
  public static final int IMAGETYPE_LIVEORGANISMIMAGE = 3;
  public static final int IMAGETYPE_SPECIMENIMAGE = 4;
  public static final int IMAGETYPE_LABELIMAGE = 5;

  /**
   * Constant values for link types
   */

  public static final int LINKTYPE_UNKNOWN = 0;
  public static final int LINKTYPE_OCCURRENCEPAGE = 1;

  /**
   * Constant for clearing issue fields
   */
  public static final int NO_ISSUES = 0x00;

  /**
   * Constant bit values for occurrence_record.geospatial_issue
   */

  /**
   * Set if latitude appears to have the wrong sign
   */
  public static final int GEOSPATIAL_PRESUMED_NEGATED_LATITUDE = 0x01;

  /**
   * Set if longitude appears to have the wrong sign
   */
  public static final int GEOSPATIAL_PRESUMED_NEGATED_LONGITUDE = 0x02;

  /**
   * Set if coordinates do not match country name
   */
  public static final int GEOSPATIAL_COUNTRY_COORDINATE_MISMATCH = 0x20;

  /**
   * Set if country name is not understood
   */
  public static final int GEOSPATIAL_UNKNOWN_COUNTRY_NAME = 0x40;

  /**
   * All geospatial bits
   */
  public static final int GEOSPATIAL_MASK = 0xFFFF;

  /**
   * Constant bit values for occurrence_record.taxonomic_issue
   */

  /**
   * Set if scientific name cannot be parsed as such
   */
  public static final int TAXONOMIC_INVALID_SCIENTIFIC_NAME = 0x01;

  /**
   * Set if kingdom not known for record
   */
  public static final int TAXONOMIC_UNKNOWN_KINGDOM = 0x02;

  /**
   * Set if scientific name as provided is ambiguous (e.g. inter-kingdom homonym)
   */
  public static final int TAXONOMIC_AMBIGUOUS_NAME = 0x04;

  /**
   * All taxonomic bits
   */
  public static final int TAXONOMIC_MASK = 0x07;

  /**
   * Constant bit values for occurrence_record.other_issue
   */

  /**
   * Set if record has no catalogue number/unit id
   */
  public static final int OTHER_MISSING_CATALOGUE_NUMBER = 0x01;

  /**
   * Set if basis of record not known
   */
  public static final int OTHER_MISSING_BASIS_OF_RECORD = 0x02;

  /**
   * Set if occurrence date not valid
   */
  public static final int OTHER_INVALID_DATE = 0x04;

  /**
   * Set if country inferred from coordinates (probably should be handled differently) TODO
   */
  public static final int OTHER_COUNTRY_INFERRED_FROM_COORDINATES = 0x08;

  /**
   * All other bits
   */
  public static final int OTHER_MASK = 0x0F;

  /**
   * Format a report of the taxonomic issues with a record
   *
   * @return String summary
   */
  public static String formatTaxonomicIssue(int issue) {
    StringBuilder buffer = new StringBuilder();

    if (issue != 0) {
      buffer.append("Taxonomic issues:");
      String prefix = "";
      if ((issue & TAXONOMIC_INVALID_SCIENTIFIC_NAME) != 0) {
        buffer.append(prefix).append(" scientific name not validly formed");
        prefix = ";";
      }
      if ((issue & TAXONOMIC_UNKNOWN_KINGDOM) != 0) {
        buffer.append(prefix).append(" kingdom not known for record");
        prefix = ";";
      }
      if ((issue & TAXONOMIC_AMBIGUOUS_NAME) != 0) {
        buffer.append(prefix).append(" supplied name ambiguous");
      }
      buffer.append('.');
    }

    return buffer.toString();
  }

  /**
   * Format a report of the other issues with a record
   *
   * @return String summary
   */
  public static String formatOtherIssue(int issue) {
    StringBuilder buffer = new StringBuilder();

    if (issue != 0) {
      buffer.append("Miscellaneous issues:");
      String prefix = "";
      if ((issue & OTHER_MISSING_CATALOGUE_NUMBER) != 0) {
        buffer.append(prefix).append(" missing catalogue number");
        prefix = ";";
      }
      if ((issue & OTHER_MISSING_BASIS_OF_RECORD) != 0) {
        buffer.append(prefix).append(" basis of record not known");
        prefix = ";";
      }
      if ((issue & OTHER_INVALID_DATE) != 0) {
        buffer.append(prefix).append(" supplied date invalid");
        prefix = ";";
      }
      if ((issue & OTHER_COUNTRY_INFERRED_FROM_COORDINATES) != 0) {
        buffer.append(prefix).append(" country inferred from coordinates");
      }
      buffer.append('.');
    }

    return buffer.toString();
  }
}
