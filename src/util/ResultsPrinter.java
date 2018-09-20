package util;

import model.game.Player;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for printing game results to file or terminal.
 *
 * @author Mshnik
 */
public final class ResultsPrinter {

  /** Root for output files. */
  private static final String ROOT_OUTPUT_FILEPATH = "data/aiLogs/randomDelegatingAI/";

  /** Place to write result output to. Defaults to System.out. */
  private static PrintStream resultsOutputStream;

  /** Place to write config output to. Defaults to System.out. */
  private static PrintStream configOutputStream;

  /* Set up output streams. */
  static {
    try {
      resultsOutputStream =
          new PrintStream(new FileOutputStream(ROOT_OUTPUT_FILEPATH + "/results.txt", true));
      configOutputStream =
          new PrintStream(new FileOutputStream(ROOT_OUTPUT_FILEPATH + "/configs.txt", true));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Prints a results row for the given player. allPlayers may contain the player, but it will be
   * filtered out.
   */
  public static void printResults(Player player, boolean win, List<Player> allPlayers) {
    resultsOutputStream.print(player.getIdString());
    resultsOutputStream.print(String.format(" [%s] vs ", win ? "WIN" : "LOSE"));
    resultsOutputStream.println(
        allPlayers
            .stream()
            .filter(p -> p != player)
            .map(Player::getIdString)
            .collect(Collectors.joining(",")));
  }

  /** Prints a config row for the given player. */
  public static void printConfig(Player player) {
    configOutputStream.println(player.getConfigString());
  }
}
