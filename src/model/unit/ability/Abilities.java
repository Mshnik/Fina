package model.unit.ability;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import model.unit.Unit;
import model.unit.building.Building;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.util.ExpandableCloud;
import model.util.ExpandableCloud.ExpandableCloudType;
import util.TextIO;

/** Index of abilities commanders can use. */
public final class Abilities {

  /** List of all available abilities as read from storage. */
  private static final List<Ability> ABILITIES;

  /** File containing the abilities, in csv format */
  private static final File ABILITIES_FILE = new File("game/units/spells.csv");

  static {
    List<Ability> abilities = new LinkedList<>();
    try {
      String[] abilityLines = TextIO.read(ABILITIES_FILE).split("\\n");

      for (String line : abilityLines) {
        String[] comps = line.split(",");
        if (comps.length == 0 || comps[0].isEmpty()) continue; // Blank line.
        if (comps[0].equals("Name")) continue; // Header line.
        if (comps[0].equals("SKIP")) continue; // Not fully implemented building.

        try {
          String name = comps[0];
          int level = Integer.parseInt(comps[1]);
          int manaCost = Integer.parseInt(comps[2]);

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

          Ability ability =
              new Ability(
                  name,
                  level,
                  manaCost,
                  ExpandableCloud.create(cloudType, cloudRadius),
                  canBeCloudBoosted,
                  castDist,
                  affectedUnitTypes,
                  affectsAllied,
                  affectsEnemy,
                  description,
                  getEffectsFor(name));
          abilities.add(ability);

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ABILITIES = Collections.unmodifiableList(abilities);
  }

  /** Helper for construction to get the list of ability effects for an ability. */
  private static List<AbilityEffect> getEffectsFor(String abilityName) {
    return Collections.emptyList();
  }

  /**
   * Returns a list of units for the given age (minus 1 because age is 1 indexed, this is 0 indexed)
   * - returns by value
   */
  public static List<Ability> getAbilitiesForAge(int age) {
    return ABILITIES.stream().filter(a -> a.level == age).collect(Collectors.toList());
  }
}
