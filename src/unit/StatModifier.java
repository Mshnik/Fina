package unit;

public final class StatModifier extends Modifier {

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
	 * @param name - the name of this statModifier
	 * @param turns - the total duration of this modifier (turns after this one).
	 * 					Can be Integer.MAX_VAL - interpreted as forever rather than the actual val
	 * @param stackable - true iff this is stackable
	 * @param stat - the stat to modify
	 * @param modType - the operation on stat to perform
	 * @param modVal - the value to modify by
	 */
	public StatModifier(String name, int turns, boolean stackable, 
			StatType stat, ModificationType modType, Object modVal){
		super(name, turns, stackable);
		modifiedStat = stat;
		this.modType = modType;
		val = modVal;
	}
	
	/** Constructor for cloning instances
	 * @param unit - The unit this is modifying.
	 * @param source - the unit this modifier is tied to.
	 * @param dummy - the StatModifier to make a copy of
	 */
	public StatModifier(Unit unit, Unit source, StatModifier dummy){
		super(unit, source, dummy);
		modifiedStat = dummy.modifiedStat;
		modType = dummy.modType;
		val = dummy.val;
		attachToUnit();
	}
	
	/** Returns the mod val - for what to do with it, see modType */
	Object getModVal(){
		return val;
	}

	/** Returns a StatModifier clone of this with the given unit and source */
	@Override
	public Modifier clone(Unit unit, Unit source) {
		return new StatModifier(unit, source, this);
	}
}

