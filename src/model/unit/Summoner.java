package model.unit;

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

  /** Can summon returns true for summoners, this may force an override. */
  public default boolean canSummon() {
    return true;
  }
}
