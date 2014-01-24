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

package org.gbif.common.parsers;

import org.gbif.common.parsers.utils.MappingUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author markus
 */
public class MappingUtilsTest {

  @Test
  public void testKingdoms() {
    Map<String, String> kingdoms = new HashMap<String, String>() {
      {
        put("", null);
        put("PLANT", "Plantae");
        put("FUNGI AND LICHENS", "Fungi");
        put("FUNGI", "Fungi");
        put("9ANIMALI", "Animalia");
      }
    };

    for (Map.Entry<String, String> taxon : kingdoms.entrySet()) {
      assertEquals(taxon.getValue(), MappingUtils.mapKingdom(taxon.getKey()));
    }

    assertNull(MappingUtils.mapKingdom(null));
    assertNull("Empty strings should return null", MappingUtils.mapKingdom(""));
  }

  @Test
  public void testPhyla() {
    Map<String, String> phyla = new HashMap<String, String>() {
      {
        put("", null);
        put("PLATYHERMINTHES", "Platyhelminthes");
        put("PlaTYHERMINTHES", "Platyhelminthes");
        put("?? PORIFERA ??", "Porifera");
        put("PRASINOPHYTA", "Prasinophyta");
        put("PSILOPHYTA (PSILOTUM)", "Psilophyta");
      }
    };

    for (Map.Entry<String, String> taxon : phyla.entrySet()) {
      assertEquals(taxon.getValue(), MappingUtils.mapPhylum(taxon.getKey()));
    }

    assertNull(MappingUtils.mapPhylum(null));
    assertNull("Empty strings should return null", MappingUtils.mapPhylum(""));
  }
}
