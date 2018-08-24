package controller.game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import model.board.Board;
import model.board.Terrain;
import model.util.MPoint;

/**
 * Input class to read a board by filepath into memory.
 *
 * @author Mshnik
 */
final class BoardReader {
  private BoardReader() {}

  /** Special character for where a player starts. */
  private static final String PLAYER_START_STRING = "C";

  /** Terrain to use for player start spaces. */
  private static final Terrain PLAYER_START_TERRAIN = Terrain.GRASS;

  /** Reads a board from memory at the given csvFilepath. */
  static Board readBoard(String csvFilepath) {
    List<String> fileLines;
    try {
      fileLines = Files.readAllLines(Paths.get(csvFilepath));
    } catch (IOException e) {
      throw new RuntimeException("Error reading file", e);
    }
    int rows = fileLines.size();

    Terrain[][] terrainArr = new Terrain[rows][];
    List<MPoint> playerStartLocations = new LinkedList<>();

    int row = 0;
    for (String line : fileLines) {
      String[] terrainRow = line.split(",");
      terrainArr[row] = new Terrain[terrainRow.length];
      for (int col = 0; col < terrainRow.length; col++) {
        String str = terrainRow[col].toUpperCase();
        if (PLAYER_START_STRING.equals(str)) {
          terrainArr[row][col] = PLAYER_START_TERRAIN;
          playerStartLocations.add(new MPoint(row, col));
        } else {
          terrainArr[row][col] = Terrain.valueOfShort(str);
        }
      }
      row++;
    }

    return new Board(terrainArr, playerStartLocations);
  }
}
