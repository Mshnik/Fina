package view.gui.animation;

import model.game.Game;
import model.unit.Unit;
import view.gui.panel.GamePanel;

/**
 * An abstract parent of an animation involving a unit. Doesn't loop - active is set to false after
 * completing once.
 */
public abstract class UnitAnimation implements Animatable {

  final GamePanel gamePanel;
  private final Unit unit;
  private UnitAnimation nextAnimation;

  /** True if this has been started, false before. Will still be true after animation completes. */
  private boolean started;

  /**
   * True if this is active, false before this is started or after the animation has been completed.
   */
  private boolean active;

  /** The current state this is on. */
  private int state;

  UnitAnimation(GamePanel gamePanel, Unit unit) {
    this.gamePanel = gamePanel;
    this.unit = unit;
    state = 0;
    started = false;
    active = false;
  }

  /** Sets the next animation. Can only be called if active is false and nextAnimation is null. */
  public synchronized void setNextAnimation(UnitAnimation nextAnimation) {
    if (active || this.nextAnimation != null) {
      throw new RuntimeException("Can't set nextAnimation now");
    }
    this.nextAnimation = nextAnimation;
  }

  /**
   * Starts this animation. Only needed on the first animation in a chain, others are started when
   * the previous one finishes.
   */
  public synchronized void start() {
    started = true;
    active = true;
  }

  public Unit getUnit() {
    return unit;
  }

  public abstract boolean isVisible(Game game);

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
  public synchronized boolean isActive() {
    return active;
  }

  /**
   * Returns true after this animation is done - once it has been started and is no longer active.
   * Synchronized to make sure that active and started are set in sync.
   */
  public synchronized boolean isCompleted() {
    return !active && started;
  }

  @Override
  public synchronized void animationCompleted() {
    gamePanel.getFrame().getAnimator().removeAnimatable(this);
    state = getStateCount() - 1;
    active = false;
    if (nextAnimation != null) {
      nextAnimation.start();
    }
  }
}
