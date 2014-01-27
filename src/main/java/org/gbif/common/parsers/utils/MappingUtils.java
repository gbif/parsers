/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.common.parsers.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static mapping utility that keeps maps for mapping kingdoms and phyla.
 * The actual maps are loaded as TAB separated files with 2 columns.
 * The first column is the verbatim, 2nd the cleaned version.
 * Lookup methods are case insensitive!
 */
public class MappingUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MappingUtils.class);

  private static final Map<String, String> KINGDOMS = new HashMap<String, String>();
  private static final Map<String, String> PHYLA = new HashMap<String, String>();
  private static final Map<String, Integer> BASIS_OF_RECORD = new HashMap<String, Integer>();
  private static final String KINGDOM_FILE = "dictionaries/parse/kingdoms.txt";
  private static final String PHYLA_FILE = "dictionaries/parse/phyla.txt";
  private static final String BASIS_OF_RECORD_FILE = "basisOfRecordIds.txt";

  private static final Pattern TAB_DELIMITED = Pattern.compile("\t");

  static {
    init();
  }

  private MappingUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  private static void fillMap(Map<String, String> map, String file) {
    map.clear();
    Reader reader = new InputStreamReader(MappingUtils.class.getClassLoader().getResourceAsStream(file));
    BufferedReader br = new BufferedReader(reader);
    try {
      String line = br.readLine();
      while (line != null) {
        String[] cols = TAB_DELIMITED.split(line);
        if (cols.length > 1) {
          map.put(cols[0].toUpperCase(), cols[1]);
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      LOGGER.debug("Exception thrown while reading mappings", e);
    } finally {
      try {
        br.close();
      } catch (IOException ignored) {
      }
    }
  }

  private static void init() {
    // read kingdoms
    fillMap(KINGDOMS, KINGDOM_FILE);
    // read phyla
    fillMap(PHYLA, PHYLA_FILE);

    // read Basis of Records
    fillStrToIntMap(BASIS_OF_RECORD, BASIS_OF_RECORD_FILE);
  }

  private static void fillStrToIntMap(Map<String, Integer> map, String file) {
    map.clear();
    Reader reader = new InputStreamReader(MappingUtils.class.getClassLoader().getResourceAsStream(file));
    BufferedReader br = new BufferedReader(reader);
    try {
      String line = br.readLine();
      while (line != null) {
        String[] cols = TAB_DELIMITED.split(line);
        if (cols.length > 1) {
          map.put(cols[0].toUpperCase(), Integer.valueOf(cols[1]));
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      LOGGER.debug("Exception thrown while reading mappings", e);
    } finally {
      try {
        br.close();
      } catch (IOException ignored) {
      }
    }

  }

  /**
   * Case insensitive CoL kingdom lookup.
   *
   * @return the clean CoL kingdom or null if unknown
   * @deprecated use checklistbanks HigherTaxaLookup instead which is included in the nub lookup service
   */
  @Deprecated
  public static String mapKingdom(String k) {
    if (k == null) {
      return null;
    }
    return KINGDOMS.get(k.toUpperCase());
  }

  /**
   * Case insensitive pyhlum lookup.
   *
   * @return the clean phylum or null if unknown
   * @deprecated use checklistbanks HigherTaxaLookup instead which is included in the nub lookup service
   */
  @Deprecated
  public static String mapPhylum(String p) {
    if (p == null) {
      return null;
    }
    return PHYLA.get(p.toUpperCase());
  }

  public static Integer mapBasisOfRecord(String bor) {
    if (bor == null) {
      return null;
    }

    return BASIS_OF_RECORD.get(bor.toUpperCase());
  }

}
