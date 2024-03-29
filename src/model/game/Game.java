package model.game;

import controller.game.GameController;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.board.Board;
import model.board.Tile;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import util.ResultsPrinter;

/**
 * Unifying model that holds all sub-model classes
 */
public final class Game implements Runnable, Stringable {

  /**
   * Boolean for triggering debug output.
   */
  private static final boolean DEBUG = false;

  /**
   * Cutoff turn for testing + ml generation. If turn hits this number, winner is player with higher
   * commander HP, draw if tied. Only used if all players are AI.
   */
  private static final int CUT_OFF_TURN = 500;

  /**
   * The count of turns so far in this game. Initialized to 0, thus the first turn is turn 1.
   * Incremented at the start of turn, so the final number is the last turn number. Incremented on
   * every player's turn, so a single player's turn numbers will be different by # of players.
   */
  private int turn;

  /**
   * The controller for this Game.
   */
  private GameController controller;

  /**
   * The players currently playing this model.game
   */
  private final LinkedList<Player> players;

  /**
   * True if all players are AI, false otherwise.
   */
  private boolean allPlayersAI;

  /**
   * The observer player who is not playing. Only non-null if all players are AI and there is a
   * graphic component.
   */
  private Player observer;

  /**
   * The players currently playing this model.game, indexed by their playerIndex.
   */
  private final Map<Integer, Player> playersByIndex;

  /**
   * The players remaining in the model.game
   */
  private final HashMap<Player, Boolean> remainingPlayers;

  /**
   * The index of the current player. -1 before the model.game has started, in range [0 ...
   * players.size() - 1] afterwards
   */
  private int index;

  /**
   * The index of the most recent human player to take a turn. If no player, will be set to Observer
   * player.
   */
  private int mostRecentHumanPlayerIndex;

  /**
   * The model.board that represents this model.game
   */
  public final Board board;

  /**
   * Different fog of war modes a game can have.
   */
  public enum FogOfWar {
    NONE(false, false),
    REGULAR(true, false),
    HIDE_ANCIENT_GROUND(true, true);

    /**
     * True iff squares the player can't see should be greyed out. Show terrain, but don't show
     * units.
     */
    public final boolean active;

    /**
     * True iff ancient ground the player can't see should be painted as grass.
     */
    public final boolean hideAncientGround;

    FogOfWar(boolean active, boolean hideAncientGround) {
      this.active = active;
      this.hideAncientGround = hideAncientGround;
    }
  }

  /**
   * The fog of war for this game
   */
  private final FogOfWar fogOfWar;

  /**
   * True if we are currently in between turns and should hide everything. Only used during fog of
   * war.
   */
  private boolean betweenTurnsFog;

  /**
   * True if this model.game is currently running, false otherwise
   */
  private boolean running;

  public Game(Board b, FogOfWar fog) {
    turn = 0;
    board = b;
    fogOfWar = fog;
    betweenTurnsFog = false;
    players = new LinkedList<>();
    playersByIndex = new HashMap<>();
    remainingPlayers = new HashMap<>();
    running = false;
    index = -1;
    mostRecentHumanPlayerIndex = -1;
  }

  /**
   * Returns the controller for this Game
   */
  public GameController getController() {
    return controller;
  }

  /**
   * Sets the GameController. Should be used in GameController construction
   */
  public void setGameController(GameController c) {
    controller = c;
  }

  /**
   * Tells the controller that this has changed, needs repainting
   */
  public void repaint() {
    controller.repaint();
  }

  /**
   * Runs this model.game - has the players take turns until the model.game is over
   */
  @Override
  public void run() {
    running = true;
    index = 0;
    mostRecentHumanPlayerIndex =
        players
            .stream()
            .filter(Player::isLocalHumanPlayer)
            .map(p -> p.index)
            .findFirst()
            .orElse(observer != null ? observer.index : -1);
    allPlayersAI = players.stream().noneMatch(Player::isLocalHumanPlayer);
    try {
      while (running && !isGameOver()) {
        repaint();
        nextTurn();
      }
      if (isGameOver()) {
        Player winner = getRemainingPlayers().isEmpty() ? null : getRemainingPlayers().get(0);
        if (controller.hasFrame()) {
          controller.frame.showGameOverAlert(winner);
        }
        players.forEach(p -> ResultsPrinter.printResults(p, p == winner, players));
        players.forEach(ResultsPrinter::printConfig);
      }
    } finally {
      running = false;
    }
  }

