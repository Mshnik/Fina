package model.unit.combatant;

import model.unit.Combatant;
import model.unit.Unit;
import model.unit.commander.Bhen;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;

import java.util.Random;

/**
 * A class that describes a prospective combat between two given units and can process it.
 * Used to handle combat logic in addition to record logistic data and paint informational
 * data for the user.
 * @author Mshnik
 */
public final class Combat {

  /**
   * Percentage bonus in attack and defense a combatant gets against another when it has a class bonus.
   * The bonus is stacked for each level of bonus.
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
   * Creates a new combat on the given attacker and defender.
   * Does not process combat until process is called.
   */
  public Combat(Combatant attacker, Unit defender) {
    this.attacker = attacker;
    this.defender = defender;

    //Account for melee = 0 range
    dist = attacker.getLocation().manhattanDistance(defender.getLocation()) - 1;
  }

  /** Returns the class bonus for the attacker (the defender is the opposite).
   * If the defender is not a combatant, this will always be 0.
   */
  public int getClassBonus() {
    if (defender instanceof Combatant) {
      return Combatant.CombatantClass.getBonusLevel(attacker.combatantClasses, ((Combatant) defender).combatantClasses);
    } else {
      return 0;
    }
  }

  /** Returns the minimum damage the attacker could do after scaling by combat classes and account for modifiers. */
  public int getMinAttack() {
    return Math.max(0, (int) (attacker.getMinAttackScaled() * (1 + COMBAT_CLASS_BONUS * getClassBonus())));
  }

  /** Returns the maximum damage the attacker could do after scaling by combat classes and account for modifiers. */
  public int getMaxAttack() {
    return Math.max(0, (int) (attacker.getMaxAttackScaled() * (1 + COMBAT_CLASS_BONUS * getClassBonus())));
  }

  /** Returns true iff the defender could counterattack if it has health left after the attack. */
  public boolean defenderCouldCounterAttack() {
    return defender.isAlive()
        && defender.owner.canSee(attacker)
        && dist <= defender.getAttackRange()
        && defender instanceof Combatant;
  }

  /** Returns the minimum damage the defender could do after scaling by combat classes and account for modifiers. */
  private int getMinCounterAttack() {
    return Math.max(0, (int) (defender.getMinAttackScaled() * (1 - (COMBAT_CLASS_BONUS * getClassBonus()))));
  }

  /** Returns the maximum damage the defender could do after scaling by combat classes and account for modifiers. */
  private int getMaxCounterAttack() {
    return Math.max(0, (int) (defender.getMaxAttackScaled() * (1 - (COMBAT_CLASS_BONUS * getClassBonus()))));
  }

  /**
   * Returns the projected minimum damage the defender could do after scaling by combat classes
   * and account for modifiers along with change in health
   * Should only be used for projections, not actual combat.
   */
  public int getProjectedMinCounterAttack() {
    double minProjectedHealthPercentage = ((double)defender.getHealth() - getMaxAttack()) / defender.getMaxHealth();
    return (int) (defender.getMinAttack() * minProjectedHealthPercentage * (1 - COMBAT_CLASS_BONUS * getClassBonus()));
  }

  /**
   * Returns the maximum damage the defender could do after scaling by combat classes
   * and account for modifiers along with change in health.
   * Should only be used for projections, not actual combat.
   */
  public int getProjectedMaxCounterAttack() {
    double maxProjectedHealthPercentage = ((double)defender.getHealth() - getMinAttack()) / defender.getMaxHealth();
    return (int) (defender.getMaxAttack() * maxProjectedHealthPercentage * (1 - COMBAT_CLASS_BONUS * getClassBonus()));
  }

  /**
   * Causes this unit to fight the given unit.
   * With this as the attacker and defender as the defender.
   * This will cause the health of the defender to change
   * @throws RuntimeException if...
   * 		- this is dead
   * 		- this can't attack currently
   * @throws IllegalArgumentException for invalid fight when...
   * 		- defender is dead
   * 		- both units belong to the same player
   * 		- defender is out of the range of this
   * 		- this' owner can't see defender
   * @return true iff defender is killed because of this action
   **/
  public final boolean process(Random random) throws IllegalArgumentException, RuntimeException{
    if (processed)
      throw new RuntimeException("Can't process " + this + "process() already called.");
    if(! attacker.isAlive())
      throw new RuntimeException (attacker + " can't fight, it is dead.");
    if(! defender.isAlive())
      throw new IllegalArgumentException(defender + " can't fight, it is dead.");
    if(attacker.owner == defender.owner)
      throw new IllegalArgumentException(attacker + " can't fight " + defender + ", they both belong to " + attacker.owner);
    if(! attacker.getCanFight())
      throw new RuntimeException(attacker + " can't fight again this turn");
    if(! attacker.owner.canSee(defender))
      throw new IllegalArgumentException(attacker.owner + " can't see " + defender);
    if(dist > attacker.getAttackRange())
      throw new IllegalArgumentException(this + " can't fight " + defender + ", it is too far away.");

    System.out.println("Start combat ------");

    // Get damage in range [min,max]
    int damage = random.nextInt(getMaxAttack() + 1 - getMinAttack()) + getMinAttack();
    System.out.println("damage: " + damage);

    //True if a counterAttack is happening, false otherwise.
    boolean counterAttack = defenderCouldCounterAttack() && damage < defender.getHealth();

    attacker.preFight(defender);
    if(counterAttack) defender.preCounterFight(attacker);

    //attacker attacks defender
    defender.changeHealth(-damage, attacker);

    //If defender is still alive, can see the first unit,
    //and this is within range, defender counterattacks
    int counterAttackDamage = 0;
    if(counterAttack){
      // Get damage in range [min,max]
      counterAttackDamage = random.nextInt(getMaxCounterAttack() + 1 - getMinCounterAttack()) + getMinCounterAttack();
      System.out.println("Counter damage: " + counterAttackDamage);

      // Change this unit's health
      attacker.changeHealth(- counterAttackDamage, defender);
      counterAttack = true;
    }

    //This can't attack this turn again
    attacker.setCanFight(false);

    //Calls postFight on units that are still alive, able to counterAttack
    if(attacker.isAlive()){
      attacker.postFight(damage, defender, counterAttackDamage);
    }
    if(defender.isAlive() && counterAttack) {
      defender.postCounterFight(counterAttackDamage, attacker, damage);
    }

    boolean defenderIsDead = ! defender.isAlive();

    //Check for killing modifiers
    Modifier mod1 = attacker.getModifierByName(Bhen.ABILITY_NAMES[0][0]);
    if(mod1 != null){
      attacker.addMovement(((CustomModifier)mod1).val.intValue());
    }

    processed = true;
    System.out.println("End combat ------");

    return defenderIsDead;
  }
}
