package unit.ability;

import unit.modifier.ModifierBundle;

public class ModifierAbility extends Ability {

	/** The bundle of modifiers that make up this ability */
	private final ModifierBundle modifiers;
	
	/** True iff modifiers should be applied to allied units */
	public final boolean appliesToAllied;
	
	/** True iff modifiers should be applied to enemy units */
	public final boolean appliesToFoe;
	
}
