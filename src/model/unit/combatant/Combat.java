package model.unit.combatant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import model.unit.Unit;
import model.unit.building.Building;
import model.unit.combatant.Combatant.CombatantClass;
import model.unit.commander.Commander;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.Modifiers;
import view.gui.panel.GamePanel;

/**
 * A class that describes a prospective combat between two given units and can process it. Used to
 * handle combat logic in addition to record logistic data and paint informational data for the
 * user.
 *
 * @author Mshnik
 */
public final class Combat {

  /**
   * True iff debug info should be logged to the console.
   */
  private static final boolean DEBUG = false;

  /**
   * Percentage bonus in attack and defense a combatant gets against another when it has a class
   * bonus. The bonus is stacked for each level of bonus.
   */
  private static final double COMBAT_CLASS_BONUS = 0.2;

  /**
   * The Combatant that will attack.
   */
  public final Combatant attacker;

  /**
   * The defending unit. If it is a combatant, it may counterattack.
   */
  public final Unit defender;

  /**
   * Number of squares between attacker and defender's locations, accounting for melee.
   */
  private final int dist;

  /**
   * The current {@link Stage} this is on.
   */
  private Stage stage;

  /**
   * The stages a Combat can be on.
   */
  public enum Stage {
    NOT_YET_STARTED,
    STARTED,
    PRE_FIGHTS_COMPLETED,
    ATTACK_COMPLETED,
    COUNTER_ATTACK_COMPLETED,
    COUNTER_ATTACK_SKIPPED,
    POST_ATTACK_COMPLETED,
    COMBAT_COMPLETED
  }

  /**
   * A pair of combatant classes.
   */
  public static final class CombatantClassPair {
    public final CombatantClass first;
    public final CombatantClass second;

    private CombatantClassPair(CombatantClass first, CombatantClass second) {
      this.first = first;
      this.second = second;
    }

    /**
     * Returns true iff first beats second, false otherwise.
     */
    public boolean firstBeatsSecond() {
      return first.hasBonusAgainstClass(second);
    }
  }

  /**
   * Creates a new combat on the given attacker and defender. Does not process combat until process
   * is called.
   */
  public Combat(Combatant attacker, Unit defender) {
    this.attacker = attacker;
    this.defender = defender;

    // Account for melee = 0 range
    dist = attacker.getLocation().manhattanDistance(defender.getLocation()) - 1;
    stage = Stage.NOT_YET_STARTED;
  }

