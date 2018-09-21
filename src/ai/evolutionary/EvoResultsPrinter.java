package ai.evolutionary;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Printer for specifically printing results from evolution simulation rounds. */
final class EvoResultsPrinter {

  /** Root for output files. */
  private static final String ROOT_OUTPUT_FILEPATH = "data/aiLogs/evo/";

  /** Simulation results printStream, where results are written out to. */
  private final PrintStream printStream;

  /** Constructs a new EvoResultsPrinter pointing at a unique (time at start) file. */
  EvoResultsPrinter() {
    try {
      printStream =
          new PrintStream(
              new FileOutputStream(
                  ROOT_OUTPUT_FILEPATH + "SimulationResults-" + System.currentTimeMillis() + ".csv",
                  true));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes a row to the simulation file for the average attributes of the players list at this
   * point in time.
   */
  void writeSimulationRoundRow(int round, Set<EvoPlayer> playerList) {
    List<Double> averageWeights =
        playerList
            .stream()
            .map(EvoPlayer::getWeightsList)
            .reduce(
                (weights1, weights2) ->
                    IntStream.range(0, weights1.size())
                        .mapToObj(i -> weights1.get(i) + weights2.get(i))
                        .collect(Collectors.toList()))
            .map(list -> list.stream().map(d -> d / playerList.size()).collect(Collectors.toList()))
            .orElseThrow(() -> new RuntimeException("Expected at least one player"));
    printStream.println(
        String.format(
            "Round %d,%d,%s",
            round,
            playerList.size(),
            averageWeights
                .stream()
                .map(d -> String.format("%.3f", d))
                .collect(Collectors.joining(","))));
  }
}
