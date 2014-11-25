package gui;

/** A paintable object that is also animatable
 * (has changes in its painting on a cycle duration)
 * @author MPatashnik
 *
 */
public interface Animatable extends Paintable{

	/** Return the length (in ms) of a step of this Animatable */
	public int getStateLength();
	
	/** Return the total number of states of this Animatable (at least 2) */
	public int getStateCount();
	
	/** Return the current state this Animatable is on (0 ... getStateCount() - 1) */
	public int getState();
	
	/** Advance the state of this animatable by 1, rolling over to 0 as necessary */
	public void advanceState();
}
