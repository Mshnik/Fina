package model.game;

import controller.game.GameController;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import model.board.Board;
import model.board.Tile;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.commander.Commander;

/** Unifying model that holds all sub-model classes */
public final class Game implements Runnable, Stringable {

  /** The controller for this Game. */
  private GameController controller;

  /** The players currently playing this model.game */
  private LinkedList<Player> players;

  /** The players remaining in the model.game */
  private HashMap<Player, Boolean> remainingPlayers;

  /**
   * The index of the current player. -1 before the model.game has started, in range [0 ...
   * players.size() - 1] afterwards
   */
  private int index;

  /** The model.board that represents this model.game */
  public final Board board;

  /** Different fog of war modes a game can have. */
  public enum FogOfWar {
    NONE(false, false),
    REGULAR(true, false),
    HIDE_ANCIENT_GROUND(true, true);

    /**
     * True iff squares the player can't see should be greyed out. Show terrain, but don't show
     * units.
     */
    public final boolean active;

    /** True iff ancient ground the player can't see should be painted as grass. */
    public final boolean hideAncientGround;

    FogOfWar(boolean active, boolean hideAncientGround) {
      this.active = active;
      this.hideAncientGround = hideAncientGround;
    }
  }

  /** The fog of war for this game */
  private final FogOfWar fogOfWar;

  /**
   * True if we are currently in between turns and should hide everything. Only used during fog of
   * war.
   */
  private boolean betweenTurnsFog;

  /** True if this model.game is currently running, false otherwise */
  private boolean running;

  public Game(Board b, FogOfWar fog) {
    board = b;
    fogOfWar = fog;
    betweenTurnsFog = false;
    players = new LinkedList<>();
    remainingPlayers = new HashMap<>();
    running = false;
    index = -1;
  }

  /** Returns the controller for this Game */
  public GameController getController() {
    return controller;
  }

  /** Sets the GameController. Should be used in GameController construction */
  public void setGameController(GameController c) {
    controller = c;
  }

  /** Tells the controller that this has changed, needs repainting */
  public void repaint() {
    controller.repaint();
  }

  /** Runs this model.game - has the players take turns until the model.game is over */
  @Override
  public void run() {
    running = true;
    index = 0;
    try {
      while (!isGameOver()) {
        repaint();
        nextTurn();
      }
    } finally {
      running = false;
    }
  }

  /** Returns true if this is currently running - the run method is executing */
  public boolean isRunning() {
    return running;
  }

  /** Adds the given player to this game */
  public void addPlayer(Player p) {
    if (remainingPlayers.containsKey(p))
      throw new IllegalArgumentException("Game " + this + " already has player " + p);
    players.add(p);
    remainingPlayers.put(p, true);
  }

  /**
   * Returns the player who's currently taking his turn. Returns null if the model.game isn't
   * running.
   */
  public Player getCurrentPlayer() {
    if (!running) return null;
    return players.getFirst();
  }

  /** Returns the remaining players in the model.game */
  public List<Player> getRemainingPlayers() {
    List<Player> remaining = new LinkedList<Player>();
    for (Player p : remainingPlayers.keySet()) {
      if (remainingPlayers.get(p)) remaining.add(p);
    }
    return remaining;
  }

  /** Returns all units in the model.game */
  public List<Unit> getUnits() {
    LinkedList<Unit> units = new LinkedList<Unit>();
    for (Player p : getRemainingPlayers()) {
      units.addAll(p.getUnits());
    }
    return units;
  }

  /** Refreshes all passive abilities on all units in the model.game */
  public void refreshPassiveAbilities() {
    List<Unit> units = getUnits();
    for (Player p : getRemainingPlayers()) {
      Commander c = p.getCommander();
      for (Ability a : c.getPassiveAbilities()) {
        for (Unit u : units) {
          a.remove(u);
        }
        a.cast(c.getLocation());
      }
    }
  }

  /** Returns the index of the current player, which rotates through as the players rotate. */
  public int getPlayerIndex() {
    return index;
  }

  /** @return the fogOfWar in this model.game */
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
   * Returns true if this model.game is ended (one of the termination conditions is true), false
   * otherwise. Returns true if there are more than 1 remaining player.
   */
  public boolean isGameOver() {
    int i = 0;
    for (Boolean b : remainingPlayers.values()) {
      if (b) i++;
    }
    return i <= 1;
  }

  /**
   * Takes the current player's turn, then advances the player. Throws a runtimeException if this is
   * called when the model.game isn't running.
   */
  private void nextTurn() throws RuntimeException {
    if (!running)
      throw new RuntimeException("Can't take turn for player - " + this + " isn't started");
    betweenTurnsFog = false;
    Player p = getCurrentPlayer();
    boolean ok = p.turnStart();
    if (ok) {
      controller.startTurnFor(p);
      p.turn();
      p.turnEnd();
    } else {
      remainingPlayers.put(p, false);
    }

    // Move this player to the end, inc players index
    players.remove(0);
    players.add(p);
    index = (index + 1) % players.size();

    // If fog, pause and wait for transfer of computer.
    if (getFogOfWar().active) {
      betweenTurnsFog = true;
      Player nextTurnPlayer = getCurrentPlayer();
      controller.frame.showPlayerChangeAlert(nextTurnPlayer);
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
