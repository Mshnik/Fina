package model.unit.combatant;

import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.Modifiers;
import model.unit.modifier.StatModifier;
import model.unit.stat.StatType;
import model.unit.stat.Stats;
import model.util.ExpandableCloud;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a moving and fighting unit
 *
 * @author MPatashnik
 */
public abstract class Combatant extends MovingUnit {

  /**
   * Possible combatant classes. A combatant may have more than one.
   */
  public enum CombatantClass {
    // Mid health mid-high damage standard melee
    FIGHTER,
    // Low health high damage, counter range
    ASSASSIN,
    // Low health high damage high range, counter ranger + tank
    MAGE,
    // Mid health mid damage mid range
    RANGER,
    // High health mid damage, counter melee
    TANK;

    /**
     * Returns the CombatantClass from the given short string.
     */
    public static CombatantClass valueOfShort(String s) {
      switch (s.toUpperCase()) {
        case "F":
          return FIGHTER;
        case "R":
          return RANGER;
        case "A":
          return ASSASSIN;
        case "T":
          return TANK;
        case "M":
          return MAGE;
        default:
          throw new RuntimeException("Unknown short string " + s);
      }
    }

    /**
     * Returns a mid-length string representation of this.
     */
    public String toMidString() {
      switch (this) {
        case FIGHTER:
          return "Ftr";
        case RANGER:
          return "Rng";
        case ASSASSIN:
          return "Asn";
        case TANK:
          return "Tnk";
        case MAGE:
          return "Mag";
        default:
          throw new RuntimeException();
      }
    }

    /**
     * Returns true iff this class has a bonus against the given other class.
     */
    public boolean hasBonusAgainstClass(CombatantClass other) {
      switch (this) {
        case FIGHTER:
          return other == ASSASSIN || other == MAGE;
        case RANGER:
          return other == FIGHTER || other == TANK;
        case ASSASSIN:
          return other == MAGE || other == RANGER;
        case TANK:
          return other == FIGHTER || other == ASSASSIN;
        case MAGE:
          return other == RANGER || other == TANK;
      }
      return false;
    }

    /**
     * Returns the number of classes in classes that have a bonus against a class in other, minus
     * the number of classes in other that have a bonus against a class in classes.
     */
    public static int getBonusLevel(
        List<CombatantClass> classes, List<CombatantClass> opposingClasses) {
      int bonusLevel = 0;
      for (CombatantClass c : classes) {
        for (CombatantClass opposingClass : opposingClasses) {
          if (c.hasBonusAgainstClass(opposingClass)) {
            bonusLevel++;
          } else if (opposingClass.hasBonusAgainstClass(c)) {
            bonusLevel--;
          }
        }
      }
      return bonusLevel;
    }
  }

  /**
   * This combatant's classes. Will have length >= 1.
   */
  public final List<CombatantClass> combatantClasses;

  /**
   * true iff this can still fight this turn. Has an impact on how to draw this
   */
  private boolean canFight;

  /**
   * Constructor for Combatant. Also adds this unit to the tile it is on as an occupant, and its
   * owner as a unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana.
   *
   * @param owner         - the player owner of this unit
   * @param name          - the name of this unit
   * @param imageFilename - the image to draw when drawing this unit.
   * @param level         - the level of this unit - the age this belongs to
   * @param manaCost      - the cost of summoning this unit. Should be a positive number.
   * @param stats         - the base unmodified stats of this unit.
   */
  public Combatant(
      Player owner,
      String name,
      String imageFilename,
      int level,
      List<CombatantClass> classes,
      int manaCost,
      Stats stats)
      throws RuntimeException {
    super(owner, name, imageFilename, level, manaCost, stats);

    combatantClasses = Collections.unmodifiableList(classes);

    if ((int) stats.getStat(StatType.MIN_ATTACK) <= 0
        || (int) stats.getStat(StatType.MAX_ATTACK) <= 0)
      throw new IllegalArgumentException("Combatant " + this + " can't have non-positive attack.");
  }

  /**
   * Call at the beginning of every turn. Can be overridden in subclasses, but those classes should
   * call the super version before doing their own additions. - ticks down modifiers and
   * re-calculates stats, if necessary. - refreshes canMove and canFight
   */
  @Override
  public void refreshForTurn() {
    super.refreshForTurn();
    canFight = true;
    if (owner != null) {
      owner.recomputeDangerRadiusFor(this);
    }
  }

  /**
   * Sets whether this unit can still attack.
   */
  public void setCanFight(boolean canFight) {
    this.canFight = canFight;
  }

  /**
   * Returns whether this unit can still attack.
   */
  public boolean getCanFight() {
    return canFight;
  }

  /**
   * All modifiers are visible.
   */
  public List<Modifier> getVisibleModifiers() {
    return getModifiers();
  }

