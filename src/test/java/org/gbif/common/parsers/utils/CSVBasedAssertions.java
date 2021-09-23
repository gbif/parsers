/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers.utils;

import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Helper to help running assertions against data held in CSV files.
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
