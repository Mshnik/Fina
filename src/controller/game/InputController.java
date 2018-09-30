package controller.game;

import controller.audio.AudioController;
import controller.decision.Choice;
import controller.decision.Decision.DecisionType;
import controller.selector.PathSelector;
import model.board.Direction;
import model.board.Tile;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.commander.Commander;
import view.gui.Cursor;
import view.gui.Frame;
import view.gui.decision.DecisionCursor;
import view.gui.decision.DecisionPanel;
import view.gui.panel.BoardCursor;

/** Manager for user input coming from any source. */
public final class InputController {

  /** Constructor for InputController */
  private InputController() {}

  /** The frame this InputController is listening to */
  private static Frame frame;

  /** Set the Frame the InputController is listening to */
  static void setFrame(Frame f) {
    frame = f;
  }

  /** Handles hovering at the given x,y on the screen. */
  static void handleMouseHover(int x, int y) {
    if (frame.getActiveCursor() instanceof BoardCursor) {
      handleMouseHoverBoardCursorActive(x, y);
    } else if (frame.getActiveCursor() instanceof DecisionCursor) {
      handleMouseHoverDecisionCursorActive(x, y);
    }
  }

  /** Handles the mouse move event when the active cursor is a board cursor. */
  private static void handleMouseHoverBoardCursorActive(int cursorX, int cursorY) {
    BoardCursor boardCursor = (BoardCursor) frame.getActiveCursor();
    Tile currentElem = boardCursor.getElm();
    int tileSize = frame.getGamePanel().cellSize();
    // Account for header, menu, and title bar on top.
    cursorY -=
        frame.getHeaderPanel().getHeight() + frame.getMenu().getHeight() + frame.getInsets().top;
    Tile destTile;
    try {
      destTile =
          frame
              .getGamePanel()
              .getElmAtWithScrollingAndMargins(cursorY / tileSize, cursorX / tileSize);
    } catch (IllegalArgumentException ex) {
      return;
    }
    if (currentElem == destTile) {
      return;
    }
    if (frame.getController().getLocationSelector() instanceof PathSelector) {
      PathSelector pathSelector = (PathSelector) frame.getController().getLocationSelector();
      if (!pathSelector.contains(destTile)
          && (currentElem.directionTo(destTile) == null
              || !frame.getController().getLocationSelector().getCloud().contains(destTile))) {
        // Force adjacency or move backwards when movement selector is open.
        return;
      }
    } else if (frame.getController().getLocationSelector() != null
        && !frame.getController().getLocationSelector().getCloud().contains(destTile)) {
      // Other clouds - force element of cloud.
      return;
    }
    boardCursor.setElm(destTile);
    boardCursor.moved();
  }

  /** Handles the mouse move event when the active cursor is a decision cursor. */
  private static void handleMouseHoverDecisionCursorActive(int cursorX, int cursorY) {
    DecisionCursor decisionCursor = (DecisionCursor) frame.getActiveCursor();
    Choice currentElem = decisionCursor.getElm();
    int x = cursorX - frame.getGamePanel().getDecisionPanel().getDrawingX();
    int y = cursorY - frame.getGamePanel().getDecisionPanel().getDrawingY();
    Choice destChoice;
    try {
      destChoice =
          frame
              .getGamePanel()
              .getDecisionPanel()
              .getElmAtWithScrolling(
                  // Subtract 3.5 for title panel. Not sure why 3.5.
                  (int) (y - DecisionPanel.DECISION_HEIGHT * 3.5) / DecisionPanel.DECISION_HEIGHT,
                  x / frame.getGamePanel().getDecisionPanel().DECISION_WIDTH);
    } catch (IllegalArgumentException ex) {
      return;
    }
    if (currentElem == destChoice) {
      return;
    }
    decisionCursor.setElm(destChoice);
    decisionCursor.moved();
  }

  /** Handles a directional input. Usually keyboard-only, one of the arrow keys. */
  static void handleDirection(Direction d) {
    @SuppressWarnings("rawtypes")
    Cursor c = frame.getActiveCursor();
    c.move(d);
  }

