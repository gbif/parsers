/*
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of bad names in UPPERCASE.
 */
public final class BlacklistedNames {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistedNames.class);

  private static final Set<String> NAMES = new HashSet<String>();

  private static final String BLACKLIST_FILE = "utils/blacklistedNames.tsv";

  static {
    init(BLACKLIST_FILE);
  }

  private BlacklistedNames() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  public static boolean contains(String s) {
    return NAMES.contains(s.toUpperCase());
  }

  public static synchronized void init(Reader reader) {
    NAMES.clear();
    BufferedReader br = new BufferedReader(reader);
    try {
      String line = br.readLine();
      while (line != null) {
        LOGGER.debug("Blacklisting: {}", line);
        NAMES.add(line.toUpperCase());
        line = br.readLine();
      }
    } catch (IOException e) {
      LOGGER.debug("Exception thrown", e);
    } finally {
      try {
        br.close();
      } catch (IOException ignored) {
      }
    }
  }

  public static synchronized void init(String filepath) {
    init(new InputStreamReader(BlacklistedNames.class.getClassLoader().getResourceAsStream(filepath)));
  }
}
