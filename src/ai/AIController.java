package ai;

import model.game.Player;

/** Controller interface held by an AI player to get the actions it should execute. */
public interface AIController {

  /** The type to show on the ui for a AI controller with set weights. */
  public static final String PROVIDED_AI_TYPE = "AI - Provided";

  /**
   * Called when this controller's turn starts. By default do nothing, can be overridden for
   * behavior.
   */
  public default void turnStart(Player player) {}

  /** Returns the next action this AI should execute, or null if it's done with its turn. */
  public AIAction getNextAction(Player player);

  /**
   * Called when the given action failed to execute because it wasn't possible. May have been an
   * invalid action. Allows the AI to clean up state before trying a new action. Throws by default,
   * to surface errors.
   */
  public default void actionFailed(Exception e, AIAction action) {
    throw new RuntimeException(e);
  }

  /**
   * Called when the given action was just executed successfully, to clean up state as necessary.
   * Does nothing by default
   */
  public default void actionExecuted(AIAction action) {}

  /** Returns a string representing the configuration of this AI. Used to create ML data. */
  public String getConfigString();
}
