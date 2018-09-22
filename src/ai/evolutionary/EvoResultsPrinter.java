package ai.evolutionary;

import util.ResultsPrinter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/** Printer for specifically printing results from evolution simulation rounds. */
final class EvoResultsPrinter {

  /** Root for output files. */
  private static final String ROOT_OUTPUT_FILEPATH = "data/aiLogs/evo/";

  /** Simulation results mainPrintStream, where averaging results are written out to. */
  private final PrintStream mainPrintStream;

  /** Individual game results printStream. */
  private final PrintStream gameResultsStream;

  /** Individual AI config printStream. */
  private final PrintStream aiConfigStream;

  /** Constructs a new EvoResultsPrinter pointing at a unique (time at start) file. */
  EvoResultsPrinter(String id) {
    try {
      mainPrintStream =
          new PrintStream(
              new FileOutputStream(
                  ROOT_OUTPUT_FILEPATH + "SimulationResults-" + id + ".csv", true));
      gameResultsStream =
          new PrintStream(
              new FileOutputStream(ROOT_OUTPUT_FILEPATH + "/results-" + id + ".txt", true));
      aiConfigStream =
          new PrintStream(
              new FileOutputStream(ROOT_OUTPUT_FILEPATH + "/configs-" + id + ".txt", true));
      ResultsPrinter.setOutputStreams(gameResultsStream, aiConfigStream);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /** Write a divider row in results and config between rounds. */
  void writeRoundDividerRowToResultsAndConfig(int round) {
    gameResultsStream.println("--- ROUND " + round + " ---");
    aiConfigStream.println("--- ROUND " + round + " ---");
  }

  /** Writes a header row to the simulation file. */
  void writeSimulationHeaderRow(EvoPlayer player) {
    mainPrintStream.println(
        String.format(
            "Round,Players,%s",
            player.getWeightsHeader().stream().collect(Collectors.joining(","))));
  }

  /**
   * Writes a row to the simulation file for the average attributes of the players list at this
   * point in time.
   */
  void writeSimulationRoundRow(int round, int playerCount, List<Double> averageWeights) {
    mainPrintStream.println(
        String.format(
            "Round %d,%d,%s",
            round,
            playerCount,
            averageWeights
                .stream()
                .map(d -> String.format("%.3f", d))
                .collect(Collectors.joining(","))));
  }
}
