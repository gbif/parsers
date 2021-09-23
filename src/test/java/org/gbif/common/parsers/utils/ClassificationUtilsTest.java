package org.gbif.common.parsers.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.gbif.common.parsers.utils.ClassificationUtils.clean;
import static org.gbif.common.parsers.utils.ClassificationUtils.cleanAuthor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClassificationUtilsTest {

  @Test
  public void testBlacklist() {
    assertNull(clean("phylum"));
    assertNull(clean("PHYLUM"));
    assertNull(clean("Phylum"));
    assertNull(clean("UnPLaCED_CLaSS"));
    // assertNull(clean("{UNPLACED_CLASS")); Won't work because of the way clean is implemented at the moment.
  }

  @Test
  public void testClean() {
    Map<String, String> taxons = new HashMap<String, String>() {

      {
        put("", null);
        put("Aus", "Aus");
        put("AUS", "Aus");
        put("AUS BUS", "AUS BUS");
        put("\"Aus", "Aus");
        put("\"Aus\"", "Aus");
        put("{Aus", "Aus");
        put("{ ", null);
        put("\\", null);
        put("\"", null);
        put("\"\"", null);
        put("\"?", null);
        put("  Aus Bus   ", "Aus Bus");
        put("\"{Aus\"", "Aus");
        put("\"\"\"{Aus\"\"\"", "Aus");
        put("\"? gryphoids", "gryphoids");
      }
    };

    for (Map.Entry<String, String> taxon : taxons.entrySet()) {
      assertEquals(taxon.getValue(), clean(taxon.getKey()));
    }

    assertNull(clean(null));
    assertNull(clean(""), "Empty strings should return null");
  }

  @Test
  public void testCleanAuthor() {
    Map<String, String> authors = new HashMap<String, String>() {

      {
        put("Ant. Müller", "Ant. Müller");
        put("Lineé", "Lineé");
        put("Mull", "Mull");
        put("(A. Müller) B. Meyer", "(A. Müller) B. Meyer");
        put("B. Meyer", "B. Meyer");
        put("L.", "L.");
        put("A. Müller & B. Meier", "A. Müller & B. Meier");
        put("A. Müller, B. Mei. C. Sone", "A. Müller, B. Mei. C. Sone");
        put("L", "L");
        put("\"A. Meyer\"", "A. Meyer");
        // put("a. meier", "A. Meier");
        put(" L.", "L.");
        put("L. ", "L.");
        put("L. {", "L.");
        put("\\", null);
        put("\\\\", null);
        put("\"", null);
        put("{} ", null);
      }
    };

    for (Map.Entry<String, String> author : authors.entrySet()) {
      assertEquals(author.getValue(), cleanAuthor(author.getKey()));
    }

    assertNull(cleanAuthor(null));
    assertNull(cleanAuthor(""), "Empty strings should return null");
  }

}
