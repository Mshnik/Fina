package ai.evolutionary;

import static ai.AIController.PROVIDED_AI_TYPE;

import ai.evolutionary.EvoPlayer.PointChangeResult;
import controller.game.GameController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import model.game.Game.FogOfWar;
import model.game.Player;

/** A population of {@link EvoPlayer}s that will play against themselves, split, and knockout. */
final class EvoPopulation {

  /** The set of types for every game - always two provided AIs. */
  private static final List<String> PROVIDED_AI_TYPES_LIST =
      Stream.of(PROVIDED_AI_TYPE, PROVIDED_AI_TYPE).collect(Collectors.toList());

  /** The max number of games to run at a time. */
  private static final int MAX_CONCURRENT_GAMES = 3;

  private final String boardFilename;

  /** The set of players in this population. */
  private final Set<EvoPlayer> playerSet;

  /** The writer to use for writing simulation results. */
  private final EvoResultsPrinter printer;

  /**
   * True once the simulation has started. Players shouldn't be added to the population from outside
   * once true
   */
  private boolean simulationStarted;

  EvoPopulation(String boardFilename) {
    this.boardFilename = boardFilename;
    playerSet = new HashSet<>();
    simulationStarted = false;
    printer = new EvoResultsPrinter();
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

  /** Has the given two players play each other and returns a result. */
  private GameController startGameAndReturnController(EvoPlayer player1, EvoPlayer player2) {
    return GameController.loadAndStartHeadless(
        "game/boards/" + boardFilename,
        PROVIDED_AI_TYPES_LIST,
        Stream.of(player1.getController(), player2.getController()).collect(Collectors.toList()),
        FogOfWar.REGULAR,
        1);
  }

  /** Checks if there is a game that hasn't reached game over yet in the given list. */
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
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    while (hasRunningGame(gameControllers)) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

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
    return results;
  }

  /** Handles the given result, making changes to the player set as necessary. */
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

  /** Starts the simulation, running for the given number of iterations. */
  void runSimulation(int iterations) {
    simulationStarted = true;
    int batchSize = MAX_CONCURRENT_GAMES * 2;
    for (int iteration = 0; iteration < iterations; iteration++) {
      System.out.println("Starting iteration " + iteration + " - " + playerSet.size() + " players");
      printer.writeSimulationRoundRow(iteration, playerSet);

      // Copy players to list and shuffle.
      List<EvoPlayer> playerList = new ArrayList<>(playerSet);
      Collections.shuffle(playerList);

      // Run games in batches batches.
      List<EvoGameResult> results = new ArrayList<>();
      int i = 0;
      for (; i < playerList.size() / batchSize; i++) {
        System.out.println("> Batch " + i + "/" + (playerList.size() / batchSize));
        results.addAll(runGamesBatch(playerList.subList(i * batchSize, (i + 1) * batchSize)));
      }
      // Run remainder (less than one batch), rounding to down to multiple of 2 as necessary.
      // If there's an odd number, the final player won't play this round.
      int remainderBatchSize = (playerList.size() - i * batchSize) / 2 * 2;
      if (remainderBatchSize > 0) {
        System.out.println("> Batch " + i + "/" + (playerList.size() / batchSize));
        results.addAll(
            runGamesBatch(playerList.subList(i * batchSize, i * batchSize + remainderBatchSize)));
      }

      // Process results.
      System.out.println("Done, evaluating results");
      for (EvoGameResult result : results) {
        if (result.hasWinner()) {
          changePointsAndHandleResult(result.getWinner(), true);
          changePointsAndHandleResult(result.getLoser(), false);
        }
      }
    }
    printer.writeSimulationRoundRow(iterations, playerSet);
  }
}
