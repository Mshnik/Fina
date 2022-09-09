package model.game;

import java.awt.*;

/**
 * A human controlled player
 */
public class HumanPlayer extends Player {

  /**
   * String to show for selecting a player of this type on the new game UI.
   */
  public static final String HUMAN_PLAYER_TYPE = "Human";

  /**
   * Wait time between endAction checks
   */
  private static final int SLEEP_TIME = 500;

  /**
   * True while the player is performing action, false once they perform the end turn action.
   */
  private boolean takingAction;

  /**
   * Constructs a human player for the given model.game
   */
  public HumanPlayer(Game g, Color c) {
    super(g, c);
  }

  @Override
  public boolean isLocalHumanPlayer() {
    return true;
  }

  @Override
  protected void turn() {
    takingAction = true;
    while (takingAction) {
      try {
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException e) {
        takingAction = false;
      }
    }
  }

  /**
   * Called by the view.gui when the player selects the end of turn action. Causes the turn loop to
   * end.
   */
  @Override
  public void endTurn() {
    takingAction = false;
  }

  @Override
  public String getIdString() {
    return "Human";
  }

  @Override
  public String getConfigString() {
    return "";
  }
}
