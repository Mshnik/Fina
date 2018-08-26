package model.unit.combatant;

import model.unit.Unit;
import model.unit.building.Building;
import model.unit.commander.Bhen;
import model.unit.commander.Commander;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.Modifiers;

import java.util.Random;

/**
 * A class that describes a prospective combat between two given units and can process it. Used to
 * handle combat logic in addition to record logistic data and paint informational data for the
 * user.
 *
 * @author Mshnik
 */
public final class Combat {

  /**
   * Percentage bonus in attack and defense a combatant gets against another when it has a class
   * bonus. The bonus is stacked for each level of bonus.
   */
  private static final double COMBAT_CLASS_BONUS = 0.2;

  /** The Combatant that will attack. */
  public final Combatant attacker;

  /** The defending unit. If it is a combatant, it may counterattack. */
  public final Unit defender;

  /** Number of squares between attacker and defender's locations, accounting for melee. */
  private final int dist;

  /** True after process() is called, to prevent double counting combat. */
  private boolean processed;

  /**
   * Creates a new combat on the given attacker and defender. Does not process combat until process
   * is called.
   */
  public Combat(Combatant attacker, Unit defender) {
    this.attacker = attacker;
    this.defender = defender;

    // Account for melee = 0 range
    dist = attacker.getLocation().manhattanDistance(defender.getLocation()) - 1;
  }

  /**
   * Returns the class bonus for the attacker (the defender is the opposite). If the defender is not
   * a combatant, this will always be 0.
   */
  public int getClassBonus() {
    if (defender instanceof Combatant) {
      return Combatant.CombatantClass.getBonusLevel(
          attacker.combatantClasses, ((Combatant) defender).combatantClasses);
    } else {
      return 0;
    }
  }

  /** Returns true if this combat is ranged - if the two combatants are not adjacent. */
  private boolean isRanged() {
    return attacker.getLocation().directionTo(defender.getLocation()) == null;
  }

  /**
   * Returns the sum of all type bonus ratio modifiers for the given source attacking the given
   * target.
   */
  private static double getTypeBonusRatio(Unit source, Unit target) {
    Modifier m;
    if (target instanceof Building) {
      m = Modifiers.siege(0);
    } else if (target instanceof Commander) {
      m = Modifiers.bloodlust(0);
    } else {
      return 1;
    }
    return 1
        + source
            .getModifiersByName(m)
            .stream()
            .mapToDouble(mod -> ((CustomModifier) mod).val.doubleValue())
            .sum();
  }

  /**
   * Returns the minimum damage the attacker could do after scaling by combat classes and account
   * for modifiers.
   */
  public int getMinAttack() {
    double classBonus = 1 + COMBAT_CLASS_BONUS * getClassBonus();
    return Math.max(
        0,
        (int) (attacker.getMinAttackScaled() * classBonus * getTypeBonusRatio(attacker, defender)));
  }

  /**
   * Returns the maximum damage the attacker could do after scaling by combat classes and account
   * for modifiers.
   */
  public int getMaxAttack() {
    double classBonus = 1 + COMBAT_CLASS_BONUS * getClassBonus();
    return Math.max(
        0,
        (int) (attacker.getMaxAttackScaled() * classBonus * getTypeBonusRatio(attacker, defender)));
  }

  /** Returns the sum of all damage reduction for the attacker. */
  public int getAttackerDamageReduction() {
    return attacker
            .getModifiersByName(Modifiers.toughness(0))
            .stream()
            .mapToInt(m -> ((CustomModifier) m).val.intValue())
            .sum()
        + attacker
            .getModifiersByName(isRanged() ? Modifiers.elusive(0) : Modifiers.armored(0))
            .stream()
            .mapToInt(m -> ((CustomModifier) m).val.intValue())
            .sum();
  }

  /** Returns true iff the defender could counterattack if it has health left after the attack. */
  public boolean defenderCouldCounterAttack() {
    return defender.isAlive()
        && defender.owner.canSee(attacker)
        && dist >= defender.getMinAttackRange()
        && dist <= defender.getMaxAttackRange()
        && defender instanceof Combatant;
  }

  /**
   * Returns the minimum damage the defender could do after scaling by combat classes and account
   * for modifiers.
   */
  private int getMinCounterAttack() {
    double classBonus = 1 - (COMBAT_CLASS_BONUS * getClassBonus());
    return Math.max(
        0,
        (int)
            (defender.getMinAttackScaled()
                * classBonus
                * getTypeBonusRatio(defender, attacker)
                * getDefenderCounterAttackBonusRatio()));
  }

  /**
   * Returns the maximum damage the defender could do after scaling by combat classes and account
   * for modifiers.
   */
  private int getMaxCounterAttack() {
    double classBonus = 1 - (COMBAT_CLASS_BONUS * getClassBonus());
    return Math.max(
        0,
        (int)
            (defender.getMaxAttackScaled()
                * classBonus
                * getTypeBonusRatio(defender, attacker)
                * getDefenderCounterAttackBonusRatio()));
  }

  /** Returns the sum of all damage reduction for the defender. */
  public int getDefenderDamageReduction() {
    return defender
            .getModifiersByName(Modifiers.toughness(0))
            .stream()
            .mapToInt(m -> ((CustomModifier) m).val.intValue())
            .sum()
        + defender
            .getModifiersByName(isRanged() ? Modifiers.elusive(0) : Modifiers.armored(0))
            .stream()
            .mapToInt(m -> ((CustomModifier) m).val.intValue())
            .sum();
  }

