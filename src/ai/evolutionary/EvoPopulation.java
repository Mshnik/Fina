package ai.evolutionary;

import ai.AIController;
import ai.evolutionary.EvoPlayer.PointChangeResult;
import controller.game.CreatePlayerOptions;
import controller.game.GameController;
import model.game.Game.FogOfWar;
import model.game.Player;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A population of {@link EvoPlayer}s that will play against themselves, split, and knockout.
 */
final class EvoPopulation {

  /**
   * The max number of games to run at a time.
   */
  private static final int MAX_CONCURRENT_GAMES = 2;

  /**
   * Valid boards to test on. A random one will be picked for every game.
   */
  private final List<String> boardFilenames;

  /**
   * Random instance to use to get a board for each game.
   */
  private final Random boardChooserRandom;

  /**
   * The set of players in this population.
   */
  private final Set<EvoPlayer> playerSet;

  /**
   * Random instance to use to get a player to randomly duplicate as necessary.
   */
  private final Random extraPlayerMutationRandom;

  /**
   * The extra points stored up to cause a spontaneous mutation on a remaining player.
   */
  private int extraMutationPoints;

  /**
   * The writer to use for writing simulation results.
   */
  private final EvoResultsPrinter printer;

  /**
   * True once the simulation has started. Players shouldn't be added to the population from outside
   * once true
   */
  private boolean simulationStarted;

  EvoPopulation(String... boardFilenames) {
    this.boardFilenames = Arrays.asList(boardFilenames);
    boardChooserRandom = new Random();
    extraPlayerMutationRandom = new Random();
    playerSet = new HashSet<>();
    simulationStarted = false;
    extraMutationPoints = 0;
    printer = new EvoResultsPrinter(Long.toString(System.currentTimeMillis()));
  }

  /**
   * Adds the given EvoPlayer to this population and returns this. Throws an exception if the
   * simulation has already started.
   */
  EvoPopulation addPlayer(EvoPlayer player) {
    if (simulationStarted) {
      throw new RuntimeException("Can't add player after simulation started");
    }
    playerSet.add(player);
    return this;
  }

  /**
   * Has the given two players play each other and returns a result.
   */
  private GameController startGameAndReturnController(EvoPlayer player1, EvoPlayer player2) {
    String boardFilename = boardFilenames.get(boardChooserRandom.nextInt(boardFilenames.size()));
    System.out.println(
        String.format(
            "|> Starting game on %s between %s and %s",
            boardFilename.replace(".csv", ""),
            player1.getController().id(),
            player2.getController().id()));
    return GameController.loadAndStartHeadless(
        "game/boards/" + boardFilename,
        Stream.of(player1.getController(), player2.getController())
            .map(controller -> new CreatePlayerOptions(AIController.PROVIDED_AI_TYPE, controller))
            .collect(Collectors.toList()),
        FogOfWar.REGULAR,
        1);
  }

  /**
   * Checks if there is a game that hasn't reached game over yet in the given list.
   */
  private boolean hasRunningGame(Collection<GameController> gameControllers) {
    return gameControllers.stream().map(g -> g.game).anyMatch(g -> !g.isGameOver());
  }

