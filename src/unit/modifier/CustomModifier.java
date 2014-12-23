package unit.modifier;

import unit.Unit;

/** A modifier that is beyond the simple boundaries of a stat modification
 * Usually works off of the name, so names must be unique and well represented.
 * @author MPatashnik
 *
 */
public class CustomModifier extends Modifier {

	/** A description of this Modifier - what it does, etc */
	public final String description;
	
	/** A numeric value for this modifier. If unneded, this will be 0 */
	public final Number val;
	
	/** Constructor for dummy instance
	 * @param name - the name of this modifier
	 * @param description - a description of this modifier
	 * @param turns - the total duration of this modifier (turns after this one).
	 * 					Can be Integer.MAX_VAL - interpreted as forever rather than the actual val
	 * @param stackable - true iff this is stackable
	 */
	public CustomModifier(String name, String description, Number val, int turns, boolean stackable) {
		super(name, turns, stackable);
		this.description = description;
		this.val = val;
	}
	
	/** Constructor for cloning instances
	 * @param unit - The unit this is modifying.
	 * @param source - the unit this modifier is tied to.
	 * @param dummy - the modifier to make a copy of
	 */
	public CustomModifier(Unit unit, Unit source, CustomModifier dummy){
		super(unit, source, dummy);
		this.description = dummy.description;
		this.val = dummy.val;
		attachToUnit();
	}

	/** Clones this for the given unit, source - creates a new customModifier(unit, source, this) */
	@Override
	public Modifier clone(Unit unit, Unit source) {
		return new CustomModifier(unit, source, this);
	}

}
