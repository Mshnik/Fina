package unit;

import java.util.Collection;

/** A Modifier for a unit - a buff or nerf, etc. */
public abstract class Modifier {

	/** The unit this is modifying */
	public final Unit unit;

	/** The source of this modification */
	public final Unit source;

	/** The remaining turns for this Modifier.
	 * If 0, on its last turn.
	 */
	private int remainingTurns;
	
	/** True iff a unit is allowed to have multiple modifiers by this name
	 * 
	 */
	public final boolean stackable;
	
	/** True once this has been attached to a unit */
	private boolean attached;
	
	/** The modifier this was cloned from. Null if this is a dummy */
	private final Modifier clonedFrom;

	/** Constructor for dummy instance
	 * @param turns - the total duration of this modifier (turns after this one).
	 * 					Can be Integer.MAX_VAL - interpreted as forever rather than the actual val
	 * @param stackable - true iff a unit can have multiple copies of this modifier
	 */
	public Modifier(int turns, boolean stackable){
		unit = null;
		remainingTurns = turns;
		source = null;
		attached = false;
		this.stackable = stackable;
		clonedFrom = null;
	}
	
	/** Constructor for cloning instances
	 * @param unit - The unit this is modifying.
	 * @param source - the unit this modifier is tied to.
	 * @param dummy - the unitModifier to make a copy of
	 */
	public Modifier(Unit unit, Unit source, Modifier dummy){
		attached = false;
		this.unit = unit;
		this.source = source;
		this.stackable = dummy.stackable;
		remainingTurns = dummy.remainingTurns;
		clonedFrom = dummy;
	}
	
	/** Call when construction of a non-dummy instance is done - adds to affected unit */
	protected void attachToUnit(){
		if(isDummy() || attached) 
			throw new RuntimeException("Can't attach a dummy or already attached modifier");
		boolean ok = unit.addModifier(this);
		if(ok) source.addGrantedModifier(this);
	}
	
	/** Returns true if another copy of this exists within the given collection
	 * Checks each member of the given collection for having the same clonedFrom as this.
	 * If this is a dummy, throws runtimeException
	 */
	public boolean cloneInCollection(Collection<? extends Modifier> elms) throws RuntimeException{
		if(isDummy()) throw new RuntimeException("Can't check if dummy is in list");
		for(Modifier m : elms){
			if(m.clonedFrom == clonedFrom) return true;
		}
		return false;
	}
	
	/** Returns true if this is a dummy (unit is null ) */
	public boolean isDummy(){
		return unit == null;
	}

	/** Returns the remaining turns of this modifier */
	public int getRemainingTurns(){
		return remainingTurns;
	}

	/** Decrements the remaining turns. Doesn't dec if is infinite (Integer.MAX_VAL) or is dummy
	 * Method calling this method should use the return of the method to maybe
	 * remove this modifier.
	 * @return true iff this is is now dead (remainingTurns < 0)
	 */
	public boolean decRemainingTurns(){
		if(remainingTurns == Integer.MAX_VALUE || isDummy()) return false;
		remainingTurns--;
		return remainingTurns < 0;
	}

	/** Kills this modifier - removes it from its source and its unit.
	 * Throws exception if dummy*/
	public void kill(){
		if(isDummy())
			throw new RuntimeException("Can't kill a dummy modifier");
		unit.removeModifier(this);
		source.removeGrantedModifier(this);
	}
}
