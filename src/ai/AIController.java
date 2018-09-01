package ai;

import model.game.Player;

/** Controller interface held by an AI player to get the actions it should execute. */
public interface AIController {

  /** Returns the next action this AI should execute, or null if it's done with its turn. */
  public AIAction getNextAction(Player player);
}
