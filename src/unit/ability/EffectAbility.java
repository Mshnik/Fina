package unit.ability;

import java.util.Set;

import unit.Combatant;
import unit.Commander;
import unit.MovingUnit;
import unit.Unit;
import board.MPoint;

/** A singleton effect ability - causes a change. */
public class EffectAbility extends Ability {
	
	/** The effect type of this EffectAbility */
	public final EffectType type;
	
	/** The magnitude of this effectAbility. Can be 0 if unused by type */
	public final Number magnitude;
	
	/** EffectAbility Constructor
	 * @param name				- the Name of this ability
	 * @param manaCost			- the mana cost of using this ability. 0 if passive
	 * @param caster			- the Commander who owns this ability
	 * @param castDist			- the distance from the commander this ability can be cast
	 * @param multiCast			- true iff this can be used multiple times in a turn
	 * @param appliesToAllied	- true iff this ability can affect allied units
	 * @param appliesToFoe		- true iff this ability can affect non-allied units
	 * @param cloud				- the cloud of points (around the target as (0,0)) that it affects
	 * @param type				- the effect type of this ability
	 * @param magnitude			- the magnitude of the effect of this ability. 
	 * 								Should be the correct type of number for the effect type
	 */
	public EffectAbility(String name, int manaCost, Commander caster,
			int castDist, boolean multiCast, boolean appliesToAllied,
			boolean appliesToFoe, Set<MPoint> cloud, EffectType type, Number magnitude) {
		super(name, manaCost, caster, castDist, multiCast, appliesToAllied,
				appliesToFoe, cloud);
		
		if(manaCost <= 0)
			throw new IllegalArgumentException("Effect Abilities can't be passive");
		
		this.type = type;
		this.magnitude = magnitude;
	}
	

	/** Applies the effectAbility to the given unit. Effect depends on Effect type of ability */
	@Override
	protected void affect(Unit u) {
		switch(type){
		case HEAL:
			u.changeHealth(magnitude.intValue(), caster);
			return;
		case REFRESH_ATTACK:
			if(! (u instanceof Combatant)) return;
			Combatant c = (Combatant)u;
			c.refreshAttack();
			return;
		case REFRESH_MOVE:
			if(! (u instanceof MovingUnit)) return;
			MovingUnit m  = (MovingUnit)u;
			m.refreshMovement();
			return;
		case TRUE_DAMAGE:
			u.changeHealth(- magnitude.intValue(), caster);
			return;
		}		
	}


	@Override
	public void remove(Unit u) {
		throw new UnsupportedOperationException("Can't Remove EffectAbility from Unit");
	}
	
	@Override
	public boolean isAffecting(Unit u) {
		throw new UnsupportedOperationException("Can't check isAffecting EffectAbility on Unit");
	}

}
