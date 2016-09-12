package org.gbif.common.parsers.utils;

import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.api.vocabulary.Rank;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Utilities to work on classifications.
 */
public final class ClassificationUtils {

  // used to clean up bad characters
  private static final Pattern CLEAN_REG_EX = Pattern.compile("[{}§';_|$%!?]+");

  // common null strings to ignore for fast performance.
  // Less frequent ones are kept in the blacklisted names dictionary!
  public static final Set<String> NULL_STRINGS =
    new HashSet<String>(Arrays.asList("/N", "\\", "\\\\", "\\N", "\\\\N", "null", "NULL", "Null"));

  private static final NameParser PARSER = new NameParser();

  private ClassificationUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  /**
   * Parses a canonical name at a specific Rank.
   */
  public static String canonicalName(String scientificName, Rank rank) {
    ParsedName pn = null;
    try {
      pn = PARSER.parse(scientificName, rank);
    } catch (UnparsableException e) {
    }
    return pn.canonicalNameWithMarker();
  }

  /**
   * Parses a scientific name and creates the canonical name including a potential hybrid and rank marker
   * plus the cultivar and strain names if existing.
   * Note: This method once used to only include the hybrid marker - if that is still needed revert to buildName
   * method.
   */
  public static String canonicalName(String scientificName) {
    return canonicalName(scientificName,null);
  }

  /**
   * Cleans up a taxon as far as possible by removing erroneous chars etc.
   * This does not do any parsing.
   *
   * @param taxon to check
   */
  public static String clean(String taxon) {
    if (Strings.isNullOrEmpty(taxon) || NULL_STRINGS.contains(taxon)) {
      return null;
    }

    String cleanedTaxon = taxon;

    // if it is a single word and ALL "UPPERCASE", turn it into a Capitalised word
    // Note: if we lowercase names with multiple words we might accidently create valid looking names by lowercasing the
    // author
    // for example ABIES ALBA REMSEN will become an Abies alba remsen which will then be interpreted badly
    // ABIES ALBA LINNEAUS 1771 will even be Abies alba linneaus 1771, a perfectly formed zoological name
    if (!cleanedTaxon.contains(" ") && cleanedTaxon.equals(cleanedTaxon.toUpperCase())) {
      cleanedTaxon = cleanedTaxon.substring(0, 1) + cleanedTaxon.substring(1).toLowerCase();
    }

    // remove the " from names with it at the beginning and end
    while (cleanedTaxon.length()> 0 && cleanedTaxon.charAt(0) == '\"' && cleanedTaxon.charAt(cleanedTaxon.length() - 1) == '\"') {
      if (cleanedTaxon.length() == 1) {
        return null;
      }
      cleanedTaxon = cleanedTaxon.substring(1, cleanedTaxon.length() - 1);
    }

    // remove the " from names with it just at the beginning
    while (cleanedTaxon.length()> 0 && cleanedTaxon.charAt(0) == '\"') {
      if (cleanedTaxon.length() == 1) {
        return null;
      }
      cleanedTaxon = cleanedTaxon.substring(1, cleanedTaxon.length());
    }

    // remove the " from names with it just at the end
    while (cleanedTaxon.length()> 0 && cleanedTaxon.charAt(cleanedTaxon.length() - 1) == '\"') {
      if (cleanedTaxon.length() == 1) {
        return null;
      }
      cleanedTaxon = cleanedTaxon.substring(0, cleanedTaxon.length() - 1);
    }


    // remove noise
    cleanedTaxon = CLEAN_REG_EX.matcher(cleanedTaxon).replaceAll("");
    cleanedTaxon = cleanedTaxon.trim();

    // don't let any blacklisted names through
    if (BlacklistedNames.contains(cleanedTaxon.toUpperCase()) || (cleanedTaxon != taxon && BlacklistedNames
      .contains(taxon.toUpperCase()))) {
      // blacklisted name
      return null;
    }

    return Strings.emptyToNull(cleanedTaxon);
  }

  /**
   * Clean some noise from the author. A large proportion are "\N" for example.
   *
   * @param author to clean
   *
   * @return cleaned author
   */
  public static String cleanAuthor(String author) {
    if (Strings.isNullOrEmpty(author) || NULL_STRINGS.contains(author)) {
      return null;
    }

    String cleanedAuthor = author;

    // remove the " from names with it at the beginning and end
    while (cleanedAuthor.length() >0 && cleanedAuthor.charAt(0) == '\"' && cleanedAuthor.charAt(cleanedAuthor.length() - 1) == '\"') {
      if (cleanedAuthor.length() == 1) {
        return null;
      }
      cleanedAuthor = cleanedAuthor.substring(1, cleanedAuthor.length() - 1);
    }

    // remove noise
    cleanedAuthor = CLEAN_REG_EX.matcher(cleanedAuthor).replaceAll("");
    cleanedAuthor = cleanedAuthor.trim();

    return Strings.emptyToNull(cleanedAuthor);
  }

  /**
   * Parses a scientific name without knowing its Rank.
   * @deprecated unsufficiently documented; should be in NameParser project if it is needed at all
   */
  @Deprecated
  public static String parseName(String scientificName) {
    return parseName(scientificName, null);
  }

  /**
   * Parses a scientific name of a specific rank.
   * @deprecated unsufficiently documented; should be in NameParser project if it is needed at all
   */
  @Deprecated
  public static String parseName(String scientificName, Rank rank) {

    try {
      ParsedName pn = PARSER.parse(scientificName, rank);
      // Handle Aus sp. and Aus bus spp.
      if (pn.isIndetermined()) {
          pn.setRank(null);
      }
      return pn.fullName();

    } catch (UnparsableException e) {
      // TODO: logging
    }

    // looks dirty, so try and normalize it as best we can and get a canonical at least
    String canon = PARSER.parseToCanonical(scientificName, rank);
    if (canon != null) {
      return canon;
    }

    return scientificName;
  }
}
