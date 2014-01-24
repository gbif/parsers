package org.gbif.common.parsers;

/**
 * Generic interface to allow multiple parser implementations.
 *
 * @param <TIN>  The input type of the parse operation
 * @param <TOUT> The output type of the parse operation
 */
public interface Parsable<TIN, TOUT> {

  /**
   * Tries to parse the input and returns a {@link ParseResult} object.
   * This should never return <code>null</code> as parse errors will be indicated in the returned object.
   *
   * @param input To parse
   *
   * @return The output result of the operation
   */
  ParseResult<TOUT> parse(TIN input);
}
