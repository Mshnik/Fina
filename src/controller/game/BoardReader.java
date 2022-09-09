package controller.game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.board.Board;
import model.board.Terrain;
import model.util.MPoint;

/**
 * Input class to read a board by filepath into memory.
 *
 * @author Mshnik
 */
public final class BoardReader {
  private BoardReader() {
  }

  /**
   * Root filepath for board files.
   */
  public static final String BOARDS_ROOT_FILEPATH = "game/boards/";

  /**
   * Special character for where a player starts.
   */
  private static final String PLAYER_START_STRING = "C";

  /**
   * Terrain to use for player start spaces.
   */
  private static final Terrain PLAYER_START_TERRAIN = Terrain.GRASS;

  /**
   * Reads a board from memory at the given csvFilepath.
   */
  public static Board readBoard(String boardFilepath) {
    List<String> fileLines;
    try {
      fileLines = Files.readAllLines(Paths.get(boardFilepath));
    } catch (IOException e) {
      throw new RuntimeException("Error reading file", e);
    }
    int rows = fileLines.size();

    Terrain[][] terrainArr = new Terrain[rows][];
    String[][] additionalInfo = new String[rows][];
    List<MPoint> playerStartLocations = new ArrayList<>();

    int row = 0;
    for (String line : fileLines) {
      String[] terrainRow = line.split(",");
      terrainArr[row] = new Terrain[terrainRow.length];
      additionalInfo[row] = new String[terrainRow.length];
      for (int col = 0; col < terrainRow.length; col++) {
        String str = terrainRow[col].toUpperCase();
        if (PLAYER_START_STRING.equals(str.substring(0, PLAYER_START_STRING.length()))) {
          terrainArr[row][col] = PLAYER_START_TERRAIN;
          playerStartLocations.add(MPoint.get(row, col));
        } else {
          terrainArr[row][col] = Terrain.valueOfShort(str.substring(0, 1));
          additionalInfo[row][col] = str.length() > 1 ? str.substring(1) : null;
        }
      }
      row++;
    }

    // Randomize starting locations.
    Collections.shuffle(playerStartLocations);

    return new Board(boardFilepath, terrainArr, additionalInfo, playerStartLocations);
  }
}
