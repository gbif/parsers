package org.gbif.common.parsers.utils;

import java.util.UUID;

/**
 * Utilities to parse an external dataset synthesized key.
 * @deprecated please use {@link org.gbif.api.util.DatasetKey} instead.
 */
@Deprecated
public class ExternalDatasetKeyUtils {

  private static final int SOURCE_KEY_INDEX = 0;
  private static final int DATASET_ID_INDEX = 1;
  private static final int UUID_LENGTH = 36;
  private static final int SYNTHESIZED_KEY_SEPARATOR_LENGTH = 1;
  private static final int SYNTHESIZED_KEY_SEPARATOR_START_INDEX = 36;
  private static final int SYNTHESIZED_KEY_DATASET_ID_START_INDEX = 37;

  /**
   * Parses the synthesized Dataset key into atoms or returns null if not parsable.
   * The key is in the format: sourceKey:datasetId where the sourceKey must be a valid UUID. A UUID always has a length
   * of 36 characters (32 digits, 5 groups, and 4 hyphens) like 550e8533-e29b-41d4-a716-446655441111 for example. The
   * datasetId must just be a non empty string.
   *
   * @param key synthesized Dataset key
   *
   * @return array of strings, first entry is the sourceKey, second is the datasetId (both unvalidated), or an empty
   *         array if the key was null or its length did not qualify as a valid external dataset key
   */
  private static String[] parseIntoAtoms(String key) {
    if (key != null && key.length() > UUID_LENGTH + SYNTHESIZED_KEY_SEPARATOR_LENGTH) {
      return new String[] {key.substring(0, SYNTHESIZED_KEY_SEPARATOR_START_INDEX),
        key.substring(SYNTHESIZED_KEY_DATASET_ID_START_INDEX)};
    }
    return new String[] {};
  }

  /**
   * Parses the synthesized Dataset key's sourceKey or returns null if not parsable.
   * The sourceKey must be a valid UUID.
   *
   * @param key synthesized Dataset key
   *
   * @return sourceKey or null if not parsable or if it was not a valid UUID
   */
  public static UUID parseSourceKey(String key) {
    String[] atoms = parseIntoAtoms(key);
    try {
      return UUID.fromString(atoms[SOURCE_KEY_INDEX]);
    } catch (Exception e) {
      return null; // nonsense provided
    }
  }

  /**
   * Parses the synthesized Dataset key's datasetId or returns null if not parsable.
   * The datasetId is a String of any non empty string. Additionally, the sourceKey must be a valid UUID.
   *
   * @param key synthesized Dataset key
   *
   * @return datasetID or null if not parsable or sourceKey was not a valid UUID
   */
  public static String parseDatasetId(String key) {
    String[] atoms = parseIntoAtoms(key);
    try {
      UUID.fromString(atoms[SOURCE_KEY_INDEX]);
      return atoms[DATASET_ID_INDEX].isEmpty() ? null : atoms[DATASET_ID_INDEX];
    } catch (Exception e) {
      return null; // nonsense provided
    }
  }
}
