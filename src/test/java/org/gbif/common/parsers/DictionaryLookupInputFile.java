package org.gbif.common.parsers;

import org.gbif.common.parsers.basisofrecord.BasisOfRecordParser;
import org.gbif.common.parsers.countryname.CountryNameParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * A utility that will use the named dictionary, and read an input file,
 * emiting to the output file only those records that do not have
 * dictionary values.
 * In essence you use this to find out which terms from a list are not in the
 * dictionary
 *
 * @author tim
 */
public class DictionaryLookupInputFile {

  /**
   * @param args dictionary (e.g. basisOfRecord, countryName), inputFile, outputFile
   */
  public static void main(String[] args) throws IOException {
    Parsable<String, String> p = null;
    if ("basisOfRecord".equalsIgnoreCase(args[0])) {
      p = BasisOfRecordParser.getInstance();
    } else if ("countryName".equalsIgnoreCase(args[0])) {
      p = CountryNameParser.getInstance();
    }

    BufferedReader r = new BufferedReader(new FileReader(args[1]));
    BufferedWriter w = new BufferedWriter(new FileWriter(args[2]));
    String line = null;
    List<String> results = new ArrayList<String>();
    while ((line = r.readLine()) != null) {
      line = StringUtils.trimToNull(line);
      if (line != null) {
        ParseResult<String> x = p.parse(line);
        if (ParseResult.STATUS.FAIL == x.getStatus()) {
          results.add(line);
        }
      }
    }
    Collections.sort(results);
    for (String s : results) {
      w.write(s + "\n");
    }

    r.close();
    w.close();
  }
}