  /**
   * Combatants are ok with any kind of modifier except summon range
   */
  @Override
  public boolean modifierOk(Modifier m) {
    if (m instanceof StatModifier) {
      return ((StatModifier) m).modifiedStat != StatType.SUMMON_RANGE;
    }
    if (m instanceof CustomModifier) {
      return ((CustomModifier) m).appliesToCombatants;
    }
    return false;
  }

  // MOVEMENT

  /**
   * Non-assassin Combatants can only move if they can still attack. Attacking means unit loses
   * movement. Assassins can move after attacking.
   */
  @Override
  public int getMovement() {
    if (hasModifierByName(Modifiers.disappearance())) {
      return super.getMovement();
    } else {
      return canFight() ? super.getMovement() : 0;
    }
  }

  // FIGHTING

  /**
   * Returns iff this can still fight this turn
   */
  public boolean canFight() {
    return canFight;
  }

  /**
   * Combatants can't summon.
   */
  @Override
  public boolean canSummon() {
    return false;
  }

  /**
   * Combatants can't cast.
   */
  @Override
  public boolean canCast() {
    return false;
  }

  /**
   * Returns the list of tiles this can attack from the given tile.
   */
  public List<Tile> getAttackableTilesFrom(Tile tile) {
    return ExpandableCloud.create(
        ExpandableCloud.ExpandableCloudType.CIRCLE, getMaxAttackRange() + 1)
        .difference(
            ExpandableCloud.create(ExpandableCloud.ExpandableCloudType.CIRCLE, getMinAttackRange()))
        .translate(tile.getPoint())
        .toTileSet(owner.game.board);
  }

  /**
   * Returns the list of tiles this can attack, given its current location. If filterForValidTargets
   * is true, filters for tiles that contain an enemy unit that owner can see.
   */
  public List<Tile> getAttackableTiles(boolean filterForValidTargets) {
    List<Tile> possibleTiles = getAttackableTilesFrom(getLocation());
    if (filterForValidTargets) {
      return possibleTiles
          .stream()
          .filter(t -> owner.canSee(t) && t.isOccupied() && t.getOccupyingUnit().owner != owner)
          .collect(Collectors.toList());
    } else {
      return possibleTiles;
    }
  }

  /**
   * Returns true iff there is at least one enemy unit within range and sight
   */
  public boolean hasFightableTarget() {
    return !getAttackableTiles(true).isEmpty();
  }

  /**
   * Returns the danger radius of this combatant - the union of all attackable tiles from all
   * movable tiles. If useMaxMovement, uses max movement instead of current movement.
   */
  public Set<Tile> getDangerRadius(boolean useMaxMovement) {
    return getDangerRadiusFromTile(getLocation(), useMaxMovement);
  }

  /**
   * Returns the danger radius of this combatant from the given tile - the union of all attackable
   * tiles from all movable tiles from the given tile. If useMaxMovement, uses max movement instead
   * of current movement.
   */
  public Set<Tile> getDangerRadiusFromTile(Tile tile, boolean useMaxMovement) {
    return owner
        .game
        .board
        .getMovementCloud(this, tile, useMaxMovement)
        .stream()
        .map(this::getAttackableTilesFrom)
        .flatMap(List::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public void preMove(List<Tile> path) {
  }

  /**
   * Called after moving - tell the frame that danger clouds including this need to be recomputed.
   */
  @Override
  public void postMove(List<Tile> path) {
    if (path.size() > 0 && owner != null) {
      owner.recomputeDangerRadiusFor(this);
      owner.maybeRemoveActionableUnit(this);
    }
  }

  @Override
  public void preCounterFight(Combatant other) {
  }

  @Override
  public void postCounterFight(int damageDealt, Combatant other, int damageTaken) {
    if (damageDealt > 0) {
      for (Modifier m : getModifiersByName(Modifiers.bornToFight(0))) {
        CustomModifier customModifier = (CustomModifier) m;
        changeHealth(customModifier.val.intValue(), this);
      }
    }
  }

  /**
   * Processes a pre-fight action that may be caused by modifiers. Still only called when the fight
   * is valid.
   */
  public void preFight(Unit other) {
  }

  /**
   * Processes a post-fight action that may be caused by modifiers. Only called when the fight is
   * valid and this is still alive.
   */
  public void postFight(int damageDealt, Unit other, int damageTaken) {
    if (damageDealt > 0) {
      for (Modifier m : getModifiersByName(Modifiers.bornToFight(0))) {
        CustomModifier customModifier = (CustomModifier) m;
        changeHealth(customModifier.val.intValue(), this);
      }
    }
    if (owner != null) {
      owner.recomputeDangerRadiusFor(this);
      owner.maybeRemoveActionableUnit(this);
    }
  }

  /**
   * When a non-dummy combatant's stats are changed, refresh the danger cloud if this is in one.
   */
  @Override
  protected void refreshStats() {
    super.refreshStats();
    if (owner != null) {
      owner.recomputeDangerRadiusFor(this);
    }
  }

  /**
   * Returns Combatant
   */
  @Override
  public String getIdentifierString() {
    return "Combatant";
  }
}
