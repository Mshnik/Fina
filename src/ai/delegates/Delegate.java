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

  /**
   * Main weight for this Delegate, to compare its value to other delegate. Output of rawScore is
   * multiplied by this value. Default value is 1, so delegates will be presumed to have a positive
   * impact if weight isn't set.
   */
  private double weight;

  /**
   * Weights applied to values within this Delegate, to compare values within its getRawScore. Of
   * varying length based on the delegate. Defaults to an array of 1's with {@link
   * #getExpectedSubweightsLength()} length.
   */
  private double[] subWeights;

  Delegate(AIActionType... actionTypes) {
    this.weight = 1;
    subWeights = new double[getExpectedSubweightsLength()];
    for (int i = 0; i < subWeights.length; i++) {
      subWeights[i] = 1;
    }
    this.validActionTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(actionTypes)));
  }

  /** Returns the current weight of this delegate. */
  public double getWeight() {
    return weight;
  }

  /** Sets the weight of this Delegate and returns it. */
  public Delegate withWeight(double weight) {
    this.weight = weight;
    return this;
  }

  /** Returns the subWeight at the given index. */
  double getSubWeight(int index) {
    return subWeights[index];
  }

  /** Returns the expected length of the subweights array. If unused, should be 0. */
  abstract int getExpectedSubweightsLength();

  /** Returns the current subweights. */
  public double[] getSubWeights() {
    return Arrays.copyOf(subWeights, subWeights.length);
  }

  /** Sets the subweights of this Delegate and returns it. */
  public Delegate withSubweights(double... subWeights) {
    if (getExpectedSubweightsLength() != subWeights.length) {
      throw new RuntimeException(
          "Can't set subWeights to "
              + Arrays.toString(subWeights)
              + ", expected length is "
              + getExpectedSubweightsLength());
    }
    this.subWeights = subWeights;
    return this;
  }

  /** Sets the subweights without checking length. Only use this if we're really sure it's ok. */
  Delegate setSubweightsUnsafe(double... subWeights) {
    this.subWeights = subWeights;
    return this;
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
    return weight * getRawScore(action);
  }

  /** Returns the most preferred action of the given actions. */
  public AIAction getPreferredAction(AIAction... actions) {
    return Arrays.stream(actions).max(Comparator.comparingDouble(this::getScore)).orElse(null);
  }
}