  /** Returns the sum of all counterattack damage boosting modifiers for the defender. */
  public double getDefenderCounterAttackBonusRatio() {
    return 1
        + defender
            .getModifiersByName(Modifiers.patience(0))
            .stream()
            .mapToDouble(m -> ((CustomModifier) m).val.doubleValue())
            .sum();
  }

  /**
   * Returns the projected minimum damage the defender could do after scaling by combat classes and
   * account for modifiers along with change in health Should only be used for projections, not
   * actual combat.
   */
  public int getProjectedMinCounterAttack() {
    if (!defenderCouldCounterAttack()) {
      return 0;
    }
    double minProjectedHealthPercentage =
        ((double) defender.getHealth() + getDefenderDamageReduction() - getMaxAttack())
            / defender.getMaxHealth();
    return (int)
        (defender.getMinAttack()
            * minProjectedHealthPercentage
            * (1 - COMBAT_CLASS_BONUS * getClassBonus())
            * getDefenderCounterAttackBonusRatio());
  }

  /**
   * Returns the maximum damage the defender could do after scaling by combat classes and account
   * for modifiers along with change in health. Should only be used for projections, not actual
   * combat.
   */
  public int getProjectedMaxCounterAttack() {
    if (!defenderCouldCounterAttack()) {
      return 0;
    }
    double maxProjectedHealthPercentage =
        ((double) defender.getHealth() + getDefenderDamageReduction() - getMinAttack())
            / defender.getMaxHealth();
    return (int)
        (defender.getMaxAttack()
            * maxProjectedHealthPercentage
            * (1 - COMBAT_CLASS_BONUS * getClassBonus())
            * getDefenderCounterAttackBonusRatio());
  }

  /**
   * Causes this unit to fight the given unit. With this as the attacker and defender as the
   * defender. This will cause the health of the defender to change
   *
   * @throws RuntimeException if... - this is dead - this can't attack currently
   * @throws IllegalArgumentException for invalid fight when... - defender is dead - both units
   *     belong to the same player - defender is out of the range of this - this' owner can't see
   *     defender
   * @return true iff defender is killed because of this action
   */
  public final boolean process(Random random) throws IllegalArgumentException, RuntimeException {
    if (processed)
      throw new RuntimeException("Can't process " + this + "process() already called.");
    if (!attacker.isAlive()) throw new RuntimeException(attacker + " can't fight, it is dead.");
    if (!defender.isAlive())
      throw new IllegalArgumentException(defender + " can't fight, it is dead.");
    if (attacker.owner == defender.owner)
      throw new IllegalArgumentException(
          attacker + " can't fight " + defender + ", they both belong to " + attacker.owner);
    if (!attacker.getCanFight())
      throw new RuntimeException(attacker + " can't fight again this turn");
    if (!attacker.owner.canSee(defender))
      throw new IllegalArgumentException(attacker.owner + " can't see " + defender);
    if (dist < attacker.getMinAttackRange())
      throw new IllegalArgumentException(this + " can't fight " + defender + ", it is too close.");
    if (dist > attacker.getMaxAttackRange())
      throw new IllegalArgumentException(
          this + " can't fight " + defender + ", it is too far away.");

    System.out.println("Start combat ------");

    // Get damage in range [min,max]
    int damage = random.nextInt(getMaxAttack() + 1 - getMinAttack()) + getMinAttack();
    System.out.println("damage: " + damage);

    // True if a counterAttack is happening, false otherwise.
    boolean counterAttack = defenderCouldCounterAttack() && damage < defender.getHealth();

    attacker.preFight(defender);
    if (counterAttack) defender.preCounterFight(attacker);

    // attacker attacks defender, less damage reduction.
    defender.changeHealth(Math.min(0, getDefenderDamageReduction() - damage), attacker);

    // If defender is still alive, can see the first unit,
    // and this is within range, defender counterattacks
    int counterAttackDamage = 0;
    if (counterAttack) {
      // Get damage in range [min,max]
      counterAttackDamage =
          random.nextInt(getMaxCounterAttack() + 1 - getMinCounterAttack()) + getMinCounterAttack();
      System.out.println("Counter damage: " + counterAttackDamage);

      // Change this unit's health
      attacker.changeHealth(
          Math.min(0, getAttackerDamageReduction() - counterAttackDamage), defender);
      counterAttack = true;
    }

    // This can't attack this turn again
    attacker.setCanFight(false);

    // Calls postFight on units that are still alive, able to counterAttack
    if (attacker.isAlive()) {
      attacker.postFight(damage, defender, counterAttackDamage);
    }
    if (defender.isAlive() && counterAttack) {
      defender.postCounterFight(counterAttackDamage, attacker, damage);
    }

    boolean defenderIsDead = !defender.isAlive();

    // Check for killing modifiers
    Modifier mod1 = attacker.getModifierByName(Bhen.ABILITY_NAMES[0][0]);
    if (mod1 != null) {
      attacker.addMovement(((CustomModifier) mod1).val.intValue());
    }

    processed = true;
    System.out.println("End combat ------");

    return defenderIsDead;
  }
}
