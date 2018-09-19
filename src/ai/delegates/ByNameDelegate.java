package ai.delegates;

import ai.AIAction.AIActionType;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of {@link Delegate} that associates a string name to each index in subWeights. This
 * allows for getting a subWeight by name in addition to by index.
 */
public abstract class ByNameDelegate extends Delegate {

  /**
   * A map of the names known to this delegate mapped to the index in subweights they correspond to.
   * Default empty.
   */
  private Map<String, Integer> nameToSubweightIndexMap;

  /**
   * Subweight to use if a name isn't in the map. Default 1, so all unknown names will be presumed
   * to have a positive impact.
   */
  private double fallbackSubweight;

  /** Constructs a new ByNameDelegate for the given map and action types. */
  ByNameDelegate(AIActionType... actionTypes) {
    super(actionTypes);
    fallbackSubweight = 1;
    this.nameToSubweightIndexMap = new HashMap<>();
  }

  /** Not used in subclasses of this. */
  @Override
  final int getExpectedSubweightsLength() {
    return 0;
  }

  /** Don't check for length in ByName, since it isn't known. Use length to always pass check. */
  @Override
  public Delegate withSubweights(double... subWeights) {
    return super.setSubweightsUnsafe(subWeights);
  }

  /** Sets the fallback subweight and returns this. */
  public Delegate withFallbackSubweight(double fallbackSubweight) {
    this.fallbackSubweight = fallbackSubweight;
    return this;
  }

  /** Adds the given name to index association to this ByNameDelegate. */
  public void addNameToSubweightIndex(String name, int index) {
    nameToSubweightIndexMap.put(name, index);
  }

  /**
   * Returns the subweight for the given name. Returns fallbackSubweight if the name doesn't map to
   * an index.
   */
  double getSubWeight(String name) {
    if (nameToSubweightIndexMap.containsKey(name)) {
      return getSubWeight(nameToSubweightIndexMap.get(name));
    } else {
      return fallbackSubweight;
    }
  }
}
