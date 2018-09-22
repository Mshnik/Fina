package model.unit.ability;

import java.util.List;
import java.util.Random;
import model.board.Tile;
import model.unit.Unit;
import model.unit.commander.Commander;
import model.util.ExpandableCloud;

/**
 * A special ability that causes the caster and the target to swap locations.
 *
 * @author Mshnik
 */
public final class SpacialShift extends Ability {

  /** The name of SpacialShift abilities. */
  static final String NAME = "Spacial Shift";

  /**
   * Ability Constructor
   *
   * @param name - the Name of this ability
   * @param abilityType - the type of this ability
   * @param level - the level of the ability.
   * @param manaCost - the mana cost of using this ability. 0 if passive
   * @param effectCloud - the cloud of tiles this ability effects.
   * @param canBeCloudBoosted - true if this abilty's cloud can be increased in size by cloud
   *     boosting effects, false if not.
   * @param castDist - the distance from the commander this ability can be cast
   * @param affectedUnitTypes - types of units this ability effects. Units with other types will not
   *     be effected by this ability.
   * @param appliesToAllied - true iff this ability can affect allied units
   * @param appliesToFoe - true iff this ability can affect non-allied units
   * @param description - a string description of this ability.
   * @param effects - the effects of this ability to apply to each unit.
   */
  SpacialShift(
      String name,
      AbilityType abilityType,
      int level,
      int manaCost,
      ExpandableCloud effectCloud,
      boolean canBeCloudBoosted,
      int castDist,
      List<Class<? extends Unit>> affectedUnitTypes,
      boolean appliesToAllied,
      boolean appliesToFoe,
      String description,
      List<AbilityEffect> effects) {
    super(
        name,
        abilityType,
        level,
        manaCost,
        effectCloud,
        canBeCloudBoosted,
        castDist,
        affectedUnitTypes,
        appliesToAllied,
        appliesToFoe,
        description,
        effects);
  }

  @Override
  public double getEffectivenessOn(Unit u) {
    return super.getEffectivenessOn(u);
  }

  @Override
  public List<Unit> cast(Commander caster, Tile location, int boostLevel, Random random) {
    List<Unit> list = super.cast(caster, location, boostLevel, random);

    if (list.size() != 1) {
      throw new RuntimeException("Can't cast SpacialShift targeting more than one unit");
    }
    Unit otherUnit = list.get(0);

    Tile casterLocation = caster.getLocation();
    Tile otherUnitLocation = otherUnit.getLocation();

    casterLocation.removeOccupyingUnit();
    otherUnitLocation.moveUnitTo(casterLocation);
    otherUnitLocation.addOccupyingUnit(caster);

    caster.setLocation(otherUnitLocation);
    otherUnit.setLocation(casterLocation);

    caster.owner.refreshVisionCloud(caster);

    return list;
  }
}