  /**
   * Runs the given set of players, pairing each with the next in the list (expects an even length).
   * Blocks until all games are finished. Returns the the set of results.
   */
  private List<EvoGameResult> runGamesBatch(List<EvoPlayer> playerList) {
    if (playerList.size() % 2 != 0) {
      throw new RuntimeException("Expected even playersLength size, got " + playerList.size());
    }
    // Start all games, collect controllers into list.
    List<GameController> gameControllers = new ArrayList<>();
    for (int i = 0; i < playerList.size() / 2; i++) {
      gameControllers.add(
          startGameAndReturnController(playerList.get(i * 2), playerList.get(i * 2 + 1)));
    }

    // Sleep until all games are done.
    System.out.print("|> Running");
    do {
      try {
        Thread.sleep(750);
        System.out.print(".");
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } while (hasRunningGame(gameControllers));
    System.out.println();

    // Collect results into list and return.
    List<EvoGameResult> results = new ArrayList<>();
    for (int i = 0; i < gameControllers.size(); i++) {
      EvoPlayer player1 = playerList.get(i * 2);
      EvoPlayer player2 = playerList.get(i * 2 + 1);
      List<Player> winnerPlayers = gameControllers.get(i).game.getRemainingPlayers();
      if (winnerPlayers.isEmpty()) {
        results.add(EvoGameResult.forTie(player1, player2));
      } else {
        Player winningPlayer = winnerPlayers.get(0);
        results.add(
            winningPlayer.index == 1
                ? EvoGameResult.forWinnerAndLoser(player1, player2)
                : EvoGameResult.forWinnerAndLoser(player2, player1));
      }
    }
    System.out.println("\\> Batch completed");
    System.out.println();
    return results;
  }

  /**
   * Handles the given result, making changes to the player set as necessary.
   */
  private void changePointsAndHandleResult(EvoPlayer player, boolean won) {
    PointChangeResult pointChangeResult = player.changePoints(won);
    switch (pointChangeResult) {
      case SPLIT:
        player.resetPoints();
        playerSet.add(player.split());
        return;
      case KNOCKOUT:
        playerSet.remove(player);
        return;
      case NO_CHANGE:
        // Do nothing.
        break;
    }
  }

  /**
   * Helper function that sums two lists of doubles. Expects the lists to be of the same length.
   */
  private static List<Double> sumLists(List<Double> list1, List<Double> list2) {
    return IntStream.range(0, list1.size())
        .mapToObj(i -> list1.get(i) + list2.get(i))
        .collect(Collectors.toList());
  }

  /**
   * Calculates the average weights of each delegate (including subweights).
   */
  private List<Double> calculateAverageWeights() {
    return playerSet
        .stream()
        .map(EvoPlayer::getWeightsList)
        .reduce(EvoPopulation::sumLists)
        .map(list -> list.stream().map(d -> d / playerSet.size()).collect(Collectors.toList()))
        .orElseThrow(() -> new RuntimeException("Expected at least one player"));
  }

  /**
   * Calculates the standard deviation of each weight (including subweights).
   */
  private List<Double> calculateWeightStandardDeviation(List<Double> averageWeights) {
    return playerSet
        .stream()
        .map(EvoPlayer::getWeightsList)
        // Map to list of (val - mean) ^ 2
        .map(
            weights ->
                IntStream.range(0, weights.size())
                    .mapToObj(
                        i ->
                            (weights.get(i) - averageWeights.get(i))
                                * (weights.get(i) - averageWeights.get(i)))
                    .collect(Collectors.toList()))
        .reduce(EvoPopulation::sumLists)
        // Divide by size (sum -> average), take square root.
        .map(
            list ->
                list.stream()
                    .map(d -> Math.pow(d / playerSet.size(), 0.5))
                    .collect(Collectors.toList()))
        .orElseThrow(() -> new RuntimeException("Expected at least one player"));
  }

  /**
   * Starts the simulation, running for the given number of iterations.
   */
  void runSimulation(int iterations) throws FileNotFoundException {
    // Start simulation.
    simulationStarted = true;
    int batchSize = MAX_CONCURRENT_GAMES * 2;
    printer.writeSimulationHeaderRow(playerSet.stream().findAny().get());

    // Iterate and write diagnostic row before beginning.
    for (int iteration = 0; iteration < iterations; iteration++) {
      System.out.println("Starting iteration " + iteration + " - " + playerSet.size() + " players");
      printer.writeRoundDividerRowToResultsAndConfig(iteration);
      printer.writeSimulationRoundRow(iteration, playerSet.size(), calculateAverageWeights());

      // Copy players to list and shuffle.
      List<EvoPlayer> playerList = new ArrayList<>(playerSet);
      Collections.shuffle(playerList);

      // Run games in batches batches.
      List<EvoGameResult> results = new ArrayList<>();
      int i = 0;
      int batches = playerList.size() / batchSize;
      int realBatchCount = batches + (playerList.size() % batchSize > 0 ? 1 : 0);
      for (; i < batches; i++) {
        System.out.println(
            "+ Batch " + (i + 1) + "/" + realBatchCount + " (" + batchSize + " players)");
        results.addAll(runGamesBatch(playerList.subList(i * batchSize, (i + 1) * batchSize)));
      }
      // Run remainder (less than one batch), rounding to down to multiple of 2 as necessary.
      // If there's an odd number, the final player won't play this round.
      int remainderBatchSize = (playerList.size() - i * batchSize) / 2 * 2;
      if (remainderBatchSize > 0) {
        System.out.println(
            "+ Batch " + (i + 1) + "/" + realBatchCount + " (" + remainderBatchSize + " players)");
        results.addAll(
            runGamesBatch(playerList.subList(i * batchSize, i * batchSize + remainderBatchSize)));
      }

      // Process results.
      System.out.println("> Done, evaluating results\n");
      for (EvoGameResult result : results) {
        if (result.hasWinner()) {
          changePointsAndHandleResult(result.getWinner(), true);
          changePointsAndHandleResult(result.getLoser(), false);
        } else {
          changePointsAndHandleResult(result.getPlayer1(), false);
          changePointsAndHandleResult(result.getPlayer2(), false);
          extraMutationPoints += EvoPlayer.DELTA_POINTS * 2;
          while (extraMutationPoints >= EvoPlayer.STARTING_POINTS) {
            EvoPlayer playerToMutate =
                playerSet
                    .stream()
                    .skip(extraPlayerMutationRandom.nextInt(playerSet.size()))
                    .findFirst()
                    .get();
            playerSet.add(playerToMutate.split());
            extraMutationPoints -= EvoPlayer.STARTING_POINTS;
          }
        }
      }
    }
    printer.writeSimulationRoundRow(iterations, playerSet.size(), calculateAverageWeights());
  }
}
