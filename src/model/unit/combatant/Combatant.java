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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a moving and fighting unit
 *
 * @author MPatashnik
 */
public abstract class Combatant extends MovingUnit {

  /** Possible combatant classes. A combatant may have more than one. */
  public enum CombatantClass {
    // Mid health mid-high damage standard melee
    FIGHTER,
    // Mid health mid damage mid range
    RANGER,
    // Low health high damage, counter range
    ASSASSIN,
    // High health mid damage, counter melee
    TANK,
    // Low health high damage high range, counter ranger + tank
    MAGE;

    /** Returns the CombatantClass from the given short string. */
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

    /** Returns a mid-length string representation of this. */
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

    /** Returns true iff this class has a bonus against the given other class. */
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

  /** This combatant's classes. Will have length >= 1. */
  public final List<CombatantClass> combatantClasses;

  /** true iff this can still fight this turn. Has an impact on how to draw this */
  private boolean canFight;

  /**
   * Constructor for Combatant. Also adds this unit to the tile it is on as an occupant, and its
   * owner as a unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana.
   *
   * @param owner - the player owner of this unit
   * @param name - the name of this unit
   * @param imageFilename - the image to draw when drawing this unit.
   * @param level - the level of this unit - the age this belongs to
   * @param manaCost - the cost of summoning this unit. Should be a positive number.
   * @param stats - the base unmodified stats of this unit.
   */
  public Combatant(
      Player owner,
      String name,
      String imageFilename,
      int level,
      List<CombatantClass> classes,
      int manaCost,
      Stats stats)
      throws RuntimeException, IllegalArgumentException {
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
  }

  /** Sets whether this unit can still attack. */
  public void setCanFight(boolean canFight) {
    this.canFight = canFight;
  }

  /** Returns whether this unit can still attack. */
  public boolean getCanFight() {
    return canFight;
  }

  /** Combatants are ok with any kind of modifier except summon range */
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
  /** Returns iff this can still fight this turn */
  public boolean canFight() {
    return canFight;
  }

  /** Returns true iff there is at least one enemy unit within range and sight */
  public boolean hasFightableTarget() {
    List<Tile> tiles =
        ExpandableCloud.create(ExpandableCloud.ExpandableCloudType.CIRCLE, getMaxAttackRange() + 1)
            .difference(
                ExpandableCloud.create(
                    ExpandableCloud.ExpandableCloudType.CIRCLE, getMinAttackRange()))
            .translate(getLocation().getPoint())
            .toTileSet(owner.game.board);
    for (Tile t : tiles) {
      if (t.isOccupied()) {
        Unit u = t.getOccupyingUnit();
        if (u.owner != owner && owner.canSee(u)) return true;
      }
    }
    return false;
  }

  @Override
  public void preMove(LinkedList<Tile> path) {}

  @Override
  public void postMove(LinkedList<Tile> path) {}

  @Override
  public void preCounterFight(Combatant other) {}

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
  public void preFight(Unit other) {}

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
  }

  /** Returns Combatant */
  @Override
  public String getIdentifierString() {
    return "Combatant";
  }
}
