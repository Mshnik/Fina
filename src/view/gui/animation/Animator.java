package view.gui.animation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.Timer;

/**
 * An instance holds the timers for changing the animations of Animatable objects with animation
 * updates.
 *
 * @author MPatashnik
 */
public final class Animator {

  /** Length of an animation frame in milliseconds. */
  private static final int FRAME_LENGTH_IN_MILLIS = 50;

  /** True if this animator is paused - stop animating all things while this is true. */
  private boolean paused;

  /** Animatables to that are animated by this */
  private final Map<Animatable, Integer> animates;

  /** Animatables to be removed on the next frame start. */
  private final Set<Animatable> animatesToRemove;

  private final Timer timer;

  /** Constructor for an Animator */
  public Animator() {
    paused = false;
    animates = Collections.synchronizedMap(new HashMap<>());
    animatesToRemove = Collections.synchronizedSet(new HashSet<>());
    timer = new Timer(FRAME_LENGTH_IN_MILLIS, e -> animate());
    timer.start();
  }

  /**
   * Callback that causes animation. Animates all animatables at 0 remaining frame counts and resets
   * to max, then decrements remaining frame count for each animatable
   */
  private void animate() {
    synchronized (animates) {
      synchronized (animatesToRemove) {
        animatesToRemove.forEach(animates::remove);
        animatesToRemove.clear();
      }
      // First animate animatables at 0 frames remaining and reset to max.
      animates
          .entrySet()
          .stream()
          .filter(e -> e.getValue() == 0)
          .map(Entry::getKey)
          .filter(Animatable::isActive)
          .forEach(
              a -> {
                a.advanceState();
                // If state is 0 after animation, this just rolled over.
                if (a.getState() == 0) {
                  a.animationCompleted();
                }
                animates.put(a, a.getStateLength());
              });

      // Then decrement all animatables.
      animates
          .keySet()
          .stream()
          .filter(Animatable::isActive)
          .forEach(a -> animates.put(a, animates.get(a) - 1));
    }
    timer.restart();
  }

  /** Sets the paused state. If this causes a change, resumes / stops the timer. */
  public void setPaused(boolean pause) {
    if (pause == this.paused) {
      return;
    }

    this.paused = pause;
    if (paused) {
      timer.stop();
    } else {
      timer.start();
    }
  }

  /** Adds the given Animatable to this Animator, and starts it animating. */
  public void addAnimatable(final Animatable a) {
    synchronized (animates) {
      if (!animates.containsKey(a)) {
        animates.put(a, a.getStateLength());
      }
    }
  }

  /**
   * Removes the given Animatable from this Animator, at the start of the next animation frame. This
   * prevents an animatable from being removed in the middle of a frame.
   */
  public void removeAnimatable(Animatable a) {
    synchronized (animatesToRemove) {
      animatesToRemove.add(a);
    }
  }

  /** Clears all animatables. */
  public void clearAnimatables() {
    synchronized (animates) {
      animates.clear();
    }
  }
}
