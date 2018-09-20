package util;

import model.game.Player;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for printing game results to file or terminal.
 *
 * @author Mshnik
 */
public final class ResultsPrinter {

  /** Place to write result output to. Defaults to System.out. */
  public static PrintStream resultsOutputStream = System.out;

  /** Place to write config output to. Defaults to System.out. */
  public static PrintStream configOutputStream = System.out;

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
