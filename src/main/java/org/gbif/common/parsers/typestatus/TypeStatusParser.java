package org.gbif.common.parsers.typestatus;

import org.gbif.api.vocabulary.TypeStatus;
import org.gbif.common.parsers.FileBasedDictionaryParser;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class TypeStatusParser extends FileBasedDictionaryParser {

  private static TypeStatusParser singletonObject = null;
  private static final CharMatcher NON_LETTERS = CharMatcher.JAVA_LETTER.negate();
  private static final Pattern NAME_SEPARATOR = Pattern.compile("^(.+) OF ");

  private TypeStatusParser(InputStream... file) {
    super(false, file);
    // also make sure we have all enum values mapped
    for (TypeStatus ts : TypeStatus.values()) {
      add(ts.name(), ts.name());
    }
  }

  @Override
  protected String normalize(String value) {
    // uppper case and trimmed
    value = super.normalize(value);
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
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
        singletonObject = new TypeStatusParser(TypeStatusParser.class.getResourceAsStream("/dictionaries/parse/typeStatus.txt"));
      }
    }
    return singletonObject;
  }


}
