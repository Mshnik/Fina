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
  public static Modifier bloodlust(int bonusDamageToCommanderPercent) {
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

  /** Modifier that reduces movement cost in woods. */
  public static Modifier pathfinder(int woodsMoveCost) {
    return new StatModifier(
            "Pathfinder",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.WOODS_COST,
            StatModifier.ModificationType.SET,
            woodsMoveCost)
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
            StatModifier.ModificationType.SET,
            mountainMoveCost)
        .uniqueCopy();
  }
}
