package org.gbif.common.parsers;

import org.gbif.api.vocabulary.TypeStatus;
import org.gbif.common.parsers.core.EnumParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/typeStatus.txt.
 */
public class TypeStatusParser extends EnumParser<TypeStatus> {

  private static TypeStatusParser singletonObject = null;
  private static final CharMatcher NON_LETTERS = CharMatcher.JAVA_LETTER.negate();
  private static final Pattern NAME_SEPARATOR = Pattern.compile("^(.+) OF ");

  private TypeStatusParser() {
    super(TypeStatus.class, false);
    init(TypeStatusParser.class.getResourceAsStream("/dictionaries/parse/typeStatus.txt"));
  }

  @Override
  protected String normalize(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    // uppercase
    value = value.toUpperCase().trim();

    // keep only words before the typifiedName if existing, e.g. Holotype for "Holotype of Dianthus fruticosus ssp. amorginus Runemark"
    Matcher m = NAME_SEPARATOR.matcher(value);
    if (m.find()) {
      value = m.group(1);
    }
    // remove whitespace and non letters
    return NON_LETTERS.removeFrom(value);
  }

  public static TypeStatusParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (TypeStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new TypeStatusParser();
      }
    }
    return singletonObject;
  }


}
