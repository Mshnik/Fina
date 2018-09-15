package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/** An abstract class for a piece of an AI that functions as an adviser to the AI. */
public abstract class Delegate {

  /** The actionTypes this Delegate cares about. Other ActionTypes will always get a score of 0. */
  private final Set<AIActionType> validActionTypes;

  Delegate(AIActionType... actionTypes) {
    this.validActionTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(actionTypes)));
  }

  /**
   * Returns a raw score of how much this Delegate likes the given action. A higher value is more
   * preferred, and a 0 value is indifference. Positive is a good score (the delegate would like to
   * perform this action), negative is a bad score (the delegate would like to not perform this
   * action).
   */
  abstract double getRawScore(AIAction action);

  /** Returns the processed score of how much this Delegate likes the given action. */
  public double getScore(AIAction action) {
    if (!validActionTypes.contains(action.actionType)) {
      return 0;
    }
    return getRawScore(action);
  }

  /** Returns the most preferred action of the given actions. */
  public AIAction getPreferredAction(AIAction... actions) {
    return Arrays.stream(actions).max(Comparator.comparingDouble(this::getScore)).orElse(null);
  }
}
