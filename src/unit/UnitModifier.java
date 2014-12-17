package unit;

/** A Modifier for a unit - a buff or nerf, etc. */
public class UnitModifier {

	/** The unit this is modifying */
	public final Unit unit;

	/** The source of this modification */
	public final Unit source;

	/** The remaining turns for this Modifier.
	 * If 0, on its last turn.
	 */
	private int remainingTurns;

	/** The stat this is modifying */
	public final StatType modifiedStat;

	public enum ModificationType{
		SET,
		ADD,
		MULTIPLY
	}

	/** The type of modification - set, add, mult */
	public final ModificationType modType;

	/** The set/added/multed value of the stat */
	private Object val;

	/** Constructor for dummy instance
	 * @param turns - the total duration of this modifier (turns after this one).
	 * 					Can be Integer.MAX_VAL - interpreted as forever rather than the actual val
	 * @param stat - the stat to modify
	 * @param modType - the operation on stat to perform
	 * @param modVal - the value to modify by
	 */
	public UnitModifier(int turns, 
			StatType stat, ModificationType modType,
			Object modVal){
		unit = null;
		remainingTurns = turns;
		source = null;
		modifiedStat = stat;
		this.modType = modType;
		val = modVal;
	}
	
	/** Constructor for cloning instances
	 * @param unit - The unit this is modifying.
	 * @param source - the unit this modifier is tied to.
	 * @param dummy - the unitModifier to make a copy of
	 */
	public UnitModifier(Unit unit, Unit source, UnitModifier dummy){
		this.unit = unit;
		this.source = source;
		
		remainingTurns = dummy.remainingTurns;
		modifiedStat = dummy.modifiedStat;
		modType = dummy.modType;
		val = dummy.val;
		
		unit.addModifier(this);
		source.addGrantedModifier(this);
	}
	
	/** Returns true if this is a dummy (unit is null ) */
	public boolean isDummy(){
		return unit == null;
	}

	/** Returns the mod val - for what to do with it, see modType */
	Object getModVal(){
		return val;
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
	 * Does nothing if its a dummy. */
	public void kill(){
		if(isDummy()) return;
		unit.removeModifier(this);
		source.removeGrantedModifier(this);
	}
}
