package model.unit.building;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import model.board.Terrain;
import model.unit.building.PlayerModifierBuilding.PlayerModifierEffect;
import model.unit.building.PlayerModifierBuilding.PlayerModifierEffectType;
import model.unit.building.StartOfTurnEffectBuilding.StartOfTurnEffect;
import model.unit.building.StartOfTurnEffectBuilding.StartOfTurnEffectType;
import model.unit.modifier.Modifier.StackMode;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.StatModifier;
import model.unit.modifier.StatModifier.ModificationType;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;
import model.util.ExpandableCloud;
import model.util.ExpandableCloud.ExpandableCloudType;
import util.TextIO;

/** Index of buildings read from storage. */
public final class Buildings {

  /** List of all available buildings as read from storage. */
  private static final List<Building> BUILDINGS;

  /** File containing the buildings, in csv format */
  private static final File BUILDINGS_FILE = new File("game/buildings.csv");

  static {
    List<Building> buildings = new LinkedList<>();

    try {
      String[] buildingLines = TextIO.read(BUILDINGS_FILE).split("\\n");
      for (String line : buildingLines) {
        String[] comps = line.split(",");
        if (comps.length == 0 || comps[0].isEmpty()) continue; // Blank line.
        if (comps[0].equals("Name")) continue; // Header line.

        try {
          String name = comps[0];
          String imageFilename = comps[1];
          int level = Integer.parseInt(comps[2]);
          int baseCost = Integer.parseInt(comps[3]);
          int costScaling = Integer.parseInt(comps[4]);
          int health = Integer.parseInt(comps[6]);

          String[] visionComps = comps[7].split("\\+");
          int vision = Integer.parseInt(visionComps[0]);
          boolean hasEagleEye = visionComps.length >= 2 && visionComps[1].equals("Eagle Eye");

          Stats stats =
              new Stats(
                  new Stat(StatType.MAX_HEALTH, health), new Stat(StatType.VISION_RANGE, vision));

          List<Terrain> validTerrain =
              Arrays.stream(comps[8].split("-"))
                  .map(s -> Terrain.valueOf(s.toUpperCase()))
                  .collect(Collectors.toList());
          String buildingType = comps[9];

          Building building;
          switch (buildingType) {
            case "Temple":
              building = new Temple(null, null);
              break;
            case "NoEffectBuilding":
              building =
                  new NoEffectBuilding(
                      null, name, imageFilename, level, baseCost, costScaling, null, stats);
              break;
            case "SummonerBuilding":
              EffectPair<Integer> summoningRadii = getSummoningRadii(name);
              building =
                  new SummonerBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      null,
                      stats,
                      summoningRadii.nonAncientGroundEffect,
                      summoningRadii.ancientGroundEffect);
              break;
            case "StartOfTurnEffectBuilding":
              EffectPair<StartOfTurnEffect> startOfTurnEffects = getStartOfTurnEffects(name);
              building =
                  new StartOfTurnEffectBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      null,
                      stats,
                      startOfTurnEffects.nonAncientGroundEffect,
                      startOfTurnEffects.ancientGroundEffect);
              break;
            case "AllUnitModifierBuilding":
              EffectPair<ModifierBundle> allUnitModifierEffects = getAllUnitModifierEffects(name);
              building =
                  new AllUnitModifierBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      null,
                      stats,
                      allUnitModifierEffects.nonAncientGroundEffect,
                      allUnitModifierEffects.ancientGroundEffect);
              break;
            case "PlayerModifierBuilding":
              EffectPair<PlayerModifierEffect> playerModifierEffects = getPlayerModifierEffects(name);
              building =
                  new PlayerModifierBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      null,
                      stats,
                      playerModifierEffects.nonAncientGroundEffect,
                      playerModifierEffects.ancientGroundEffect);
              break;
            default:
              throw new RuntimeException("Got unknown building type: " + buildingType);
          }

