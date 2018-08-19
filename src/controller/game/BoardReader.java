package controller.game;

import model.board.Board;
import model.board.Terrain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Input class to read a board by filepath into memory.
 * @author Mshnik
 */
public final class BoardReader {
  private BoardReader() {}

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

    int row = 0;
    for (String line : fileLines) {
      terrainArr[row] = Arrays.stream(line.split(","))
          .map(Terrain::valueOfShort)
          .collect(Collectors.toList())
          .toArray(new Terrain[0]);
      row++;
    }

    return new Board(terrainArr);
  }
}
