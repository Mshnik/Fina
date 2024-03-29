package model.unit.stat;

import model.unit.modifier.Modifier;
import model.unit.modifier.StatModifier;
import util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holder for the stats for a model.unit. Unless otherwise noted, all stats are non-negative.
 */
public class Stats implements Iterable<Stat> {

  /**
   * Base stats for everything, to be overridden as template. Initializes all stats to their base
   * 0-ish vals.
   *
   * <p>//TODO - use?
   */
  private static final HashMap<StatType, Object> TEMPLATE;

  static {
    TEMPLATE = new HashMap<>();
    // Base -> Null
    TEMPLATE.put(StatType.MAX_HEALTH, 0);
    TEMPLATE.put(StatType.MANA_PER_TURN, 0);
    TEMPLATE.put(StatType.ACTIONS_PER_TURN, 0);
    TEMPLATE.put(StatType.MIN_ATTACK, 0);
    TEMPLATE.put(StatType.MAX_ATTACK, 0);
    TEMPLATE.put(StatType.DAMAGE_REDUCTION, 0);
    TEMPLATE.put(StatType.MIN_ATTACK_RANGE, 0);
    TEMPLATE.put(StatType.MAX_ATTACK_RANGE, 0);
    TEMPLATE.put(StatType.SUMMON_RANGE, 0);
    TEMPLATE.put(StatType.VISION_RANGE, 0);
    TEMPLATE.put(StatType.MOVEMENT_TOTAL, 0);
    TEMPLATE.put(StatType.GRASS_COST, 0);
    TEMPLATE.put(StatType.WOODS_COST, 0);
    TEMPLATE.put(StatType.MOUNTAIN_COST, 0);
    TEMPLATE.put(StatType.SEA_COST, 0);
  }

  /**
   * Utility class for a pair of stats.
   */
  public static final class StatPair {
    public final Stat first;
    public final Stat second;

    private StatPair(Stat first, Stat second) {
      this.first = first;
      this.second = second;
    }
  }

  /**
   * The stats maintained by this unitstats
   */
  private final HashMap<StatType, Object> stats;

  /**
   * Constructor for Stats with base zero stats. Input stats must not have no duplicates among type
   * - will overwrite arbitrarily.
   */
  public Stats(Stat... stats) {
    this.stats = new HashMap<>(TEMPLATE);
    for (Stat s : stats) {
      this.stats.put(s.name, s.val);
    }
  }

  /**
   * Constructor for Stats by adding on top of given baseStats. Input stats must not have no
   * duplicates among type - will overwrite arbitrarily.
   */
  public Stats(Stats baseStats, Stat... stats) {
    this.stats = new HashMap<>(baseStats.stats);
    this.stats.put(StatType.BASE, baseStats);
    for (Stat s : stats) {
      this.stats.put(s.name, s.val);
    }
  }

  /**
   * Constructor for Stats from a base stats and a collection of modifiers.
   */
  public Stats(Stats base, Collection<Modifier> modifiers) throws IllegalArgumentException {
    this.stats = new HashMap<>(base.stats);
    stats.put(StatType.BASE, base);

    // Process modifiers - ignore non stat modifiers
    if (modifiers != null) {
      for (Modifier m : modifiers) {
        if (m instanceof StatModifier) {
          StatModifier s = (StatModifier) m;
          Object newVal = stats.get(s.modifiedStat);
          if (newVal instanceof Integer
              && (s.getModVal() instanceof Integer || s.getModVal() instanceof Double)) {
            switch (s.modType) {
              case SET_MIN:
                newVal = Math.min((int) newVal, (int) s.getModVal());
                break;
              case SET_MAX:
                newVal = Math.max((int) newVal, (int) s.getModVal());
                break;
              case ADD:
                newVal = (int) newVal + (int) s.getModVal();
                break;
              case MULTIPLY:
                newVal = (int) ((int) newVal * (double) s.getModVal());
                break;
            }
          } else if (s.getModVal() instanceof Double) {
            switch (s.modType) {
              case SET_MIN:
                newVal = Math.min((double) newVal, (double) s.getModVal());
                break;
              case SET_MAX:
                newVal = Math.max((double) newVal, (double) s.getModVal());
                break;
              case ADD:
                newVal = Mth.roundTo(((Double) newVal + (double) s.getModVal()), -2);
                break;
              case MULTIPLY:
                newVal = Mth.roundTo(((Double) newVal * (double) s.getModVal()), -2);
                break;
            }
          } else {
            throw new RuntimeException(
                "Unhandled stat value type / mod type pair "
                    + newVal.getClass()
                    + ", "
                    + s.getModVal().getClass());
          }
          stats.put(s.modifiedStat, newVal);
        }
      }
    }
  }

  /**
   * Returns true if this is a base (has no base stat), false otherwise
   */
  private boolean isBase() {
    return !stats.containsKey(StatType.BASE);
  }

