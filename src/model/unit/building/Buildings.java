package model.unit.building;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import model.board.Terrain;
import model.unit.modifier.PlayerModifier;
import model.unit.building.StartOfTurnEffectBuilding.StartOfTurnEffect;
import model.unit.building.StartOfTurnEffectBuilding.StartOfTurnEffectType;
import model.unit.building.SummonerBuilding.SummonerBuildingEffect;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.Modifiers;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;
import model.util.ExpandableCloud;
import model.util.ExpandableCloud.ExpandableCloudType;
import util.TextIO;

/**
 * Index of buildings read from storage.
 */
public final class Buildings {

  /**
   * List of all available buildings as read from storage.
   */
  private static final List<Building> BUILDINGS;

  /**
   * File containing the buildings, in csv format
   */
  private static final File BUILDINGS_FILE = new File("game/units/buildings.csv");

  static {
    List<Building> buildings = new LinkedList<>();

    try {
      String[] buildingLines = TextIO.read(BUILDINGS_FILE).split("\\n");
      for (String line : buildingLines) {
        String[] comps = line.split(",");
        if (comps.length == 0 || comps[0].isEmpty()) continue; // Blank line.
        if (comps[0].equals("Name")) continue; // Header line.
        if (comps[0].equals("SKIP")) continue; // Not fully implemented building.

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

          String nonAncientGroundEffectDescription = comps[10];
          String ancientGroundEffectDescription = comps[11];

          Building building;
          switch (buildingType) {
            case "Temple":
              building = new Temple(null);
              break;
            case "NoEffectBuilding":
              building =
                  new NoEffectBuilding(
                      null, name, imageFilename, level, baseCost, costScaling, validTerrain, stats);
              break;
            case "SummonerBuilding":
              EffectPair<SummonerBuilding.SummonerBuildingEffect> summoningRadii =
                  getSummoningEffects(name);
              building =
                  new SummonerBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      validTerrain,
                      new Stats(stats, new Stat(StatType.ACTIONS_PER_TURN, 1)),
                      summoningRadii.nonAncientGroundEffect,
                      summoningRadii.ancientGroundEffect);
              break;
            case "StartOfTurnEffectBuilding":
              EffectPair<StartOfTurnEffect> startOfTurnEffects =
                  getStartOfTurnEffects(
                      name, nonAncientGroundEffectDescription, ancientGroundEffectDescription);
              building =
                  new StartOfTurnEffectBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      validTerrain,
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
                      validTerrain,
                      stats,
                      allUnitModifierEffects.nonAncientGroundEffect,
                      allUnitModifierEffects.ancientGroundEffect);
              break;
            case "CommanderModifierBuilding":
              EffectPair<ModifierBundle> commanderModifierEffects =
                  getCommanderModifierEffects(name);
              building =
                  new CommanderModifierBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      validTerrain,
                      stats,
                      commanderModifierEffects.nonAncientGroundEffect,
                      commanderModifierEffects.ancientGroundEffect);
              break;
            case "PlayerModifierBuilding":
              EffectPair<List<PlayerModifier>> playerModifierEffects =
                  getPlayerModifierEffects(
                      name, nonAncientGroundEffectDescription, ancientGroundEffectDescription);
              building =
                  new PlayerModifierBuilding(
                      null,
                      name,
                      imageFilename,
                      level,
                      baseCost,
                      costScaling,
                      validTerrain,
                      stats,
                      playerModifierEffects.nonAncientGroundEffect,
                      playerModifierEffects.ancientGroundEffect);
              break;
            default:
              throw new RuntimeException("Got unknown building type: " + buildingType);
          }
          Modifiers.solid(.35).clone(building);
          if (hasEagleEye) {
            Modifiers.eagleEye().clone(building);
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

  /**
   * Utility class representing a pair of effects.
   */
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

  /**
   * Helper to get summon radii by building name. Throws for unknown name.
   */
  private static EffectPair<SummonerBuildingEffect> getSummoningEffects(String buildingName) {
    switch (buildingName) {
      case "Portal":
        return EffectPair.of(new SummonerBuildingEffect(1, 1), new SummonerBuildingEffect(3, 3));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /**
   * Helper to get StartOfTurnEffects by building name. Throws for unknown name.
   */
  private static EffectPair<StartOfTurnEffect> getStartOfTurnEffects(
      String buildingName, String nonAncientGroundDescription, String ancientGroundDescription) {
    switch (buildingName) {
      case "Fountain":
        return EffectPair.of(
            new StartOfTurnEffect(
                StartOfTurnEffectType.HEAL_COMBATANT,
                5,
                ExpandableCloud.create(ExpandableCloudType.SQUARE, 1),
                nonAncientGroundDescription),
            new StartOfTurnEffect(
                StartOfTurnEffectType.HEAL_COMBATANT,
                20,
                ExpandableCloud.create(ExpandableCloudType.SQUARE, 2),
                ancientGroundDescription));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /**
   * Helper to get AllUnitModifierEffects by building name. Throws for unknown name.
   */
  private static EffectPair<ModifierBundle> getAllUnitModifierEffects(String buildingName) {
    switch (buildingName) {
      case "Armory":
        return EffectPair.of(
            Modifiers.strengthened(2),
            new ModifierBundle(Modifiers.strengthened(5), Modifiers.toughness(3)));
      case "Comms Tower":
        return EffectPair.of(
            new ModifierBundle(Modifiers.communications(1, 0)),
            new ModifierBundle(Modifiers.communications(3, 2)));
      case "Cemetery":
        return EffectPair.of(
            new ModifierBundle(Modifiers.bloodlust(.05)),
            new ModifierBundle(Modifiers.bloodlust(.2)));
      case "Siege Works":
        return EffectPair.of(
            new ModifierBundle(Modifiers.siege(.25)), new ModifierBundle(Modifiers.siege(1)));
      case "Military Academy":
        return EffectPair.of(
            new ModifierBundle(Modifiers.patience(.05)),
            new ModifierBundle(Modifiers.patience(.2)));
      case "Sanctuary":
        return EffectPair.of(
            new ModifierBundle(Modifiers.hexproof(.1)), new ModifierBundle(Modifiers.hexproof(.4)));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /**
   * Helper to get CommanderModifierEffects by building name. Throws for unknown name.
   */
  private static EffectPair<ModifierBundle> getCommanderModifierEffects(String buildingName) {
    switch (buildingName) {
      case "Dojo":
        return EffectPair.of(
            new ModifierBundle(Modifiers.training(1)), new ModifierBundle(Modifiers.training(3)));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  /**
   * Helper to get PlayerModifier effects for the given building name. Throws for unknown name.
   */
  private static EffectPair<List<PlayerModifier>> getPlayerModifierEffects(
      String buildingName, String nonAncientGroundDescription, String ancientGroundDescription) {
    switch (buildingName) {
      case "Well":
        return EffectPair.of(
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.MANA_GENERATION, 50, nonAncientGroundDescription)),
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.MANA_GENERATION, 150, ancientGroundDescription)));
      case "Library":
        return EffectPair.of(
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.RESEARCH_GENERATION, 25, nonAncientGroundDescription)),
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.RESEARCH_GENERATION, 75, ancientGroundDescription)));
      case "Laboratory":
        return EffectPair.of(
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.CAST_SELECT_BOOST, 1, nonAncientGroundDescription)),
            Arrays.asList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.CAST_SELECT_BOOST, 1, ancientGroundDescription),
                new PlayerModifier(PlayerModifier.PlayerModifierType.CAST_CLOUD_BOOST, 1, "")));
      case "Ritual Grounds":
        return EffectPair.of(
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.SUMMON_DISCOUNT, 10, nonAncientGroundDescription)),
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.SUMMON_DISCOUNT, 30, ancientGroundDescription)));
      case "Archive":
        return EffectPair.of(
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.CAST_DISCOUNT, 15, nonAncientGroundDescription)),
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.CAST_DISCOUNT, 40, ancientGroundDescription)));
      case "Studio":
        return EffectPair.of(
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.BUILD_DISCOUNT, 15, nonAncientGroundDescription)),
            Collections.singletonList(
                new PlayerModifier(
                    PlayerModifier.PlayerModifierType.BUILD_DISCOUNT, 40, ancientGroundDescription)));
      default:
        throw new RuntimeException("Unknown building name " + buildingName);
    }
  }

  ////////////////////
  // END CONSTRUCTION METHODS - BELOW HERE IS API.
  ///////////////////

  /**
   * Returns a list of all buildings..
   */
  public static List<Building> getBuildings() {
    return Collections.unmodifiableList(BUILDINGS);
  }

  /**
   * Returns a list of all buildings for the given level.
   */
  public static List<Building> getBuildingsForLevel(int level) {
    return BUILDINGS.stream().filter(b -> b.level == level).collect(Collectors.toList());
  }
}
