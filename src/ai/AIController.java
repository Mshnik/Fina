package ai;

import model.game.Player;

/**
 * Controller interface held by an AI player to get the actions it should execute.
 */
public interface AIController {

  /**
   * The type to show on the ui for a AI controller with set weights.
   */
  String PROVIDED_AI_TYPE = "AI - Provided";

  /**
   * The id of this AIController, to set the id of the player. If unimplemented (returns empty),
   * uses timestamp-based id.
   */
  default String id() {
    return "";
  }

  /**
   * Called when this controller's turn starts. By default do nothing, can be overridden for
   * behavior.
   */
  default void turnStart(Player player) {
  }

  /**
   * Returns the next action this AI should execute, or null if it's done with its turn.
   */
  AIAction getNextAction(Player player);

  /**
   * Called when the given action failed to execute because it wasn't possible. May have been an
   * invalid action. Allows the AI to clean up state before trying a new action. Throws by default,
   * to surface errors.
   */
  default void actionFailed(Exception e, AIAction action) {
    throw new RuntimeException(e);
  }

  /**
   * Called when the given action was just executed successfully, to clean up state as necessary.
   * Does nothing by default
   */
  default void actionExecuted(AIAction action) {
  }

  /**
   * Returns a string representing the configuration of this AI. Used to create ML data.
   */
  String getConfigString();
}
