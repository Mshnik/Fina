package model.unit.modifier;

import model.unit.modifier.Modifier.StackMode;
import model.unit.stat.StatType;

/**
 * Listing of Modifier instances.
 *
 * @author Mshnik
 */
public final class Modifiers {
  private Modifiers() {}

  /** Modifier that gives life gain after dealing damage. */
  public static final Modifier BORN_TO_FIGHT =
      new CustomModifier(
          "Born to Fight",
          "Born to Fight",
          "After dealing damage, this unit gains some health",
          10,
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          false,
          false,
          true);

  /** Modifier that deals more damage to commanders. */
  public static final Modifier BLOODLUST =
      new CustomModifier(
          "Bloodlust",
          "Bloodlust",
          "This unit deals more damage to commanders",
          10,
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          false,
          false,
          true);

  /** Modifier that allows a unit to see into woods and past mountains. */
  public static final Modifier EAGLE_EYE =
      new CustomModifier(
          "Eagle Eye",
          "Eagle Eye",
          "This unit can see into woods and past mountains.",
          null,
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          true,
          true,
          true);

  /** Modifier that reduces movement cost in woods. */
  public static final Modifier PATHFINDER =
      new StatModifier(
          "Pathfinder",
          "Pathfinder",
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          StatType.WOODS_COST,
          StatModifier.ModificationType.SET,
          1);

  /** Modifier that reduces movement cost in woods. */
  public static final Modifier TRAILBLAZER =
      new StatModifier(
          "Trailblazer",
          "Trailblazer",
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          StatType.MOUNTAIN_COST,
          StatModifier.ModificationType.SET,
          3);

  /** Modifier that slowly regenerates health. */
  public static final Modifier TOUGHNESS =
      new CustomModifier(
          "Toughness",
          "Toughness",
          "This unit gains health each turn",
          5,
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          true,
          true,
          true);

  /** Modifier that slowly regenerates health. */
  public static final Modifier DISAPPEARANCE =
      new CustomModifier(
          "Disappearance",
          "Disappearance",
          "This unit can move after attacking",
          null,
          Integer.MAX_VALUE,
          StackMode.STACKABLE,
          false,
          false,
          true);
}
