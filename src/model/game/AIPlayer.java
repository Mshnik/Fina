package model.game;

import ai.AIAction;
import ai.AIController;
import java.awt.Color;

/** A player controlled by an AI. */
public final class AIPlayer extends Player {

  /**
   * Causes the AIPlayer thread to sleep between executing each action If playing against at least
   * one human opponent.
   */
  private static final int SLEEP_TIME_BETWEEN_ACTIONS_AGAINST_HUMAN = 100;

  /** The controller handling behavior specification for this AIPlayer. */
  private final AIController aiController;

  /** Constructor for Player class with just model.game. */
  public AIPlayer(Game g, Color c, AIController aiController) {
    super(g, c);
    this.aiController = aiController;
  }

  /** Handles the given action. */
  private void handleAction(AIAction action) {
    // TODO: handle action.
  }

  /** Sleeps for a short period of time, for realism against human. */
  private void sleepIfHumanOpponent() {
    if (SLEEP_TIME_BETWEEN_ACTIONS_AGAINST_HUMAN > 0) {
      try {
        Thread.sleep(SLEEP_TIME_BETWEEN_ACTIONS_AGAINST_HUMAN);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Executes the turn for the AI - executes next actions as long as there is one, then terminates.
   */
  @Override
  protected final void turn() {
    AIAction action = aiController.getNextAction(this);
    while (action != null) {
      handleAction(action);
      sleepIfHumanOpponent();
      action = aiController.getNextAction(this);
    }
    sleepIfHumanOpponent();
  }

  /**
   * Causes the player's turn to end. Shouldn't be needed in AIPlayer since turn() should just
   * terminate when it runs out of actions to do. Can be overridden if necessary.
   */
  @Override
  public void endTurn() {}
}
