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

  /** True after process() is called, to prevent double counting combat. */
  private boolean processed;

  /**
   * Creates a new combat on the given attacker and defender.
   * Does not process combat until process is called.
   */
  public Combat(Combatant attacker, Unit defender) {
    this.attacker = attacker;
    this.defender = defender;
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

    int room = attacker.getLocation().manhattanDistance(defender.getLocation()) - 1; //Account for melee = 0 range
    if(room > attacker.getAttackRange())
      throw new IllegalArgumentException(this + " can't fight " + defender + ", it is too far away.");

    System.out.println("Start combat ------");

    // Get damage in range [min,max]
    int damage = random.nextInt(attacker.getMaxAttack() + 1 - attacker.getMinAttack()) + attacker.getMinAttack();
    System.out.println("Base damage: " + damage);

    // If defender is combatant, scale by bonus levels as needed.
    if (defender instanceof Combatant) {
      int bonusLevel = Combatant.CombatantClass.getBonusLevel(attacker.combatantClasses, ((Combatant) defender).combatantClasses);
      damage *= 1 + COMBAT_CLASS_BONUS * bonusLevel;
      System.out.println("Bonus Level: " + bonusLevel);
      System.out.println("Scaled damage: " + damage);
    }

    //True if a counterAttack is happening, false defenderwise.
    boolean counterAttack = defender.isAlive() && defender.owner.canSee(attacker) && room <= defender.getAttackRange()
        && damage < defender.getHealth() && defender instanceof Combatant;

    attacker.preFight(defender);
    if(counterAttack) defender.preCounterFight(attacker);

    //This attacks defender
    defender.changeHealth(-damage, attacker);

    //If defender is still alive, can see the first unit,
    //and this is within range, defender counterattacks
    int counterAttackDamage = 0;
    if(counterAttack){
      // Get damage in range [min,max]
      counterAttackDamage = random.nextInt(attacker.getMaxAttack() + 1 - attacker.getMinAttack()) + attacker.getMinAttack();
      System.out.println("Counter damage: " + counterAttackDamage);

      // Given counter attack is happening, we know defender to be a combatant.
      int bonusLevel = Combatant.CombatantClass.getBonusLevel(((Combatant) defender).combatantClasses, attacker.combatantClasses);
      counterAttackDamage *= 1 + COMBAT_CLASS_BONUS * bonusLevel;

      System.out.println("Bonus Level: " + bonusLevel);
      System.out.println("Scaled damage: " + counterAttackDamage);

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
