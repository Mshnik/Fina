package view.gui.animation;

import model.unit.Unit;
import view.gui.panel.GamePanel;

/** An abstract parent of an animation involving a unit. */
public abstract class UnitAnimation implements Animatable {

  final GamePanel gamePanel;
  private final Unit unit;

  /** The current state this is on. */
  private int state;

  /** True if this is active, false once the animation has been completed. */
  private boolean active;

  UnitAnimation(GamePanel gamePanel, Unit unit) {
    this.gamePanel = gamePanel;
    this.unit = unit;

    state = 0;
    active = true;
  }

  public Unit getUnit() {
    return unit;
  }

  @Override
  public int getState() {
    return state;
  }

  @Override
  public void advanceState() {
    state = (state + 1) % getStateCount();
  }

  @Override
  public void setState(int state) {
    this.state = state;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void animationCompleted() {
    gamePanel.getFrame().getAnimator().removeAnimatable(this);
    state = getStateCount() - 1;
    active = false;
  }
}
