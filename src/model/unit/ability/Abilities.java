package model.unit.ability;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.unit.Unit;
import model.unit.ability.Ability.AbilityType;
import model.unit.building.Building;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.Modifiers;
import model.util.ExpandableCloud;
import model.util.ExpandableCloud.ExpandableCloudType;
import util.TextIO;

/**
 * Index of abilities commanders can use.
 */
public final class Abilities {

  /**
   * List of all available abilities as read from storage.
   */
  private static final Map<String, Ability> ABILITIES;

  /**
   * File containing the abilities, in csv format
   */
  private static final File ABILITIES_FILE = new File("game/units/spells.csv");

  static {
    LinkedHashMap<String, Ability> abilities = new LinkedHashMap<>();
    try {
      String[] abilityLines = TextIO.read(ABILITIES_FILE).split("\\n");

      for (String line : abilityLines) {
        String[] comps = line.split(",");
        if (comps.length == 0 || comps[0].isEmpty()) continue; // Blank line.
        if (comps[0].equals("Name")) continue; // Header line.
        if (comps[0].equals("SKIP")) continue; // Not fully implemented ability.

        try {
          String name = comps[0];
          int level = Integer.parseInt(comps[1]);
          int manaCost = Integer.parseInt(comps[2]);
          AbilityType abilityType = AbilityType.valueOf(comps[3].toUpperCase());

          String[] cloudComps = comps[4].split("-");
          ExpandableCloudType cloudType = ExpandableCloudType.valueOf(cloudComps[0].toUpperCase());
          int cloudRadius = Integer.parseInt(cloudComps[1]);
          boolean canBeCloudBoosted = comps[5].equals("Yes");

          int castDist = Integer.parseInt(comps[6]);
          List<String> affectedUnitTypeStrings =
              Arrays.stream(comps[7].split("/")).collect(Collectors.toList());
          List<Class<? extends Unit>> affectedUnitTypes = new ArrayList<>();
          if (affectedUnitTypeStrings.contains("Combatant")) {
            affectedUnitTypes.add(Combatant.class);
          }
          if (affectedUnitTypeStrings.contains("Building")) {
            affectedUnitTypes.add(Building.class);
          }
          if (affectedUnitTypeStrings.contains("Commander")) {
            affectedUnitTypes.add(Commander.class);
          }

          boolean affectsAllied = comps[8].equals("Yes");
          boolean affectsEnemy = comps[9].equals("Yes");

          String description = comps[10];

          AbilityConstructor constructor;
          switch (name) {
            case Sacrifice.NAME:
              constructor = Sacrifice::new;
              break;
            case SpacialShift.NAME:
              constructor = SpacialShift::new;
              break;
            default:
              constructor = Ability::new;
              break;
          }

          if (abilities.containsKey(name)) {
            throw new RuntimeException(String.format("Abilities already has ability for name %s: %s", name, abilities.get(name)));
          }

          abilities.put(
              name,
              constructor.create(
                  name,
                  abilityType,
                  level,
                  manaCost,
                  ExpandableCloud.create(cloudType, cloudRadius),
                  canBeCloudBoosted,
                  castDist,
                  affectedUnitTypes,
                  affectsAllied,
                  affectsEnemy,
                  description,
                  getEffectsFor(name)));

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ABILITIES = Collections.unmodifiableMap(abilities);
  }

  /**
   * A generic functional interface wrapping a constructor for a particular Ability type.
   */
  @FunctionalInterface
  private interface AbilityConstructor<A extends Ability> {
    /**
     * Creates a new ability instance from the given values.
     */
    A create(
        String name,
        AbilityType abilityType,
        int level,
        int manaCost,
        ExpandableCloud effectCloud,
        boolean canBeCloudBoosted,
        int castDist,
        List<Class<? extends Unit>> affectedUnitTypes,
        boolean appliesToAllied,
        boolean appliesToFoe,
        String description,
        List<AbilityEffect> effects);
  }

  /**
   * Helper for construction to get the list of ability effects for an ability.
   */
  private static List<AbilityEffect> getEffectsFor(String abilityName) {
    switch (abilityName) {
      case "Magic Missile":
        return Collections.singletonList(AbilityEffect.damage(30, 45));
      case "Heal":
        return Collections.singletonList(AbilityEffect.healPercentageOfMaxHp(.5));
      case "Clairvoyance":
        return Collections.singletonList(AbilityEffect.modifierBundle(Modifiers.farsight(3)));
      case "Sacrifice":
        return Collections.singletonList(AbilityEffect.destroyUnit());
      case "Research":
        return Collections.singletonList(AbilityEffect.research(75));
      case "Cone of Flame":
        return Collections.singletonList(AbilityEffect.damage(45, 60));
      case "Strengthen":
        return Collections.singletonList(AbilityEffect.modifierBundle(Modifiers.strengthened(10)));
      case "Repair":
        return Collections.singletonList(AbilityEffect.healPercentageOfMaxHp(1.0));
      case "Toughen":
        return Collections.singletonList(AbilityEffect.modifierBundle(Modifiers.toughness(10)));
      case "Quicken":
        return Collections.singletonList(AbilityEffect.modifierBundle(Modifiers.quickness(3)));
      case "Cone of Ice":
        return Arrays.asList(
            AbilityEffect.damage(45, 60), AbilityEffect.modifierBundle(Modifiers.sluggish(1)));
      case "Cone of Electricity":
        return Arrays.asList(
            AbilityEffect.damage(45, 60), AbilityEffect.modifierBundle(Modifiers.weakened(5)));
      case "Mass Heal":
        return Collections.singletonList(AbilityEffect.healPercentageOfMaxHp(.5));
      case "Resolution":
        return Arrays.asList(
            AbilityEffect.healConstantHp(25), AbilityEffect.modifierBundle(Modifiers.tenacity(25)));
      case "Refresh":
        return Collections.singletonList(AbilityEffect.refreshUnit());
      case "Spacial Shift":
        return Collections.emptyList();
      case "Cone of Light":
        return Arrays.asList(
            AbilityEffect.damage(60, 90), AbilityEffect.modifierBundle(Modifiers.blinded(2)));
      case "Mana Shield":
        return Arrays.asList(
            AbilityEffect.modifierBundle(Modifiers.shielded(0.25)),
            AbilityEffect.healPercentageOfMaxHp(0.5));
      case "Levitation":
        return Collections.singletonList(
            AbilityEffect.modifierBundle(
                new ModifierBundle(
                    Modifiers.flight(), Modifiers.farsight(3), Modifiers.quickness(1))));
      case "Channel":
        return Collections.singletonList(AbilityEffect.mana(25));
      default:
        throw new RuntimeException("Unknown ability name " + abilityName);
    }
  }

  /**
   * Returns the ability for {@code name}, or throws an exception if it does not exist.
   */
  public static Ability getAbilityForName(String name) {
    if (ABILITIES.containsKey(name)) {
      return ABILITIES.get(name);
    } else {
      throw new RuntimeException("Unknown ability " + name);
    }
  }

  /**
   * Returns a list of all abilities
   */
  public static List<Ability> getAbilities() {
    return Collections.unmodifiableList(new ArrayList<>(ABILITIES.values()));
  }

  /**
   * Returns a list of abilities for the given age (minus 1 because age is 1 indexed, this is 0
   * indexed) - returns by value
   */
  public static List<Ability> getAbilitiesForAge(int age) {
    return ABILITIES.values().stream().filter(a -> a.level == age).collect(Collectors.toList());
  }
}
