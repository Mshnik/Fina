package unit.ability;

import java.util.Set;
import board.MPoint;
import unit.Commander;
import unit.Unit;
import unit.modifier.ModifierBundle;

public class ModifierAbility extends Ability {

	/** The bundle of modifiers that make up this ability. Should be dummies in here. */
	private final ModifierBundle modifiers;
	
	/** ModifierAbility Constructor
	 * @param name				- the Name of this ability
	 * @param manaCost			- the mana cost of using this ability. 0 if passive
	 * @param caster			- the Commander who owns this ability
	 * @param castDist			- the distance from the commander this ability can be cast
	 * @param multiCast			- true iff this can be used multiple times in a turn
	 * @param appliesToAllied	- true iff this ability can affect allied units
	 * @param appliesToFoe		- true iff this ability can affect non-allied units
	 * @param cloud				- the cloud of points (around the target as (0,0)) that it affects
	 * @param modifiers 		- a bundle of modifiers to apply when this ability is cast
	 */
	public ModifierAbility(String name, int manaCost, Commander caster,
			int castDist, boolean multiCast, boolean appliesToAllied,
			boolean appliesToFoe, Set<MPoint> cloud, ModifierBundle modifiers) {
		
		super(name, manaCost, caster, castDist, multiCast, appliesToAllied,
				appliesToFoe, cloud);
		
		if(! modifiers.isDummyBundle())
			throw new IllegalArgumentException("Can't make ability of non-dummy abilities");
		this.modifiers = modifiers;
	}

	/** Applies this modifierAbility to the given unit */
	@Override
	protected void affect(Unit u) {
		modifiers.clone(u, caster);
	}
	
}
