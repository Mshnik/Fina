package ai.delegates;

import ai.AIAction;
import java.util.Arrays;
import java.util.Comparator;

/** An interface for a piece of an AI that functions as an adviser to the AI. */
@FunctionalInterface
public interface Delegate {

  /**
   * Returns a score of how much this Delegate likes the given action. A higher value is more
   * preferred, and a 0 value is indifference. Positive is a good score (the delegate would like to
   * perform this action), negative is a bad score (the delegate would like to not perform this
   * action).
   */
  public double getScore(AIAction action);

  /** Returns the most preferred action of the given actions. */
  public default AIAction getPreferredAction(AIAction... actions) {
    return Arrays.stream(actions).max(Comparator.comparingDouble(this::getScore)).orElse(null);
  }
}
