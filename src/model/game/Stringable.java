package model.game;

/**
 * Implementing classes have a variety of string methods for various lengths of detail
 *
 * @author MPatashnik
 */
public interface Stringable {
  /**
   * Standard toString - should be be very short, for debugging purposes only
   */
  String toString();

  /**
   * A short string that has core stats - name and costs. May be used for UI purposes
   */
  String toStringShort();

  /**
   * A longer string that has a fuller description - name, costs, and some stats. May be used for UI
   * purposes.
   */
  String toStringLong();

  /**
   * A full string that has all relevant fields, for debugging purposes only. Doesn't have to be
   * formatted well. May call the toStringShort() or toStringLong() methods of other Stringables
   * that are related, but won't call other toStringFull() to prevent infinite recursion
   */
  String toStringFull();
}
