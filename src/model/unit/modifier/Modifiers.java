package model.unit.modifier;

/**
 * Listing of Modifier instances.
 * @author Mshnik
 */
public final class Modifiers {
  private Modifiers() {}

  /** Modifier that gives life gain after dealing damage. */
  public static final CustomModifier BORN_TO_FIGHT =
      new CustomModifier("Born to Fight",
          "After dealing damage, this unit gains [x] health",
          10,
          Integer.MAX_VALUE,
          true,
          false,
          false,
          true);

  /** Modifier that reduces movement cost in woods. */
  public static final CustomModifier PATHFINDER =
      new CustomModifier("Pathfinder",
          "Reduces movement cost in woods to [x]",
          1,
          Integer.MAX_VALUE,
          false,
          false,
          true,
          true);

  /** Modifier that reduces movement cost in woods. */
  public static final CustomModifier TRAILBLAZER =
      new CustomModifier("Trailblazer",
          "Reduces movement cost in mountains to [x]",
          3,
          Integer.MAX_VALUE,
          false,
          false,
          true,
          true);

  /** Modifier that slowly regenerates health. */
  public static final CustomModifier TOUGHNESS =
      new CustomModifier("Toughness",
          "This unit gains [x] health each turn",
          5,
          Integer.MAX_VALUE,
          true,
          true,
          true,
          true);

  /** Modifier that slowly regenerates health. */
  public static final CustomModifier DISAPPEARANCE =
      new CustomModifier("Disappearance",
          "This unit can move after attacking",
          null,
          Integer.MAX_VALUE,
          false,
          false,
          false,
          true);
}
