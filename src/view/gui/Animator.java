package view.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Timer;

/**
 * An instance holds the timers for changing the animations of Animatable objects with animation
 * updates.
 *
 * @author MPatashnik
 */
public final class Animator {

  /** Animatables to that are animated by this */
  private Set<Animatable> animates;

  /** Constructor for an Animator */
  Animator() {
    animates = Collections.synchronizedSet(new HashSet<Animatable>());
  }

  /** Adds the given Animatable to this Animator, and starts it animating. */
  void addAnimatable(final Animatable a) {
    if (!animates.contains(a)) {
      final Timer t = new Timer(a.getStateLength(), null);
      t.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Check if this is still to be animated. If not, exit.
              if (!animates.contains(a)) return;
              if (a.isActive()) a.advanceState();
              t.restart();
            }
          });
      animates.add(a);
      t.start();
    }
  }

  /** Removes the given Animatable from this Animator */
  public void removeAnimatable(Animatable a) {
    animates.remove(a);
  }
}