  /**
   * Returns the requested stat
   */
  public Object getStat(StatType type) {
    return stats.get(type);
  }

  /**
   * Returns a list of stats for the given array of stat types. Stats are returned in the same order
   * they are queried in.
   */
  private ArrayList<Stat> getStatsList(StatType[] t, boolean filterOmittableZeroes) {
    ArrayList<Stat> s = new ArrayList<Stat>();
    for (StatType p : t) {
      if (!filterOmittableZeroes
          || (stats.get(p) instanceof Integer && ((Integer) stats.get(p)) > 0)
          || (stats.get(p) instanceof Double && ((Double) stats.get(p)) > 0)) {
        s.add(new Stat(p, stats.get(p)));
      }
    }
    return s;
  }

  /**
   * Returns an arrayList of the movement stats: - Move total - Grass cost - woods cost - mountain
   * cost
   */
  public List<Stat> getMovementCostStatsList(boolean filterOmittableZeroes) {
    StatType[] t = {StatType.GRASS_COST, StatType.WOODS_COST, StatType.MOUNTAIN_COST};
    return getStatsList(t, filterOmittableZeroes);
  }

  /**
   * Returns an arrayList of the attack stats: - Attack - Attack Range
   */
  public List<StatPair> getAttackStatsList(boolean filterOmittableZeroes) {
    StatType[] t = {
        StatType.MIN_ATTACK, StatType.MAX_ATTACK, StatType.MIN_ATTACK_RANGE, StatType.MAX_ATTACK_RANGE
    };
    Map<StatType, Stat> statTypeStatMap =
        getStatsList(t, filterOmittableZeroes)
            .stream()
            .collect(Collectors.toMap(Stat::getType, Function.identity()));

    List<StatPair> lst = new ArrayList<>();
    if (statTypeStatMap.containsKey(StatType.MAX_ATTACK)) {
      lst.add(
          new StatPair(
              statTypeStatMap.getOrDefault(StatType.MIN_ATTACK, new Stat(StatType.MIN_ATTACK, 0)),
              statTypeStatMap.getOrDefault(StatType.MAX_ATTACK, new Stat(StatType.MAX_ATTACK, 0))));
    }
    if (statTypeStatMap.containsKey(StatType.MAX_ATTACK_RANGE)) {
      lst.add(
          new StatPair(
              statTypeStatMap.getOrDefault(
                  StatType.MIN_ATTACK_RANGE, new Stat(StatType.MIN_ATTACK_RANGE, 0)),
              statTypeStatMap.getOrDefault(
                  StatType.MAX_ATTACK_RANGE, new Stat(StatType.MAX_ATTACK_RANGE, 0))));
    }
    return Collections.unmodifiableList(lst);
  }

  /**
   * Returns an arrayList of the standard stats: - Max Health - Mana Per Turn - Vision Range -
   * Summon Range
   */
  public List<Stat> getStandardStatsList(boolean filterOmittableZeroes) {
    StatType[] t = {StatType.MAX_HEALTH, StatType.VISION_RANGE, StatType.SUMMON_RANGE};
    return getStatsList(t, filterOmittableZeroes);
  }

  /**
   * Returns a new Stats with this (if this is a base) as the base or this' base if this is
   * non-base, and the given modifiers
   */
  public Stats modifiedWith(Collection<Modifier> modifiers) {
    if (isBase()) return new Stats(this, modifiers);
    else return new Stats((Stats) getStat(StatType.BASE), modifiers);
  }

  /**
   * Basic toString impelementation that shows off the stats this represents
   */
  @Override
  public String toString() {
    String s = "";
    Iterator<Stat> i = iterator();
    while (i.hasNext()) {
      Stat st = i.next();
      if (st.name == StatType.BASE) continue;
      s += st.name + " : " + st.val + ", ";
    }
    return s;
  }

  /**
   * Returns an iterator over these stats
   */
  public Iterator<Stat> iterator() {
    return new StatIterator();
  }

  /**
   * An iterator over this stats that shows each stat in turn. Might not catch concurrent
   * modification exceptions, so make sure to get a new iterator after the Stats has been modified.
   */
  private class StatIterator implements Iterator<Stat> {

    private int index;
    private final ArrayList<Stat> statArr;

    private StatIterator() {
      index = 0;
      statArr = new ArrayList<Stat>();
      for (Map.Entry<StatType, Object> e : stats.entrySet()) {
        statArr.add(new Stat(e.getKey(), e.getValue()));
      }
      Collections.sort(statArr);
    }

    /**
     * Returns true if there is another stat to return
     */
    @Override
    public boolean hasNext() {
      return index < statArr.size();
    }

    /**
     * Returns the next stat, in ordinal by type order
     */
    @Override
    public Stat next() {
      Stat s = statArr.get(index);
      index++;
      return s;
    }

    /**
     * Removal not supported - throws runtime exception
     */
    @Override
    public void remove() {
      throw new RuntimeException("Not Supported");
    }
  }
}
