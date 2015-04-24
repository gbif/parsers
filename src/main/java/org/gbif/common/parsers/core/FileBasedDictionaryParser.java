package org.gbif.common.parsers.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;

/**
 * A very simple Dictionary backed by a tab delimited file.
 */
public abstract class FileBasedDictionaryParser<T> extends DictionaryBackedParser<T> {

  public FileBasedDictionaryParser(boolean caseSensitive) {
    super(caseSensitive);
  }

  protected void init(InputStream input) {
    init(new Source(input));
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
    private String line = null;

    Source(InputStream file) {
      r = new BufferedReader(new InputStreamReader(file, Charsets.UTF_8));
    }

    @Override
    public boolean hasNext() {
      if (line != null) {
        return true;
      }

      try {
        // we discard bad lines
        boolean valid = false;
        do {
          line = r.readLine();
          if (line != null) {
            valid = tab.split(line).length == 2;
          }
        } while (!valid && line != null);
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
