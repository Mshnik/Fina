package model.game;

import ai.AIAction;
import ai.AIController;
import model.board.Tile;
import model.unit.MovingUnit;
import model.unit.combatant.Combat;
import model.unit.combatant.Combatant;

import java.awt.Color;
import java.util.LinkedList;

/** A player controlled by an AI. */
public final class AIPlayer extends Player {

  /**
   * Causes the AIPlayer thread to sleep between executing each action If playing against at least
   * one human opponent.
   */
  private static final int SLEEP_TIME_BETWEEN_ACTIONS_AGAINST_HUMAN = 10;

  /** The controller handling behavior specification for this AIPlayer. */
  private final AIController aiController;

  /** Constructor for Player class with just model.game. */
  public AIPlayer(Game g, Color c, AIController aiController) {
    super(g, c);
    this.aiController = aiController;
  }

  /** Handles the given action. */
  private void handleAction(AIAction action) {
    switch (action.actionType) {
      case MOVE_UNIT:
        ((MovingUnit) action.actingUnit).move(action.movePath);
        break;
      case ATTACK:
        Combat combat =
            new Combat((Combatant) action.actingUnit, action.targetedTile.getOccupyingUnit());
        combat.process(game.getController().getCombatRandom());
        break;
      case SUMMON_COMBATANT_OR_BUILD_BUILDING:
        game.getController().summonUnit(action.actingUnit, action.targetedTile, action.unitToSummon);
        break;
      case CAST_SPELL:
        throw new RuntimeException("Unimplemented");
      default:
        throw new RuntimeException("Got unhandled actionType: " + action);
    }
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

  @Override
  public boolean isLocalHumanPlayer() {
    return false;
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
