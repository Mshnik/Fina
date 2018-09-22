package model.unit.ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.board.Direction;
import model.board.Tile;
import model.game.Player;
import model.game.Stringable;
import model.unit.Unit;
import model.unit.building.Building;
import model.unit.building.PlayerModifierBuilding;
import model.unit.building.PlayerModifierBuilding.PlayerModifierEffectType;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.util.Cloud;
import model.util.ExpandableCloud;
import model.util.ExpandableCloud.ExpandableCloudType;

/** Parent class of abilities useable by Commanders. */
public class Ability implements Stringable {

  /** The name of this ability */
  public final String name;

  /** Types of abilities. */
  public enum AbilityType {
    ATTACK,
    HEAL,
    BUFF,
    UTILITY;
  }

  /** The type of this Ability. */
  public final AbilityType abilityType;

  /** The level of the ability */
  public final int level;

  /** The cost of casting this Ability */
  public final int manaCost;

  /**
   * The effect range of this Ability, as a collection of locations relative to its origin location
   */
  public final ExpandableCloud effectCloud;

  /** True if cloud boosting effects increase the size of this ability's cloud. */
  public final boolean canBeCloudBoosted;

  /**
   * Distance from the commander this can be cast. 0 for requiring commander as origin. If > 0,
   * can't pick tile commander is on.
   */
  public final int castDist;

  /** Types of units this ability should be applied to. */
  public final List<Class<? extends Unit>> affectedUnitTypes;

  /** True iff modifiers should be applied to allied units */
  public final boolean appliesToAllied;

  /** True iff modifiers should be applied to enemy units */
  public final boolean appliesToFoe;

  /** A string description of this ability, used for debugging and UI. */
  public final String description;

  /** A list of AbilityEffects to apply to each affected unit. */
  public final List<AbilityEffect> effects;

  /**
   * Ability Constructor
   *
   * @param name - the Name of this ability
   * @param abilityType - the Type of this ability.
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
  Ability(
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
    this.name = name;
    this.abilityType = abilityType;
    this.level = level;
    this.effectCloud = effectCloud;
    this.canBeCloudBoosted = canBeCloudBoosted;
    this.affectedUnitTypes = affectedUnitTypes;
    this.appliesToAllied = appliesToAllied;
    this.appliesToFoe = appliesToFoe;
    this.manaCost = manaCost;
    this.castDist = castDist;
    this.description = description;
    this.effects = effects;
  }

  /** Returns the mana cost of casting this ability minus the discounts for the given player. */
  public int getManaCostWithDiscountsForPlayer(Player p) {
    return (int)
        (manaCost
            * (1
                - p.getUnits()
                        .stream()
                        .filter(u -> u instanceof PlayerModifierBuilding)
                        .flatMap(u -> ((PlayerModifierBuilding) u).getEffect().stream())
                        .filter(e -> e.effectType == PlayerModifierEffectType.CAST_DISCOUNT)
                        .mapToInt(e -> e.value)
                        .sum()
                    / 100.0));
  }

  /**
   * Returns true iff this ability would affect the given unit. Doesn't check location, only type
   * and ownership.
   */
  public boolean wouldAffect(Unit u, Commander caster) {
    return (u instanceof Combatant && affectedUnitTypes.contains(Combatant.class)
            || u instanceof Commander && affectedUnitTypes.contains(Commander.class)
            || u instanceof Building && affectedUnitTypes.contains(Building.class))
        && (u.owner == caster.owner && appliesToAllied || u.owner != caster.owner && appliesToFoe);
  }

  /** Returns the effect cloud translated for the given location and caster location. */
  public List<Tile> getTranslatedEffectCloud(Commander caster, Tile castLocation, int boostLevel) {
    Cloud boostedCloud = effectCloud.expand(canBeCloudBoosted ? boostLevel : 0);

    // Check for rotatable types - if need be, rotate.
    if (effectCloud.type == ExpandableCloudType.CONE
        || effectCloud.type == ExpandableCloudType.WALL) {
      Direction d = caster.getLocation().directionTo(castLocation);
      if (d != null) {
        switch (d) {
          case LEFT:
            boostedCloud = boostedCloud.rotate(false);
            boostedCloud = boostedCloud.rotate(false);
            break;
          case UP:
            boostedCloud = boostedCloud.rotate(false);
            break;
          case RIGHT:
            // Already in correct orientation.
            break;
          case DOWN:
            boostedCloud = boostedCloud.rotate(true);
            break;
        }
      }
    }
    return boostedCloud.translate(castLocation.getPoint()).toTileSet(caster.owner.game.board);
  }

  /**
   * Returns a double that represents how effective the given ability is on the given unit by
   * applying {@link AbilityEffect#getEffectivenessPercentage(Unit)} for each effect on the given
   * unit, then averaging. Should be overridden if Ability is overridden to do custom math, but
   * should return in [0,1].
   */
  public double getEffectivenessOn(Unit unit) {
    return effects.stream().mapToDouble(effect -> effect.getEffectivenessPercentage(unit)).sum()
        / effects.size();
  }

  /**
   * Casts this ability. Returns true if this call is ok and cast happens, throws exception
   * otherwise. If this is passive, always cast with the commander's location as its location.
   * Returns the list of units this affected.
   */
  public List<Unit> cast(Commander caster, Tile location, int boostLevel, Random random)
      throws RuntimeException {
    // Check mana
    if (caster.getMana() < manaCost) {
      throw new RuntimeException("Can't Cast " + this + "; OOM");
    }

    // Check location - if cast dist is 0, expect location to be caster's location.
    // Otherwise, check that tile is in correct range for cast dist.
    // Also check dist == 1 for cone type in order for rotation to work.
    if (castDist == 0 && !location.getPoint().equals(caster.getLocation().getPoint())) {
      throw new RuntimeException(
          "Can't Cast " + this + " at " + location + "; must be on commander's location");
    } else if (location.manhattanDistance(caster.getLocation())
        > castDist + caster.owner.getCastSelectBoost()) {
      throw new RuntimeException(
          "Can't Cast " + this + " at " + location + "; too far away from commander");
    }
    if (castDist != 1
        && (effectCloud.type == ExpandableCloudType.CONE
            || effectCloud.type == ExpandableCloudType.WALL)) {
      throw new RuntimeException("Cone and Wall types must be cast at distance one");
    }

    // Ok. Cast.
    // Compute cloud with boost and rotation.
    List<Tile> affectedTiles = getTranslatedEffectCloud(caster, location, boostLevel);

    // Subtract mana cost from commander.
    caster.addMana(-manaCost);

    List<Unit> affectedUnits = new ArrayList<>();

    // Go through all tiles in affected tiles and affect units as need be.
    for (Tile t : affectedTiles) {
      if (!t.isOccupied()) continue;

      Unit u = t.getOccupyingUnit();
      if (wouldAffect(u, caster)) {
        affectedUnits.add(u);
        for (AbilityEffect effect : effects) {
          effect.affect(u, caster, random);
        }
        u.owner.refreshVisionCloud(u);
      }
    }
    return Collections.unmodifiableList(affectedUnits);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public String toStringShort() {
    return name + ":(" + manaCost + ")";
  }

  @Override
  public String toStringLong() {
    return toStringShort() + ": " + description;
  }

  @Override
  public String toStringFull() {
    return toStringLong();
  }
}
