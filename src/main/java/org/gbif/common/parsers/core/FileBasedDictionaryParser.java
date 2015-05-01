package org.gbif.common.parsers.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;

/**
 * A very simple Dictionary backed by a tab delimited file.
 */
public abstract class FileBasedDictionaryParser<T> extends DictionaryBackedParser<T> {

  public FileBasedDictionaryParser(boolean caseSensitive) {
    super(caseSensitive);
  }

  protected void init(InputStream input) {
    init(input, null);
  }

  /**
   * Init the parser to read the InputStream and ignore lines starting with the commentMarker.
   *
   * @param input
   * @param commentMarker marker identifying a commented line (e.g. #) or null to read all lines
   */
  protected void init(InputStream input, String commentMarker) {
    init(new Source(input, commentMarker));
  }

  /**
   * Returns the value read from the dictionary as an instance of <T>
   *
   * @param value
   * @return
   */
  protected abstract T fromDictFile(String value);

  /**
   * An iterator over a well formed tab file.
   * Should the file be poorly formed expect runtime exceptions.
   */
  class Source implements Iterator<KeyValue<String, T>> {

    private final BufferedReader r;
    private final Pattern tab = Pattern.compile("\t");
    private final String commentMarker;
    private String line = null;

    Source(InputStream file) {
      this(file, null);
    }

    Source(InputStream file, String commentMarker) {
      r = new BufferedReader(new InputStreamReader(file, Charsets.UTF_8));
      this.commentMarker = commentMarker;
    }

    @Override
    public boolean hasNext() {
      if (line != null) {
        return true;
      }

      try {
        // we discard empty or commented lines
        do {
          line = r.readLine();
        } while (line != null && !isValidLine(line));
      } catch (IOException ignored) {
        close();
        return false;
      }

      if (line == null) {
        close();
        return false;
      } else {
        return true;
      }
    }

    /**
     * Check if a line is valid or not.
     * A valid line is not a comment (if configured) and should be in the form "key<tab>value".
     *
     * @param line
     * @return
     */
    private boolean isValidLine(String line) {
      if (line == null) {
        return false;
      }

      if (commentMarker != null) {
        if (line.startsWith(commentMarker)) {
          return false;
        }
      }
      return (tab.split(line).length == 2);
    }

    @Override
    public KeyValue<String, T> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      String[] atoms = tab.split(line);
      line = null;
      return new KeyValue<String, T>(StringUtils.trimToNull(atoms[0]), fromDictFile(StringUtils.trimToNull(atoms[1])));
    }

    @Override
    public void remove() {
    }

    public void close() {
      if (r != null) {
        try {
          r.close();
        } catch (IOException ignored) {
        }
      }
    }
  }
}
