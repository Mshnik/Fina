package model.board;

/**
 * The types of terrain that can be represented by tiles.
 */
public enum Terrain {
  GRASS,
  WOODS,
  MOUNTAIN,
  SEA,
  ANCIENT_GROUND;

  /**
   * Parses the given short string to a Terrain.
   */
  public static Terrain valueOfShort(String string) {
    switch (string.toUpperCase()) {
      case "G":
        return GRASS;
      case "W":
        return WOODS;
      case "M":
        return MOUNTAIN;
      case "S":
        return SEA;
      case "A":
        return ANCIENT_GROUND;
      default:
        throw new RuntimeException("Unknown short terrain: " + string);
    }
  }
}
