package model.unit.modifier;

import model.unit.modifier.Modifier.StackMode;
import model.unit.stat.StatType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Listing of Modifier instances. Each time a method is called it returns a unique copy for the
 * purposes of cloning.
 *
 * @author Mshnik
 */
public final class Modifiers {
  private Modifiers() {}

  /** A list of all modifier descriptions in the game. */
  private static final List<ModifierDescription> MODIFIER_DESCRIPTIONS;

  static {
    ArrayList<ModifierDescription> list = new ArrayList<>();
    list.add(new ModifierDescription(armored(0)));
    list.add(new ModifierDescription(bornToFight(0)));
    list.add(new ModifierDescription(bloodlust(0)));
    list.add(new ModifierDescription(eagleEye()));
    list.add(new ModifierDescription(elusive(0)));
    list.add(new ModifierDescription(farsight(0)));
    list.add(new ModifierDescription(flight()));
    list.add(new ModifierDescription(hexproof(0)));
    list.add(new ModifierDescription(pathfinder(0)));
    list.add(new ModifierDescription(patience(0)));
    list.add(new ModifierDescription(shielded(0)));
    list.add(new ModifierDescription(strengthened(0)));
    list.add(new ModifierDescription(tenacity(0)));
    list.add(new ModifierDescription(toughness(0)));
    list.add(new ModifierDescription(trailblazer(0)));
    list.add(new ModifierDescription(quickness(0)));
    MODIFIER_DESCRIPTIONS = Collections.unmodifiableList(list);
  }

  /** A description of a modifier or bundle, for showing in UI. */
  public static final class ModifierDescription {
    private final String name;
    private final String description;
    private final int turnsRemaining;

    private ModifierDescription(Modifier m) {
      this.name = m.name;
      this.description = m.toStatString();
      this.turnsRemaining = m.getRemainingTurns();
    }

    private ModifierDescription(ModifierBundle m) {
      this.name = m.getModifiers().get(0).name;
      this.description = m.toStatString().trim();
      this.turnsRemaining = m.getTurnsRemaining();
    }

    @Override
    public String toString() {
      return name
          + " - "
          + description
          + (turnsRemaining == Integer.MAX_VALUE ? "" : " (" + turnsRemaining + " turns)");
    }
  }

  /** Returns a list of all modifier descriptions. */
  public static List<ModifierDescription> getModifierDescriptions() {
    return MODIFIER_DESCRIPTIONS;
  }

  /** Returns a list of all modifier descriptions for the given list of modifiers */
  public static List<ModifierDescription> getModifierDescriptions(List<Modifier> modifiers) {
    HashSet<Modifier> addedModifiers = new HashSet<>();
    ArrayList<ModifierDescription> descriptions = new ArrayList<>();
    for (Modifier m : modifiers) {
      if (!addedModifiers.contains(m)) {
        if (m.bundle != null) {
          descriptions.add(new ModifierDescription(m.bundle));
          addedModifiers.addAll(m.bundle.getModifiers());
        } else {
          descriptions.add(new ModifierDescription(m));
          addedModifiers.add(m);
        }
      }
    }
    return Collections.unmodifiableList(descriptions);
  }

  /** Modifier that reduces incoming melee damage */
  public static Modifier armored(int damageReduction) {
    return new CustomModifier(
            "Armored",
            "This unit takes -x- less damage from melee attacks",
            damageReduction,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /** Modifier that gives life gain after dealing damage. */
  public static Modifier bornToFight(int healthGainedPerAttack) {
    return new CustomModifier(
            "Born to Fight",
            "After dealing damage, this unit gains -x- health",
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
            "This unit deals -x%- more damage to commanders",
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

  /** Modifier that reduces incoming ranged damage */
  public static Modifier elusive(int damageReduction) {
    return new CustomModifier(
            "Elusive",
            "This unit takes -x- less damage from ranged attacks",
            damageReduction,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /** Modifier that increases total movement */
  public static Modifier farsight(int visionRangeIncrease) {
    return new StatModifier(
            "Farsight",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.VISION_RANGE,
            StatModifier.ModificationType.ADD,
            visionRangeIncrease)
        .uniqueCopy();
  }

  /**
   * Modifier that gives the unit move cost 1 on all terrain, eagle eye, and isn't hidden in woods.
   */
  public static ModifierBundle flight() {
    return new ModifierBundle(
        new CustomModifier(
            "Flight", "", null, Integer.MAX_VALUE, StackMode.STACKABLE, false, true, true),
        eagleEye(),
        trailblazer(1),
        pathfinder(1));
  }

  /** Modifier that makes a unit takes less damage from spells. */
  public static Modifier hexproof(double spellDamageReductionPercent) {
    return new CustomModifier(
            "Hexproof",
            "This unit takes -x%- damage from commander spells",
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
            "This unit deals -x%- more damage when counter attacking",
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
            "This unit deals -x%- damage to buildings",
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
            "This unit gains -x- health each turn",
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
            "This unit takes -x- less damage from all sources",
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
