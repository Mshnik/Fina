package unit;

import java.util.Collection;

/** Holder for the stats for a unit.
 * Unless otherwise noted, all stats are non-negative. */
public class UnitStats {

	/** The base (pre-modification) stats this was calculated from, if any */
	public final UnitStats base; 		
	
	/** The health cap for this unit. Actual health level is kept track of by unit.
	 * maxHealth >= 0. */
	public final int maxHealth;
	
	/** The mana generation/cost of this unit. Positive is generation, negative is cost */
	public final int manaPerTurn;
	
	/** Damage dealt when attacking (Before applying defenses). attack >= 0 */
	public final int attack;
	
	/** Type of damage dealt when attacking. */
	public final AttackType attackType;
	
	/** Damage dealt when counterattacking (before applying defenses). counterattack >= 0 */
	public final int counterattack;
	
	/** Defense against physical damage - subtracted from all physical attacks before taking damage.
	 * physicalDefense >= 0 */
	public final int physicalDefense;
	
	/** Defense against ranged damage - subtracted from all ranged attacks before taking damage
	 * rangeDefense >= 0 */
	public final int rangeDefense;
	
	/** Defense against magic damage - subtracted from all magic attacks before taking damage
	 * magicDefense >= 0 */
	public final int magicDefense;
	
	/** Distance at which another unit can be attacked. 
	 * Distance is measured using manhattan distance - 1.
	 * range >= 0.
	 */
	public final int range;
	
	/** Constructor for UnitStats
	 * @param maxHealth 		- the Maximum health (life points, etc) of this unit
	 * @param manaPerTurn		- the mana this generates (or cost, if < 0) per turn
	 * @param attack			- attack of this unit
	 * @param attackType		- type of attack of this unit
	 * @param counterattack 	- counter attack strength of this unit
	 * @param physicalDefense	- physical defense of this unit - defense applied to physical attacks
	 * @param rangeDefense		- ranged defense of this unit	- defense applied to ranged attacks
	 * @param magicDefense		- magic defense of this tuni	- defense applied to magic attacks
	 */
	public UnitStats(int maxHealth, int manaPerTurn, int attack, AttackType attackType, int counterattack, 
					 int physicalDefense, int rangeDefense, int magicDefense, int range) 
			throws IllegalArgumentException{
		this.maxHealth = maxHealth;
		this.manaPerTurn = manaPerTurn;
		this.attack = attack;
		this.attackType = attackType;
		this.counterattack = counterattack;
		this.physicalDefense = physicalDefense;
		this.rangeDefense = rangeDefense;
		this.magicDefense = magicDefense;
		this.range = range;
		base = null;
	}
	
	/** Constructor for UnitStats from a base stats and a collection of modifiers.
	 */
	public UnitStats(UnitStats base, Collection<UnitModifier> modifiers) throws IllegalArgumentException{
		int maxHealth = base.maxHealth;
		int manaPerTurn = base.manaPerTurn;
		int attack = base.attack;
		AttackType attackType = base.attackType;
		int counterattack = base.counterattack;
		int physicalDefense = base.physicalDefense;
		int rangeDefense = base.rangeDefense;
		int magicDefense = base.magicDefense;
		int range = base.range;
		
		//Process modifiers
		if(modifiers != null){
			for(UnitModifier m : modifiers){
				//TODO
			}
		}
		
		this.base = base;
		this.maxHealth = maxHealth;
		this.manaPerTurn = manaPerTurn;
		this.attack = attack;
		this.attackType = attackType;
		this.counterattack = counterattack;
		this.physicalDefense = physicalDefense;
		this.rangeDefense = rangeDefense;
		this.magicDefense = magicDefense;
		this.range = range;
	}
	
	/** Returns true if this is a base stats (unmodified), false otherwise */
	public boolean isBase(){
		return base == null;
	}
	
}
