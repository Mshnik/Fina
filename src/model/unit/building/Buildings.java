package model.unit.building;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import model.board.Terrain;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;
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
        if (comps.length == 0) continue; // Blank line.
        if (comps[0].equals("Name")) continue; // Header line.

        try {
          String name = comps[0];
          String imageFilename = comps[1];
          int level = Integer.parseInt(comps[2]);
          int baseCost = Integer.parseInt(comps[3]);
          int costScaling = Integer.parseInt(comps[4]);
          int health = Integer.parseInt(comps[6]);

          String[] visionComps = comps[7].split(", ");
          int vision = Integer.parseInt(visionComps[0]);
          boolean hasEagleEye = visionComps.length >= 2 && visionComps[1].equals("Eagle Eye");

          List<Terrain> validTerrain =
              Arrays.stream(comps[8].split(", "))
                  .map(s -> Terrain.valueOf(s.toUpperCase()))
                  .collect(Collectors.toList());

          Building building =
              new StartOfTurnEffectBuilding(
                  null,
                  name,
                  imageFilename,
                  level,
                  baseCost,
                  costScaling,
                  null,
                  new Stats(
                      new Stat(StatType.MAX_HEALTH, health),
                      new Stat(StatType.VISION_RANGE, vision)),
                  null,
                  0,
                  null);

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

  /** Returns a list of all buildings for the given level. */
  public static List<Building> getBuildingsForLevel(int level) {
    return BUILDINGS.stream().filter(b -> b.level == level).collect(Collectors.toList());
  }
}