  /** Handles a confirm or cancel input. */
  static void handleConfirmOrCancel(boolean confirm) {
    GameController gc = frame.getController();
    switch (gc.getToggle()) {
        // No toggle currently open. Maybe open one.
      case NONE:
        if (confirm) {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          gc.startActionDecision();
        } else {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          gc.startPlayerActionDecision();
        }
        break;
        // Process the decision.
      case DECISION:
        if (confirm) {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          if (!gc.frame.getActiveCursor().canSelect()) return;
          Choice decision = ((DecisionCursor) gc.frame.getActiveCursor()).getElm();
          switch (gc.getDecisionType()) {
            case CONFIRM_ATTACK_DECISION:
              gc.processConfirmAttackDecision(decision);
              break;
            case ACTION_DECISION:
              gc.processActionDecision(decision);
              break;
            case COMMANDER_ACTION_DECISION:
              gc.processCommanderActionDecision(decision);
              break;
            case PLAYER_ACTIONS_DECISION:
              gc.processPlayerActionDecision(decision);
              break;
            case END_OF_TURN_DECISION:
              gc.processEndTurnDecision(decision);
              break;
            case VIEW_OPTIONS_DECISION:
              gc.processViewOptionsDecision(decision);
              break;
            case SUMMON_DECISION:
              boolean startedDecision = gc.startSummonSelection(decision);
              if (startedDecision) {
                ((BoardCursor) gc.frame.getActiveCursor())
                    .setSelectType(BoardCursor.SelectType.SUMMON);
              } else {
                throw new RuntimeException(
                    "No valid space to summon - " + "shouldn't have been able to pick this");
              }
              break;
            case CAST_DECISION:
              gc.startCastSelection(decision);
              ((BoardCursor) gc.frame.getActiveCursor()).setSelectType(BoardCursor.SelectType.CAST);
              break;
            case NEW_ABILITY_DECISION:
              gc.processNewAbilityDecision(decision);
              break;
            default:
              break;
          }
        } else {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_NO);
          if (!gc.isDecisionManditory()) {
            if (gc.getDecisionType() == DecisionType.SUMMON_DECISION
                || gc.getDecisionType() == DecisionType.CAST_DECISION) {
              gc.cancelDecision();
              gc.startCommanderActionDecision(
                  gc.frame.getGamePanel().boardCursor.getElm().getOccupyingUnit()
                      instanceof Commander);
            } else if (gc.getDecisionType() == DecisionType.COMMANDER_ACTION_DECISION
                || gc.getDecisionType() == DecisionType.INFO_HOVER_DECISION) {
              gc.cancelDecision();
              gc.startActionDecision();
            } else if (gc.getDecisionType() == DecisionType.END_OF_TURN_DECISION
                || gc.getDecisionType() == DecisionType.VIEW_OPTIONS_DECISION) {
              gc.cancelDecision();
              gc.startPlayerActionDecision();
            } else {
              gc.cancelDecision();
            }
          } else {
            // TODO - tried to cancel manditory decision.
          }
        }
        break;
      case SUMMON_SELECTION:
        BoardCursor bc = (BoardCursor) gc.frame.getActiveCursor();
        bc.setSelectType(BoardCursor.SelectType.DEFAULT);
        if (confirm) {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          if (!gc.frame.getActiveCursor().canSelect()) return;
          Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
          gc.processSummonSelection(loc);
        } else {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_NO);
          boolean summoningUnit = gc.getSummonType().equals(GameController.SummonType.UNIT);
          gc.cancelSummonSelection();
          Summoner summoner = (Summoner) bc.getElm().getOccupyingUnit();
          if (summoningUnit) {
            gc.startSummonCombatantDecision(summoner);
          } else {
            gc.startSummonBuildingDecision(summoner);
          }
        }
        break;
      case CAST_SELECTION:
        ((BoardCursor) gc.frame.getActiveCursor()).setSelectType(BoardCursor.SelectType.DEFAULT);
        if (confirm) {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          if (!gc.frame.getActiveCursor().canSelect()) return;
          Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
          gc.processCastSelection(loc);
        } else {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_NO);
          gc.cancelCastSelection();
          gc.startCastDecision();
        }
        break;
      case ATTACK_SELECTION:
        ((BoardCursor) gc.frame.getActiveCursor()).setSelectType(BoardCursor.SelectType.DEFAULT);
        if (confirm) {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          gc.startConfirmAttackDecision();
        } else {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_NO);
          gc.cancelAttackSelection();
          gc.startActionDecision();
        }
        break;
        // Path selection - should only be the case after pathSelection already started
      case PATH_SELECTION:
        ((BoardCursor) gc.frame.getActiveCursor()).setSelectType(BoardCursor.SelectType.DEFAULT);
        if (confirm) {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_YES);
          if (!gc.frame.getActiveCursor().canSelect()) return;
          Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
          gc.processPathSelection(loc);
        } else {
          AudioController.playEffect(AudioController.SoundEffect.CLICK_NO);
          gc.cancelPathSelection();
          gc.startActionDecision();
        }
        break;
      default:
        break;
    }
  }

  /** Handles a unit cycle input. */
  static void handleUnitCycle() {
    if (frame.getActiveCursor() instanceof BoardCursor) {
      GameController gc = frame.getController();
      BoardCursor cursor = (BoardCursor) frame.getActiveCursor();
      Unit hoveredUnit = cursor.getElm().getOccupyingUnit();
      Unit nextUnit = gc.game.getCurrentPlayer().getNextActionableUnit(hoveredUnit);
      if (nextUnit != null) {
        cursor.setElm(nextUnit.getLocation());
        cursor.moved();
      }
    }
  }
}
