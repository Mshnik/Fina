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
   */
  private Map<String, Integer> nameToSubweightIndexMap;

  /** Constructs a new ByNameDelegate for the given map and action types. */
  ByNameDelegate(AIActionType... actionTypes) {
    super(actionTypes);
    this.nameToSubweightIndexMap = new HashMap<>();
  }

  /** Adds the given name to index association to this ByNameDelegate. */
  public void addNameToSubweightIndex(String name, int index) {
    nameToSubweightIndexMap.put(name, index);
  }

  /**
   * Returns the subweight for the given name. Throws an IllegalArgumentException if the key is
   * unknown.
   */
  double getSubWeight(String name) {
    if (nameToSubweightIndexMap.containsKey(name)) {
      return getSubWeight(nameToSubweightIndexMap.get(name));
    } else {
      throw new IllegalArgumentException("Unknown name " + name);
    }
  }
}