  /**
   * Stops the current game by setting currently running to false.
   */
  public void kill() {
    running = false;
  }

  /**
   * Returns true if this is currently running - the run method is executing
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Adds the given player to this game
   */
  public void addPlayer(Player p) {
    if (remainingPlayers.containsKey(p))
      throw new IllegalArgumentException("Game " + this + " already has player " + p);
    players.add(p);
    playersByIndex.put(players.size(), p);
    remainingPlayers.put(p, true);
  }

  /**
   * Creates an observer player who isn't in the game. Returns the observer player.
   */
  public Player createObserverPlayer() {
    HumanPlayer observer = new HumanPlayer(this, Color.WHITE);
    players.remove(observer);
    remainingPlayers.remove(observer);
    this.observer = observer;
    return observer;
  }

  /**
   * Returns he current turn.
   */
  public int getTurn() {
    return turn;
  }

  /**
   * Returns the player who's currently taking his turn. Returns null if the model.game isn't
   * running.
   */
  public Player getCurrentPlayer() {
    if (!running) return null;
    return players.getFirst();
  }

  /**
   * Returns the most recent human player to taking a turn. Returns null if the model.game isn't
   * running.
   */
  public Player getMostRecentHumanPlayer() {
    return playersByIndex.get(mostRecentHumanPlayerIndex);
  }

  /**
   * Returns the remaining players in the model.game
   */
  public List<Player> getRemainingPlayers() {
    List<Player> remaining = new LinkedList<Player>();
    for (Player p : remainingPlayers.keySet()) {
      if (remainingPlayers.get(p)) remaining.add(p);
    }
    return remaining;
  }

  /**
   * Returns all units in the model.game
   */
  public List<Unit> getUnits() {
    LinkedList<Unit> units = new LinkedList<Unit>();
    for (Player p : getRemainingPlayers()) {
      units.addAll(p.getUnits());
    }
    return units;
  }

  /**
   * Returns all units in the model.game owned by players other than the given player
   */
  public List<Unit> getOtherPlayersUnits(Player player) {
    LinkedList<Unit> units = new LinkedList<Unit>();
    for (Player p : getRemainingPlayers()) {
      if (p != player) {
        units.addAll(p.getUnits());
      }
    }
    return units;
  }

  /**
   * Returns a joined map of all danger radii in the game for the given player - filters out
   * combatants the player can't see (And thus shouldn't know about).
   */
  public Map<Combatant, Set<Tile>> getDangerRadius(Player player) {
    Map<Combatant, Set<Tile>> dangerRadius = new HashMap<>();
    for (Player p : players) {
      Map<Combatant, Set<Tile>> dangerRadiusMap = p.getDangerRadius();
      synchronized (dangerRadiusMap) {
        dangerRadiusMap
            .entrySet()
            .stream()
            .filter(e -> player.canSee(e.getKey()))
            .forEach(e -> dangerRadius.put(e.getKey(), e.getValue()));
      }
    }

    return dangerRadius;
  }

  /**
   * Returns the index of the current player, which rotates through as the players rotate.
   */
  public int getPlayerIndex() {
    return index;
  }

  /**
   * @return the fogOfWar in this model.game
   */
  public FogOfWar getFogOfWar() {
    return fogOfWar;
  }

  /**
   * Returns true if the given tile is visible to the current player, false otherwise. Helper for
   * painting fog of war and hiding units
   */
  public boolean isVisible(Tile t) {
    return !getFogOfWar().active
        || (!betweenTurnsFog && getCurrentPlayer() != null && getCurrentPlayer().canSee(t));
  }

