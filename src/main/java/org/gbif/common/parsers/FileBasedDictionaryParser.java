package org.gbif.common.parsers;

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
public class FileBasedDictionaryParser extends DictionaryBackedParser<String, String> {

  public FileBasedDictionaryParser(boolean caseSensitive, final InputStream... inputs) {
    super(caseSensitive);
    for (InputStream input : inputs) {
      init(new Source(input));
    }
  }


  /**
   * An iterator over a well formed tab file. Should the file be poorly formed expect runtime exceptions.
   *
   * @author tim
   */
  static class Source implements Iterator<KeyValue<String, String>> {

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
    public KeyValue<String, String> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      String[] atoms = tab.split(line);
      line = null;
      return new KeyValue<String, String>(StringUtils.trimToNull(atoms[0]), StringUtils.trimToNull(atoms[1]));
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
