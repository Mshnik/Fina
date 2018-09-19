package model.unit;

import java.util.Map;
import model.unit.building.Building;
import model.unit.combatant.Combatant;

/** Interface representing a unit that can summon or build units. */
public interface Summoner {

  /**
   * Returns true if summoning a model.unit is currently ok - checks surrounding area for free space
   */
  public boolean hasSummonSpace();

  /**
   * Returns true if building a model.unit is currently ok - checks surrounding area for free
   * ancient ground
   */
  public boolean hasBuildSpace();

  /** Returns the list of things this can summon. (Doesn't take into account cost or space). * */
  public Map<String, Combatant> getSummonables();

  /** Returns the list of things this can build. (Doesn't take into account cost or space). */
  public Map<String, Building> getBuildables();
}
