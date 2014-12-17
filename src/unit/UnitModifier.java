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
	
	/** Constructor
	 * @param u - The unit this is modifying
	 * @param source - the unit this modifier is tied to.
	 * @param turns - the total duration of this modifier (turns after this one)
	 */
	public UnitModifier(Unit u, Unit source, int turns, 
			StatType stat, ModificationType modType,
			Object modVal){
		unit = u;
		remainingTurns = turns;
		this.source = source;
		modifiedStat = stat;
		this.modType = modType;
		val = modVal;
	}
	
	/** Returns the mod val - for what to do with it, see modType */
	Object getModVal(){
		return val;
	}
	
	/** Returns the remaining turns of this modifier */
	public int getRemainingTurns(){
		return remainingTurns;
	}
	
	/** Decrements the remaining turns.
	 * Method calling this method should use the return of the method to maybe
	 * remove this modifier.
	 * @return true iff this is is now dead (remainingTurns < 0)
	 */
	public boolean decRemainingTurns(){
		remainingTurns--;
		return remainingTurns < 0;
	}
}