  /**
   * Returns the current stage this combat is on.
   */
  public Stage getStage() {
    return stage;
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

  /**
   * Returns a list of all pairs of combatant classes that create the class bonus. Pairs that have
   * no impact are left off.
   */
  public List<CombatantClassPair> getRelevantClassPairs() {
    if (!(defender instanceof Combatant)) {
      return Collections.emptyList();
    }

    ArrayList<CombatantClassPair> list = new ArrayList<>();
    Combatant combatantDefender = (Combatant) defender;
    for (CombatantClass attackerClass : attacker.combatantClasses) {
      for (CombatantClass defenderClass : combatantDefender.combatantClasses) {
        if (attackerClass.hasBonusAgainstClass(defenderClass)
            || defenderClass.hasBonusAgainstClass(attackerClass)) {
          list.add(new CombatantClassPair(attackerClass, defenderClass));
        }
      }
    }
    return list;
  }

  /**
   * Returns true if this combat is ranged - if the two combatants are not adjacent.
   */
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
  private int getMinAttack() {
    double classBonus = 1 + COMBAT_CLASS_BONUS * getClassBonus();
    return Math.max(
        0,
        (int) (attacker.getMinAttackScaled() * classBonus * getTypeBonusRatio(attacker, defender)));
  }

  /**
   * Returns the projected minimum damage the attacker could do after scaling by combat classes and
   * account for modifiers. Should only be used for projections, not actual combat.
   */
  public int getProjectedMinAttack() {
    return (int)
        Math.max(
            0,
            getMinAttack() * Math.max(0, 1 - getDefenderPercentageDamageReduction())
                - getDefenderFlatDamageReduction());
  }

  /**
   * Returns the minimum percent damage the attacker could do after scaling by combat classes and
   * account for modifiers.
   */
  public double getProjectedMinAttackPercent() {
    return (double) getProjectedMinAttack() / defender.getMaxHealth();
  }

  /**
   * Returns the maximum damage the attacker could do after scaling by combat classes and account
   * for modifiers.
   */
  private int getMaxAttack() {
    double classBonus = 1 + COMBAT_CLASS_BONUS * getClassBonus();
    return Math.max(
        0,
        (int) (attacker.getMaxAttackScaled() * classBonus * getTypeBonusRatio(attacker, defender)));
  }

  /**
   * Returns the projected maximum damage the attacker could do after scaling by combat classes and
   * account for modifiers. Should only be used for projections, not actual combat.
   */
  public int getProjectedMaxAttack() {
    return (int)
        Math.max(
            0,
            getMaxAttack() * Math.max(0, 1 - getDefenderPercentageDamageReduction())
                - getDefenderFlatDamageReduction());
  }

  /**
   * Returns the maximum percent damage the attacker could do after scaling by combat classes and
   * account for modifiers.
   */
  public double getProjectedMaxAttackPercent() {
    return (double) getMaxAttack() / defender.getMaxHealth();
  }

  /**
   * Returns the sum of all percentage damage reduction for the defender, for the given combat.
   */
  private double getAttackerPercentageDamageReduction() {
    return attacker
        .getModifiersByName(isRanged() ? Modifiers.elusive(0) : Modifiers.armored(0))
        .stream()
        .mapToDouble(m -> ((CustomModifier) m).val.doubleValue())
        .sum();
  }

  /**
   * Returns the sum of all damage reduction for the attacker.
   */
  private int getAttackerFlatDamageReduction() {
    return attacker
        .getModifiersByName(Modifiers.toughness(0))
        .stream()
        .mapToInt(m -> ((CustomModifier) m).val.intValue())
        .sum();
  }

  /**
   * Returns true iff the defender could counterattack if it has health left after the attack.
   */
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

  /**
   * Returns the sum of all percentage damage reduction for the defender, for the given combat.
   */
  private double getDefenderPercentageDamageReduction() {
    double basePercentDamageReduction =
        defender
            .getModifiersByName(isRanged() ? Modifiers.elusive(0) : Modifiers.armored(0))
            .stream()
            .mapToDouble(m -> ((CustomModifier) m).val.doubleValue())
            .sum();
    if (isRanged() && !attacker.hasModifierByName(Modifiers.siege(0))) {
      return basePercentDamageReduction
          + defender
          .getModifiersByName(Modifiers.solid(0))
          .stream()
          .mapToDouble(m -> ((CustomModifier) m).val.doubleValue())
          .sum();
    } else {
      return basePercentDamageReduction;
    }
  }

  /**
   * Returns the sum of all damage reduction for the defender.
   */
  private int getDefenderFlatDamageReduction() {
    return defender
        .getModifiersByName(Modifiers.toughness(0))
        .stream()
        .mapToInt(m -> ((CustomModifier) m).val.intValue())
        .sum();
  }

  /**
   * Returns the sum of all counterattack damage boosting modifiers for the defender.
   */
  private double getDefenderCounterAttackBonusRatio() {
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
        ((double) defender.getHealth() + getDefenderFlatDamageReduction() - getMaxAttack())
            / defender.getMaxHealth();
    return (int)
        (defender.getMinAttack()
            * minProjectedHealthPercentage
            * (1 - COMBAT_CLASS_BONUS * getClassBonus())
            * getDefenderCounterAttackBonusRatio());
  }

  /**
   * Returns the projected minimum percent damage the defender could do after scaling by combat
   * classes and account for modifiers along with change in health Should only be used for
   * projections, not actual combat.
   */
  public double getProjectedMinCounterAttackPercent() {
    return (double) getProjectedMinCounterAttack() / attacker.getMaxHealth();
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
        ((double) defender.getHealth() + getDefenderFlatDamageReduction() - getMinAttack())
            / defender.getMaxHealth();
    return (int)
        (defender.getMaxAttack()
            * maxProjectedHealthPercentage
            * (1 - COMBAT_CLASS_BONUS * getClassBonus())
            * getDefenderCounterAttackBonusRatio());
  }

  /**
   * Returns the projected maximum percent damage the defender could do after scaling by combat
   * classes and account for modifiers along with change in health Should only be used for
   * projections, not actual combat.
   */
  public double getProjectedMaxCounterAttackPercent() {
    return (double) getProjectedMaxCounterAttack() / attacker.getMaxHealth();
  }

  /**
   * Causes this unit to fight the given unit. With this as the attacker and defender as the
   * defender. This will cause the health of the defender to change
   *
   * @return true iff defender is killed because of this action
   * @throws RuntimeException if... - this is dead - this can't attack currently
   * @throws IllegalArgumentException for invalid fight when... - defender is dead - both units
   * belong to the same player - defender is out of the range of this - this' owner can't see
   * defender
   */
  public final boolean process(Random random) throws IllegalArgumentException, RuntimeException {
    if (stage != Stage.NOT_YET_STARTED)
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
    if (!attacker.getAttackableTiles(true).contains(defender.getLocation()))
      throw new IllegalArgumentException(
          this + " can't fight " + defender + ", it isn't in the valid attack range");
    stage = Stage.STARTED;
    if (DEBUG) System.out.println("Start combat ------");

    // Get damage in range [min,max], change with damage reduction.
    int damage = random.nextInt(getMaxAttack() + 1 - getMinAttack()) + getMinAttack();
    if (DEBUG) System.out.println("Raw Attack damage: " + damage);
    int finalDamage =
        (int)
            Math.max(
                0,
                damage * Math.max(0, 1 - getDefenderPercentageDamageReduction())
                    - getDefenderFlatDamageReduction());
    if (DEBUG) System.out.println("Final Attack damage: " + finalDamage);

    // True if a counterAttack is happening, false otherwise.
    boolean counterAttack = defenderCouldCounterAttack() && finalDamage < defender.getHealth();

    attacker.preFight(defender);
    if (counterAttack) {
      defender.preCounterFight(attacker);
    }
    stage = Stage.PRE_FIGHTS_COMPLETED;

    // attacker attacks defender, less damage reduction.
    defender.changeHealth(-finalDamage, attacker);
    stage = Stage.ATTACK_COMPLETED;

    // If defender is still alive, can see the first unit,
    // and this is within range, defender counterattacks
    int counterAttackDamage = 0;
    if (counterAttack) {
      // Get damage in range [min,max]
      counterAttackDamage =
          random.nextInt(getMaxCounterAttack() + 1 - getMinCounterAttack()) + getMinCounterAttack();
      if (DEBUG) {
        System.out.println("Raw Counter damage: " + counterAttackDamage);
      }

      // Change this unit's health
      int finalCounterDamage =
          (int)
              Math.max(
                  0,
                  counterAttackDamage * Math.max(0, 1 - getAttackerPercentageDamageReduction())
                      - getAttackerFlatDamageReduction());
      if (DEBUG) {
        System.out.println("Final Counter damage: " + finalCounterDamage);
      }
      attacker.changeHealth(Math.min(0, -finalCounterDamage), defender);
      stage = Stage.COUNTER_ATTACK_COMPLETED;
    } else {
      stage = Stage.COUNTER_ATTACK_SKIPPED;
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
    stage = Stage.POST_ATTACK_COMPLETED;

    // If frame, add animation(s).
    if (attacker.owner.game.getController().hasFrame()) {
      GamePanel gamePanel = attacker.owner.game.getController().frame.getGamePanel();
      if (attacker.isAlive()) {
        gamePanel.addCombatAnimation(
            attacker, defender.getLocation(), defender.isAlive() && counterAttack);
      } else if (defender.isAlive() && counterAttack) {
        gamePanel.addCombatAnimation((Combatant) defender, attacker.getLocation(), false);
      }
    }

    boolean defenderIsDead = !defender.isAlive();

    stage = Stage.COMBAT_COMPLETED;
    if (DEBUG) System.out.println("End combat ------");

    return defenderIsDead;
  }
}
