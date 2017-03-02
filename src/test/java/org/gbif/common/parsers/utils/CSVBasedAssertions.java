package org.gbif.common.parsers.utils;

import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Function;

import static org.junit.Assert.fail;

/**
 *
 */
public class CSVBasedAssertions {

  private static final String COLUMN_SEPARATOR = ";";
  private static final String COMMENT_MARKER = "#";

  /**
   * Utility function to run assertions received as Function on each rows of an input file.
   *
   * @param filepath
   * @param assertRow
   */
  public static void assertTestFile(String filepath, Function<String[], Void> assertRow) {
    File testInputFile = FileUtils.getClasspathFile(filepath);
    try (CSVReader csv = CSVReaderFactory.build(testInputFile, COLUMN_SEPARATOR, true)) {
      while (csv.hasNext()) {
        String[] row = csv.next();
        if (row == null || row[0].startsWith(COMMENT_MARKER)) {
          continue;
        }
        assertRow.apply(row);
      }
    } catch (IOException e) {
      fail("Problem reading testFile " + filepath + " " + e.getMessage());
    }
  }
}
