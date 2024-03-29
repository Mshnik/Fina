package model.game;

import ai.AIAction;
import ai.AIController;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import model.unit.MovingUnit;
import model.unit.combatant.Combat;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;

/**
 * A player controlled by an AI.
 */
public final class AIPlayer extends Player {

  /**
   * Id suffix to assign. Will be regenerated between different class loadings, but that should be
   * enough to guarantee id uniqueness.
   */
  private static final AtomicInteger idCounter = new AtomicInteger();

  /**
   * Causes the AIPlayer thread to sleep between executing each action If playing against at least
   * one human opponent.
   */
  private static final int SLEEP_TIME_BETWEEN_ACTIONS_AGAINST_HUMAN = 0;

  /**
   * A unique id assigned at construction time. Useful for ML logs.
   */
  private final String id;

  /**
   * The controller handling behavior specification for this AIPlayer.
   */
  private final AIController aiController;

  /**
   * Constructor for Player class with just model.game.
   */
  public AIPlayer(Game g, Color c, AIController aiController) {
    super(g, c);
    id =
        aiController.id().isEmpty()
            ? System.currentTimeMillis() + "-" + idCounter.getAndIncrement()
            : aiController.id();
    this.aiController = aiController;
  }

  /**
   * Handles the given action.
   */
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
        game.getController()
            .summonUnit(action.actingUnit, action.targetedTile, action.unitToSummon);
        break;
      case CAST_SPELL:
        game.getController()
            .castSpell((Commander) action.actingUnit, action.spellToCast, action.targetedTile);
        break;
      default:
        throw new RuntimeException("Got unhandled actionType: " + action.actionType);
    }
  }

  /**
   * Sleeps for a short period of time, for realism against human.
   */
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
    aiController.turnStart(this);
    AIAction action = aiController.getNextAction(this);
    while (action != null) {
      try {
        handleAction(action);
        aiController.actionExecuted(action);
      } catch (Exception e) {
        aiController.actionFailed(e, action);
      }
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
  public void endTurn() {
  }

  @Override
  public String getIdString() {
    return "AI " + id;
  }

  @Override
  public String getConfigString() {
    return getIdString() + " {" + aiController.getConfigString() + "}";
  }
}
