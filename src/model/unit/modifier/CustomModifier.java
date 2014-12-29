package model.unit.modifier;

import model.unit.Unit;

/** A modifier that is beyond the simple boundaries of a stat modification
 * Usually works off of the name, so names must be unique and well represented.
 * @author MPatashnik
 *
 */
public class CustomModifier extends Modifier {

	/** True if this applies to Buildings */
	public final boolean appliesToBuildings;
	
	/** True iff this applies to Commanders */
	public final boolean appliesToCommanders;
	
	/** True iff this applies to Combatants */
	public final boolean appliesToCombatants;
	
	/** A description of this Modifier - what it does, etc */
	public final String description;
	
	/** A numeric value for this modifier. If unneded, this will be 0 */
	public final Number val;
	
	/** Constructor for dummy instance
	 * @param name - the name of this modifier
	 * @param description - a description of this modifier
	 * @param val - the magnitude of the modifier, as needed
	 * @param turns - the total duration of this modifier (turns after this one).
	 * 					Can be Integer.MAX_VAL - interpreted as forever rather than the actual val
	 * @param stackable - true iff this is stackable
	 * @param buildings - true iff this modifier can apply to buildings
	 * @param commanders - true iff this modifier can apply to commanders
	 * @param combatants - true iff this modifier can apply to combatants
	 */
	public CustomModifier(String name, String description, Number val, int turns, boolean stackable,
			boolean buildings, boolean commanders, boolean combatants) {
		super(name, turns, stackable);
		this.description = description;
		this.val = val;
		this.appliesToBuildings = buildings;
		this.appliesToCommanders = commanders;
		this.appliesToCombatants = combatants;
	}
	
	/** Constructor for cloning instances
	 * @param model.unit - The model.unit this is modifying.
	 * @param source - the model.unit this modifier is tied to.
	 * @param dummy - the modifier to make a copy of
	 */
	public CustomModifier(Unit unit, Unit source, CustomModifier dummy){
		super(unit, source, dummy);
		this.description = dummy.description;
		this.val = dummy.val;
		this.appliesToBuildings = dummy.appliesToBuildings;
		this.appliesToCombatants = dummy.appliesToCombatants;
		this.appliesToCommanders = dummy.appliesToCommanders;
		attachToUnit();
	}

	/** Clones this for the given model.unit, source - creates a new customModifier(model.unit, source, this) */
	@Override
	public Modifier clone(Unit unit, Unit source) {
		return new CustomModifier(unit, source, this);
	}
	
	@Override
	public String toStatString(){
		return description;
	}
	
	@Override
	public String toStringLong(){
		return toStringShort() + " " + description;
	}
	
	@Override
	public String toStringFull(){
		return toStringShort() + " " + description;
	}

}
