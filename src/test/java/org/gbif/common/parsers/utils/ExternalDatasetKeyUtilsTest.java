package org.gbif.common.parsers.utils;

import java.util.UUID;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExternalDatasetKeyUtilsTest {

  private static final String EXTERNAL_DATASET_KEY =
    "7879e569-4a13-4643-b833-d1a564675b86:urn%3Alsid%3Aknb.ecoinformatics.org%3Aknb-lter-cdr%3A8133";
  private static final String INVALID_EXTERNAL_DATASET_KEY =
    "7879e569:urn%3Alsid%3Aknb.ecoinformatics.org%3Aknb-lter-cdr%3A8133";
  private static final String SHORT_EXTERNAL_DATASET_KEY = "7879e569";

  @Test
  public void testParseSourceKeyInvalid() {
    UUID sourceKey = ExternalDatasetKeyUtils.parseSourceKey(INVALID_EXTERNAL_DATASET_KEY);
    assertNull(sourceKey);
  }

  @Test
  public void testParseSourceKeyShort() {
    UUID sourceKey = ExternalDatasetKeyUtils.parseSourceKey(SHORT_EXTERNAL_DATASET_KEY);
    assertNull(sourceKey);
  }

  @Test
  public void testParseSourceKey() {
    UUID sourceKey = ExternalDatasetKeyUtils.parseSourceKey(EXTERNAL_DATASET_KEY);
    assertEquals(UUID.fromString("7879e569-4a13-4643-b833-d1a564675b86"), sourceKey);
  }

  @Test
  public void testParseDatasetIdInvalid() {
    String datasetId = ExternalDatasetKeyUtils.parseDatasetId(INVALID_EXTERNAL_DATASET_KEY);
    assertNull(datasetId);
  }

  @Test
  public void testParseDatasetId() {
    String datasetId = ExternalDatasetKeyUtils.parseDatasetId(EXTERNAL_DATASET_KEY);
    assertEquals("urn%3Alsid%3Aknb.ecoinformatics.org%3Aknb-lter-cdr%3A8133", datasetId);
  }

}
