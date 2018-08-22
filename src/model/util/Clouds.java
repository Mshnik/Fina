package model.util;

import model.board.MPoint;
import util.TextIO;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Listing of clouds loaded into memory.
 *
 * @author Mshnik
 */
public final class Clouds {

  /** File containing the units, in csv format */
  private static final File CLOUDS_FILE = new File("game/util/clouds.csv");

  /** Map of cloud type to clouds, in order by level. */
  private static Map<Cloud.CloudType, Map<Integer, Cloud>> CLOUDS_MAP;

  /* Initialize clouds map. */
  static {
    CLOUDS_MAP = new HashMap<>();

    for (Cloud.CloudType type : Cloud.CloudType.values()) {
      CLOUDS_MAP.put(type, new HashMap<>());
    }

    try {
      String[] cloudLines = TextIO.read(CLOUDS_FILE).split("\\n");
      for (String line : cloudLines) {
        String[] components = line.split(",");
        if (components.length == 0) continue; // Empty line
        if (components[0].equals("Type")) continue; // Header

        Cloud.CloudType type = Cloud.CloudType.valueOf(components[0].toUpperCase());
        int level = Integer.parseInt(components[1]);
        String matrixString = components[4].replaceAll(" ", "");

        if (CLOUDS_MAP.get(type).containsKey(level)) {
          throw new RuntimeException(
              "Already have cloud for type,level: " + CLOUDS_MAP.get(type).get(level));
        }
        CLOUDS_MAP.get(type).put(level, new FileCloud(parseCloudMatrix(matrixString), type, level));
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    for (Cloud.CloudType key : CLOUDS_MAP.keySet()) {
      CLOUDS_MAP.put(key, Collections.unmodifiableMap(CLOUDS_MAP.get(key)));
    }
    CLOUDS_MAP = Collections.unmodifiableMap(CLOUDS_MAP);
  }

  /**
   * Parses a matrix string of the form [[01010][101][0c]]... to a set of points. 0 is point not in
   * cloud, 1 is point in cloud, c is center of cloud.
   */
  private static Set<MPoint> parseCloudMatrix(String cloudMatrixString) {
    int centerRow = -1;
    int centerCol = -1;

    String[] rows = cloudMatrixString.toLowerCase().replaceAll("\\[\\[|\\]\\]", "").split("\\]\\[");

    for (int row = 0; row < rows.length; row++) {
      for (int col = 0; col < rows[row].length(); col++) {
        if (rows[row].charAt(col) == 'c') {
          if (centerRow != -1 && centerCol != -1) {
            throw new RuntimeException(
                "Can't set center twice, was already " + centerRow + ", " + centerCol);
          }
          centerRow = row;
          centerCol = col;
        }
      }
    }

    HashSet<MPoint> set = new HashSet<>();
    set.add(MPoint.ORIGIN);
    for (int row = 0; row < rows.length; row++) {
      for (int col = 0; col < rows[row].length(); col++) {
        if (rows[row].charAt(col) == '1') {
          set.add(new MPoint(row - centerRow, col - centerCol));
        }
      }
    }

    return Collections.unmodifiableSet(set);
  }

  /** Return a cloud for the given type and level, or throw if it doesn't exist. */
  public static Cloud getCloud(Cloud.CloudType cloudType, int level) {
    if (!CLOUDS_MAP.containsKey(cloudType)) {
      throw new RuntimeException("Invalid cloudType " + cloudType);
    }

    Map<Integer, Cloud> clouds = CLOUDS_MAP.get(cloudType);
    if (!clouds.containsKey(level)) {
      throw new RuntimeException("Invalid level " + level);
    }
    return clouds.get(level);
  }

  /** Cloud read from file. */
  private static final class FileCloud extends Cloud {

    /** Constructs a new cloud from file. */
    private FileCloud(Set<MPoint> points, CloudType cloudType, int level) {
      super(points, cloudType, level);
    }

    @Override
    public Cloud changeLevel(int levelDelta) {
      return getCloud(cloudType, level + levelDelta);
    }
  }
}
