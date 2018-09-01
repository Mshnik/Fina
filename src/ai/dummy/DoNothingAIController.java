package ai.dummy;

import ai.AIAction;
import ai.AIController;
import model.game.Player;

/** An AI Controller that takes no action on every turn. */
public final class DoNothingAIController implements AIController {

  /** Extra time this AI should sleep on its turn, to simulate doing stuff. */
  private final int extraSleepTime;

  public DoNothingAIController(int extraSleepTime) {
    this.extraSleepTime = extraSleepTime;
  }

  @Override
  public AIAction getNextAction(Player player) {
    if (extraSleepTime > 0) {
      try {
        Thread.sleep(extraSleepTime);
      } catch (InterruptedException e) {
        return null;
      }
    }
    return null;
  }
}
