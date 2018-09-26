package ai.delegates;

import ai.AIAction.AIActionType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

  /** Overrides {@link Delegate#copy()} to add name to weights map and fallback weight. */
  @Override
  public Delegate copy() {
    ByNameDelegate delegate = (ByNameDelegate) super.copy();
    delegate.nameToSubweightIndexMap.putAll(nameToSubweightIndexMap);
    delegate.fallbackSubweight = fallbackSubweight;
    return delegate;
  }

  /** Sets the weight of this Delegate and returns it. Overridden to narrow return type. */
  @Override
  public ByNameDelegate withWeight(double weight) {
    super.withWeight(weight);
    return this;
  }

  /** Not used in subclasses of this. */
  @Override
  final int getExpectedSubweightsLength() {
    return 0;
  }

  /** Has headers for names by index. */
  @Override
  public final List<String> getSubweightsHeaders() {
    return nameToSubweightIndexMap
        .entrySet()
        .stream()
        .sorted(Comparator.comparingInt(Entry::getValue))
        .map(Entry::getKey)
        .collect(Collectors.toList());
  }

  /** Don't check for length in ByName, since it isn't known. Use length to always pass check. */
  @Override
  public ByNameDelegate withSubweights(double... subWeights) {
    super.setSubweightsUnsafe(subWeights);
    return this;
  }

  /** Sets the fallback subweight and returns this. */
  public ByNameDelegate withFallbackSubweight(double fallbackSubweight) {
    this.fallbackSubweight = fallbackSubweight;
    return this;
  }

  /** Adds the given name to index association to this ByNameDelegate and returns this */
  public ByNameDelegate withNameToSubweightIndex(String name, int index) {
    nameToSubweightIndexMap.put(name, index);
    return this;
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

  /**
   * Alters the current weight of this delegate by the given delta. Overridden to narrow return
   * type.
   */
  @Override
  public ByNameDelegate changeWeight(double deltaWeight) {
    super.changeWeight(deltaWeight);
    return this;
  }

  /** Returns the subWeight at the given index. Overridden to narrow return type. */
  @Override
  public ByNameDelegate changeSubWeight(int index, double deltaWeight) {
    super.changeSubWeight(index, deltaWeight);
    return this;
  }

  /** Returns the subWeight at the given name. */
  public ByNameDelegate changeSubWeight(String name, double deltaWeight) {
    return changeSubWeight(nameToSubweightIndexMap.get(name), deltaWeight);
  }
}
