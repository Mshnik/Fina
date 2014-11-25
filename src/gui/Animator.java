package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

/** An instance holds the timers for changing the animations of
 * Animatable objects with animation updates.
 * @author MPatashnik
 *
 */
public class Animator {

	/** Animatables to that are animated by this */
	private Set<Animatable> animates;
	
	/** Constructor for an Animator */
	public Animator(){
		animates = Collections.synchronizedSet(new HashSet<Animatable>());
	}
	
	/** Adds the given Animatable to this Animator, and starts it animating. */
	public void addAnimatable(final Animatable a){
		final Timer t = new Timer(a.getStateLength(), null);
		t.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//Check if this is still to be animated. If not, exit.
				if(! animates.contains(a)) return;
				a.advanceState();
				t.restart();
			}
		});
		animates.add(a);
		t.start();
	}
	
	/** Removes the given Animatable from this Animator */
	public void removeAnimatable(Animatable a){
		animates.remove(a);
	}
	
}
