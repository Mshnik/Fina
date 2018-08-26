package model.unit.modifier;

import model.unit.modifier.Modifier.StackMode;
import model.unit.stat.StatType;

/**
 * Listing of Modifier instances. Each time a method is called it returns a unique copy for the
 * purposes of cloning.
 *
 * @author Mshnik
 */
public final class Modifiers {
  private Modifiers() {}

  /** Modifier that gives life gain after dealing damage. */
  public static Modifier bornToFight(int healthGainedPerAttack) {
    return new CustomModifier(
            "Born to Fight",
            "After dealing damage, this unit gains some health",
            healthGainedPerAttack,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            false,
            false,
            true)
        .uniqueCopy();
  }

  /** Modifier that deals more damage to commanders. */
  public static Modifier bloodlust(double bonusDamageToCommanderPercent) {
    return new CustomModifier(
            "Bloodlust",
            "This unit deals more damage to commanders",
            bonusDamageToCommanderPercent,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            false,
            false,
            true)
        .uniqueCopy();
  }

  /** Modifier that slowly regenerates health. */
  public static Modifier disappearance() {
    return new CustomModifier(
            "Disappearance",
            "This unit can move after attacking",
            null,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            false,
            false,
            true)
        .uniqueCopy();
  }

  /** Modifier that allows a unit to see into woods and past mountains. */
  public static Modifier eagleEye() {
    return new CustomModifier(
            "Eagle Eye",
            "This unit can see into woods and past mountains.",
            null,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /**
   * Modifier that gives the unit move cost 1 on all terrain, eagle eye, and isn't hidden in woods.
   */
  public static ModifierBundle flight() {
    return new ModifierBundle(
        new CustomModifier(
            "Flight",
            "This unit has move cost 1 on all terrain, eagle eye, and isn't hidden in woods",
            0,
            Integer.MAX_VALUE,
            StackMode.DURATION_ADD,
            false,
            true,
            true),
        eagleEye(),
        trailblazer(1),
        pathfinder(1));
  }

  /** Modifier that makes a unit takes less damage from spells. */
  public static Modifier hexproof(double spellDamageReductionPercent) {
    return new CustomModifier(
            "Hexproof",
            "This unit takes less damage from commander spells",
            spellDamageReductionPercent,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /** Modifier that reduces movement cost in woods. */
  public static Modifier pathfinder(int woodsMoveCost) {
    return new StatModifier(
            "Pathfinder",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.WOODS_COST,
            StatModifier.ModificationType.SET_MIN,
            woodsMoveCost)
        .uniqueCopy();
  }

  /** Modifier that deals more damage on counterattack */
  public static Modifier patience(double bonusCounterAttackDamagePercent) {
    return new CustomModifier(
            "Siege",
            "This unit deals more damage on counter attacking",
            bonusCounterAttackDamagePercent,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            false,
            false,
            true)
        .uniqueCopy();
  }

  /** Modifier that increases max health. */
  public static Modifier shielded(double maxHealthIncreasePercent) {
    return new StatModifier(
            "Shielded",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.MAX_HEALTH,
            StatModifier.ModificationType.MULTIPLY,
            1 + maxHealthIncreasePercent)
        .uniqueCopy();
  }

  /** Modifier that deals more damage to buildings. */
  public static Modifier siege(double bonusDamageToBuildingPercent) {
    return new CustomModifier(
            "Siege",
            "This unit deals more damage to buildings",
            bonusDamageToBuildingPercent,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            false,
            false,
            true)
        .uniqueCopy();
  }

  /** Modifier Bundle that grants bonus min and max attack. */
  public static ModifierBundle strengthened(int attackBonus) {
    return new ModifierBundle(
        new StatModifier(
                "Strengthened",
                Integer.MAX_VALUE,
                StackMode.STACKABLE,
                StatType.MIN_ATTACK,
                StatModifier.ModificationType.ADD,
                attackBonus)
            .uniqueCopy(),
        new StatModifier(
                // Empty so name doesn't occur twice.
                "",
                Integer.MAX_VALUE,
                StackMode.STACKABLE,
                StatType.MAX_ATTACK,
                StatModifier.ModificationType.ADD,
                attackBonus)
            .uniqueCopy());
  }

  /** Modifier that slowly regenerates health. */
  public static Modifier tenacity(int healthGainedPerTurn) {
    return new CustomModifier(
            "Tenacity",
            "This unit gains health each turn",
            healthGainedPerTurn,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /** Modifier that reduces incoming damage */
  public static Modifier toughness(int damageReduction) {
    return new CustomModifier(
            "Toughness",
            "This unit takes less damage",
            damageReduction,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /** Modifier that reduces movement cost in mountains. */
  public static Modifier trailblazer(int mountainMoveCost) {
    return new StatModifier(
            "Trailblazer",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.MOUNTAIN_COST,
            StatModifier.ModificationType.SET_MIN,
            mountainMoveCost)
        .uniqueCopy();
  }

  /** Modifier that increases total movement */
  public static Modifier quickness(int totalMovementIncrease) {
    return new StatModifier(
            "Quickness",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.MOVEMENT_TOTAL,
            StatModifier.ModificationType.ADD,
            totalMovementIncrease)
        .uniqueCopy();
  }
}
