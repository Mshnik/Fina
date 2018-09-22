package model.unit.ability;

import java.util.List;
import java.util.Random;
import model.board.Tile;
import model.unit.Unit;
import model.unit.commander.Commander;
import model.util.ExpandableCloud;

/**
 * A special ability that causes the caster to gain mana.
 *
 * @author Mshnik
 */
final class Sacrifice extends Ability {

  /** The name of Sacrifice abilities. */
  static final String NAME = "Sacrifice";

  /** The ratio of unit cost to mana gain. */
  private static final double MANA_GAIN_RATIO = .25;

  /**
   * Ability Constructor
   *
   * @param name - the name of this ability.
   * @param abilityType - the type of this ability.
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
   * @param abilityEffects - the effects this ability causes.
   */
  Sacrifice(
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
      List<AbilityEffect> abilityEffects) {
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
        abilityEffects);
  }

  @Override
  public double getEffectivenessOn(Unit u) {
    return super.getEffectivenessOn(u);
  }

  @Override
  public List<Unit> cast(Commander caster, Tile location, int boostLevel, Random random) {
    List<Unit> affectedUnits = super.cast(caster, location, boostLevel, random);
    for (Unit u : affectedUnits) {
      caster.addMana((int) (u.getManaCost() * MANA_GAIN_RATIO));
    }
    return affectedUnits;
  }
}