          buildings.add(building);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    BUILDINGS = Collections.unmodifiableList(buildings);
  }

  /** Utility class representing a pair of effects. */
  private static final class EffectPair<E> {
    private final E nonAncientGroundEffect;
    private final E ancientGroundEffect;

    private EffectPair(E nonAncientGroundEffect, E ancientGroundEffect) {
      this.nonAncientGroundEffect = nonAncientGroundEffect;
      this.ancientGroundEffect = ancientGroundEffect;
    }

    private static <E> EffectPair<E> of(E nonAncientGroundEffect, E ancientGroundEffect) {
      return new EffectPair<>(nonAncientGroundEffect, ancientGroundEffect);
    }
  }

  /** Helper to get summon radii by building name. Throws for unknown name. */
  public static EffectPair<Integer> getSummoningRadii(String buildingName) {
    switch (buildingName) {
      case "Portal":
        return EffectPair.of(1, 3);
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /** Helper to get StartOfTurnEffects by building name. Throws for unknown name. */
  private static EffectPair<StartOfTurnEffect> getStartOfTurnEffects(String buildingName) {
    switch (buildingName) {
      case "Well":
        return EffectPair.of(
            new StartOfTurnEffect(StartOfTurnEffectType.MANA_GENERATION, 30, null),
            new StartOfTurnEffect(StartOfTurnEffectType.MANA_GENERATION, 100, null));
      case "Library":
        return EffectPair.of(
            new StartOfTurnEffect(StartOfTurnEffectType.RESEARCH_GAIN, 5, null),
            new StartOfTurnEffect(StartOfTurnEffectType.RESEARCH_GAIN, 15, null));
      case "Fountain":
        return EffectPair.of(
            new StartOfTurnEffect(
                StartOfTurnEffectType.HEAL_COMBATANT,
                15,
                ExpandableCloud.create(ExpandableCloudType.CIRCLE, 1)),
            new StartOfTurnEffect(
                StartOfTurnEffectType.HEAL_COMBATANT,
                25,
                ExpandableCloud.create(ExpandableCloudType.CIRCLE, 2)));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /** Helper to get AllUnitModifierEffects by building name. Throws for unknown name. */
  private static EffectPair<ModifierBundle> getAllUnitModifierEffects(String buildingName) {
    switch (buildingName) {
      case "Armory":
        return EffectPair.of(
            new ModifierBundle(
                new StatModifier(
                    "Strengthened Min Atk - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.MIN_ATTACK,
                    ModificationType.ADD,
                    3),
                new StatModifier(
                    "Strengthened Max Atk - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.MAX_ATTACK,
                    ModificationType.ADD,
                    3)),
            new ModifierBundle(
                new StatModifier(
                    "Strengthened Min Atk - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.MIN_ATTACK,
                    ModificationType.ADD,
                    3),
                new StatModifier(
                    "Strengthened Max Atk - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.MAX_ATTACK,
                    ModificationType.ADD,
                    3),
                new StatModifier(
                    "Toughness - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.DAMAGE_REDUCTION,
                    ModificationType.ADD,
                    3)));
      case "Comms Tower":
        return EffectPair.of(
            new ModifierBundle(
                new StatModifier(
                    "Farsight - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.VISION_RANGE,
                    ModificationType.ADD,
                    2),
                new StatModifier(
                    "Quickness - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.MOVEMENT_TOTAL,
                    ModificationType.ADD,
                    1)),
            new ModifierBundle(
                new StatModifier(
                    "Farsight - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.VISION_RANGE,
                    ModificationType.ADD,
                    3),
                new StatModifier(
                    "Quickness - Armory",
                    Integer.MAX_VALUE,
                    StackMode.STACKABLE,
                    StatType.MOVEMENT_TOTAL,
                    ModificationType.ADD,
                    2)));
      case "Cemetery":
        return EffectPair.of(new ModifierBundle(), new ModifierBundle());
      case "Siege Works":
        return EffectPair.of(new ModifierBundle(), new ModifierBundle());
      case "Military Academy":
        return EffectPair.of(new ModifierBundle(), new ModifierBundle());
      case "Sanctuary":
        return EffectPair.of(new ModifierBundle(), new ModifierBundle());
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /** Helper to get PlayerModifier effects for the given building name. Throws for unknown name. */
  private static EffectPair<PlayerModifierEffect> getPlayerModifierEffects(String buildingName) {
    switch (buildingName) {
      case "Laboratory":
        return EffectPair.of(
            null, new PlayerModifierEffect(PlayerModifierEffectType.CAST_CLOUD_BOOST, 1));
      case "Ritual Grounds":
        return EffectPair.of(
            new PlayerModifierEffect(PlayerModifierEffectType.SUMMON_DISCOUNT, 15),
            new PlayerModifierEffect(PlayerModifierEffectType.SUMMON_DISCOUNT, 30));
      case "Archive":
        return EffectPair.of(
            new PlayerModifierEffect(PlayerModifierEffectType.BUILD_DISCOUNT, 20),
            new PlayerModifierEffect(PlayerModifierEffectType.BUILD_DISCOUNT, 40));
      case "Studio":
        return EffectPair.of(
            new PlayerModifierEffect(PlayerModifierEffectType.CAST_DISCOUNT, 20),
            new PlayerModifierEffect(PlayerModifierEffectType.CAST_DISCOUNT, 40));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  ////////////////////
  // END CONSTRUCTION METHODS - BELOW HERE IS API.
  ///////////////////

  /** Returns a list of all buildings for the given level. */
  public static List<Building> getBuildingsForLevel(int level) {
    return BUILDINGS.stream().filter(b -> b.level == level).collect(Collectors.toList());
  }
}
