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
    list.add(new ModifierDescription(blinded(0)));
    list.add(new ModifierDescription(bloodlust(0)));
    list.add(new ModifierDescription(bornToFight(0)));
    list.add(new ModifierDescription(communications(0, 0)));
    list.add(new ModifierDescription(disappearance()));
    list.add(new ModifierDescription(eagleEye()));
    list.add(new ModifierDescription(elusive(0)));
    list.add(new ModifierDescription(farsight(0)));
    list.add(new ModifierDescription(flight()));
    list.add(new ModifierDescription(hexproof(0)));
    list.add(new ModifierDescription(pathfinder(0)));
    list.add(new ModifierDescription(patience(0)));
    list.add(new ModifierDescription(shielded(0)));
    list.add(new ModifierDescription(siege(0)));
    list.add(new ModifierDescription(sluggish(0)));
    list.add(new ModifierDescription(strengthened(0)));
    list.add(new ModifierDescription(tenacity(0)));
    list.add(new ModifierDescription(toughness(0)));
    list.add(new ModifierDescription(trailblazer(0)));
    list.add(new ModifierDescription(quickness(0)));
    list.add(new ModifierDescription(weakened(0)));
    MODIFIER_DESCRIPTIONS = Collections.unmodifiableList(list);
  }

  /** A description of a modifier or bundle, for showing in UI. */
  public static final class ModifierDescription {
    private final String name;
    private final String formattedVal;
    private final String description;
    private final int turnsRemaining;

    private ModifierDescription(Modifier m) {
      this.name = m.name;
      this.formattedVal = m.getValueFormatted();
      this.description = m.toStatString();
      this.turnsRemaining = m.getRemainingTurns();
    }

    private ModifierDescription(ModifierBundle m) {
      this.name = m.getModifiers().get(0).name;
      this.formattedVal = m.getModifiers().get(0).getValueFormatted();
      this.description = m.toStatString().trim();
      this.turnsRemaining = m.getTurnsRemaining();
    }

    /**
     * Returns a string describing the number of turns remaining, or empty if this is infinite
     * duration.
     */
    private String getTurnsRemainingString() {
      return turnsRemaining == Integer.MAX_VALUE ? "" : " (" + turnsRemaining + " turns)";
    }

    private String getFormattedValStringWithSpace() {
      return formattedVal.isEmpty() ? formattedVal : " " + formattedVal + " ";
    }

    /** Returns a short string for displaying on the info panel. */
    public String toStringShort() {
      return name + getFormattedValStringWithSpace() + getTurnsRemainingString();
    }

    /** Returns a longer string for displaying on the info panel. */
    @Override
    public String toString() {
      return name
          + getFormattedValStringWithSpace()
          + " - "
          + description
          + getTurnsRemainingString();
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

  /**
   * Returns the ModifierBundle by name, with the given value. If the value is not used, it may be
   * null.
   */
  public static ModifierBundle getBundleByName(String modName, Number modValue) {
    switch (modName) {
      case "Armored":
        return new ModifierBundle(armored(modValue.doubleValue()));
      case "Blinded":
        return new ModifierBundle(blinded(modValue.intValue()));
      case "Bloodlust":
        return new ModifierBundle(bloodlust(modValue.doubleValue()));
      case "Born to Fight":
        return new ModifierBundle(bornToFight(modValue.intValue()));
      case "Communications":
        throw new RuntimeException(
            "Communications instantiation not supported here because it requires two args");
      case "Disappearance":
        return new ModifierBundle(disappearance());
      case "Eagle Eye":
        return new ModifierBundle(eagleEye());
      case "Elusive":
        return new ModifierBundle(elusive(modValue.doubleValue()));
      case "Farsight":
        return new ModifierBundle(farsight(modValue.intValue()));
      case "Flight":
        return flight();
      case "Hexproof":
        return new ModifierBundle(hexproof(modValue.doubleValue()));
      case "Pathfinder":
        return new ModifierBundle(pathfinder(modValue.intValue()));
      case "Patience":
        return new ModifierBundle(patience(modValue.doubleValue()));
      case "Quickness":
        return new ModifierBundle(quickness(modValue.intValue()));
      case "Shielded":
        return new ModifierBundle(shielded(modValue.doubleValue()));
      case "Siege":
        return new ModifierBundle(siege(modValue.doubleValue()));
      case "Sluggish":
        return new ModifierBundle(sluggish(modValue.intValue()));
      case "Strengthened":
        return strengthened(modValue.intValue());
      case "Tenacity":
        return new ModifierBundle(tenacity(modValue.intValue()));
      case "Toughness":
        return new ModifierBundle(toughness(modValue.intValue()));
      case "Trailblazer":
        return new ModifierBundle(trailblazer(modValue.intValue()));
      case "Weakened":
        return new ModifierBundle(weakened(modValue.intValue()));
      default:
        throw new RuntimeException("Unknown modName: " + modName);
    }
  }

  /** Modifier that reduces incoming melee damage */
  public static Modifier armored(double damageReduction) {
    return new CustomModifier(
            "Armored",
            "This unit takes -x%- less damage from melee attacks",
            damageReduction,
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            true,
            true,
            true)
        .uniqueCopy();
  }

  /** Modifier that decreases vision range */
  public static Modifier blinded(int visionRangeDecreases) {
    return new StatModifier(
            "Blinded",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.VISION_RANGE,
            StatModifier.ModificationType.ADD,
            -visionRangeDecreases)
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

  /** ModifierBundle that increases both vision and movement under the name "Communications" */
  public static ModifierBundle communications(int visionIncrease, int movementIncrease) {
    return new ModifierBundle(
        new CustomModifier(
            "Communications", "", null, Integer.MAX_VALUE, StackMode.STACKABLE, false, true, true),
        farsight(visionIncrease),
        quickness(movementIncrease));
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
  public static Modifier elusive(double damageReductionPercent) {
    return new CustomModifier(
            "Elusive",
            "This unit takes -x%- less damage from ranged attacks",
            damageReductionPercent,
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
            "Patience",
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

  /** Modifier that decreases total movement */
  public static Modifier sluggish(int totalMovementDecrease) {
    return new StatModifier(
            "Sluggish",
            Integer.MAX_VALUE,
            StackMode.STACKABLE,
            StatType.MOVEMENT_TOTAL,
            StatModifier.ModificationType.ADD,
            -totalMovementDecrease)
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

  /** Modifier that decreases attack strength */
  public static ModifierBundle weakened(int attackDecrease) {
    return new ModifierBundle(
        new StatModifier(
                "Weakened",
                Integer.MAX_VALUE,
                StackMode.STACKABLE,
                StatType.MIN_ATTACK,
                StatModifier.ModificationType.ADD,
                -attackDecrease)
            .uniqueCopy(),
        new StatModifier(
                // Empty so name doesn't occur twice.
                "",
                Integer.MAX_VALUE,
                StackMode.STACKABLE,
                StatType.MAX_ATTACK,
                StatModifier.ModificationType.ADD,
                -attackDecrease)
            .uniqueCopy());
  }
}
