package view.gui;

/**
 * A paintable object that is also animatable (has changes in its painting on a cycle duration)
 *
 * @author MPatashnik
 */
public interface Animatable extends Paintable {

  /** Return the length (in ms) of a step of this Animatable. Can be overriden for different delay timing. */
  default int getStateLength() {
    return 75;
  }

  /** Return the total number of states of this Animatable (at least 2) */
  int getStateCount();

  /** Return the current state this Animatable is on (0 ... getStateCount() - 1) */
  int getState();

  /** Advance the state of this animatable by 1, rolling over to 0 as necessary */
  void advanceState();

  /** Sets the animation state. Will mod by the number of possible states */
  void setState(int state);

  /** True if this is currently active (do animation), false otherwise */
  boolean isActive();
}