  /**
   * Returns true if the given tile is visible to the most recent human player to take a turn, false
   * otherwise. Helper for painting fog of war and hiding units. If no human player, don't paint fog
   * of war (but AIs will respect it).
   */
  public boolean isVisibleToMostRecentHumanPlayer(Tile t) {
    if (!getFogOfWar().active) {
      return true;
    }

    return !betweenTurnsFog
        && (observer != null && mostRecentHumanPlayerIndex == observer.index
        || playersByIndex.get(mostRecentHumanPlayerIndex).canSee(t));
  }

  /**
   * Returns true if this model.game is ended (one of the termination conditions is true), false
   * otherwise. Returns true if there are more than 1 remaining player.
   */
  public boolean isGameOver() {
    if (allPlayersAI && turn >= CUT_OFF_TURN) {
      // Game ends due to cut off - winner is player with highest commander health,
      // otherwise tie.
      int maxCommanderHealth =
          getRemainingPlayers()
              .stream()
              .mapToInt(p -> p.getCommander().getHealth())
              .max()
              .orElse(Integer.MAX_VALUE);
      boolean tie =
          getRemainingPlayers()
              .stream()
              .mapToInt(p -> p.getCommander().getHealth())
              .filter(i -> i == maxCommanderHealth)
              .count()
              > 1;
      for (Player p : remainingPlayers.keySet()) {
        if (tie || p.getCommander().getHealth() < maxCommanderHealth) {
          remainingPlayers.put(p, false);
        }
      }
      return true;
    } else {
      int i = 0;
      for (Player p : remainingPlayers.keySet()) {
        if (remainingPlayers.get(p) && p.isAlive()) {
          i++;
        } else {
          remainingPlayers.put(p, false);
        }
      }
      return i <= 1;
    }
  }

  /**
   * Takes the current player's turn, then advances the player. Throws a runtimeException if this is
   * called when the model.game isn't running.
   */
  private void nextTurn() throws RuntimeException {
    if (!running)
      throw new RuntimeException("Can't take turn for player - " + this + " isn't started");
    turn++;
    betweenTurnsFog = false;
    Player p = getCurrentPlayer();
    if (p.isLocalHumanPlayer()) {
      mostRecentHumanPlayerIndex = p.index;
    }
    if (DEBUG && !controller.hasFrame()) {
      System.out.println("Player " + p.index + "'s turn");
    }
    boolean ok = p.turnStart();
    if (ok) {
      controller.startTurnFor(p);
      p.turn();
    } else {
      remainingPlayers.put(p, false);
    }

    // Move this player to the end, inc players index.
    // Don't inc mostRecentHumanPlayerIndex yet.
    players.remove(0);
    players.add(p);
    index = (index + 1) % players.size();

    // If fog, pause and wait for transfer of computer.
    if (getFogOfWar().active) {
      Player nextTurnPlayer = getCurrentPlayer();
      if (nextTurnPlayer != null
          && nextTurnPlayer.isLocalHumanPlayer()
          && nextTurnPlayer.index != mostRecentHumanPlayerIndex) {
        betweenTurnsFog = true;
        if (controller.hasFrame()) {
          controller.frame.showPlayerChangeAlert(nextTurnPlayer);
        }
      }
    }
  }

  @Override
  public String toString() {
    String s = "Game of ";
    for (Player p : players) {
      s += p.toStringLong() + (remainingPlayers.get(p) ? "=Alive" : "=Dead") + " ";
    }
    return s;
  }

  @Override
  public String toStringShort() {
    return players.size() + "-Player Game";
  }

  @Override
  public String toStringLong() {
    String s = "Game of ";
    for (Player p : players) {
      s += p.toStringLong() + (remainingPlayers.get(p) ? "=Alive" : "=Dead") + " ";
    }
    return s;
  }

  @Override
  public String toStringFull() {
    String s = "Game of ";
    for (Player p : players) {
      s += p.toStringLong() + (remainingPlayers.get(p) ? "=Alive" : "=Dead") + " ";
    }
    s += "Fog Of War=" + (fogOfWar.active ? "On" : "Off") + board.toStringLong();
    return s;
  }
}
