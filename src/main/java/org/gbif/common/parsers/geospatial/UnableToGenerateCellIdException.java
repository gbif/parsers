package org.gbif.common.parsers.geospatial;

/**
 * Used to indicate that a Cell Id cannot be instantiated.
 */
public class UnableToGenerateCellIdException extends Exception {

  private static final long serialVersionUID = 5154386920306317433L;

  /**
   * @param m The message
   */
  public UnableToGenerateCellIdException(String m) {
    super(m);
  }
}
