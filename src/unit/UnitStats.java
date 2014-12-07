package unit;

import java.util.Collection;


/** Holder for the stats for a unit.
 * Unless otherwise noted, all stats are non-negative. */
public class UnitStats{

	/** The base (pre-modification) stats this was calculated from, if any */
	private UnitStats base; 		
	
	/** The health cap for this unit. Actual health level is kept track of by unit.
	 * maxHealth >= 0. */
	private int maxHealth;
	
	/** The mana generation/cost of this unit, if any. Positive is generation */
	private int manaPerTurn;
	
	/** Damage dealt when attacking (Before applying defenses). attack >= 0 */
	private int attack;
	
	/** Type of damage dealt when attacking. */
	private AttackType attackType;
	
	/** Defense against physical damage - percent of physical damage prevented.
	 * 1 > physicalDefense >= 0 */
	private double physicalDefense;
	
	/** Defense against magic damage - percent of magic damage prevented
	 * 1 > magicDefense >= 0 */
	private double magicDefense;
	
	/** Distance at which another unit can be attacked, or at which units can be summoned. 
	 * Distance is measured using manhattan distance - 1.
	 * Units with range 0 are melee, others are ranged
	 * range >= 0.
	 */
	private int range;
	
	/** Base vision radius of this unit */
	private int visionRange;

	/** Constructor for UnitStats
	 * @param maxHealth 		- the Maximum health (life points, etc) of this unit
	 * @param manaPerTurn		- the mana this generates (or cost, if < 0) per turn
	 * @param attack			- attack of this unit
	 * @param attackType		- type of attack of this unit
	 * @param physicalDefense	- physical defense of this unit - defense applied to physical attacks
	 * @param magicDefense		- magic defense of this unit	- defense applied to magic attacks
	 * @param range				- range of this unit - 0 represents melee (adjacent units)
	 * @param visionRange		- visionRange of this unit - 0 represents no extra vision beyond its own tile
	 */
	public UnitStats(int maxHealth, int manaPerTurn, int attack, AttackType attackType,
					 double physicalDefense, double magicDefense, int range, int visionRange) 
			throws IllegalArgumentException{
		this.maxHealth = maxHealth;
		this.manaPerTurn = manaPerTurn;
		this.attack = attack;
		this.attackType = attackType;
		this.physicalDefense = physicalDefense;
		this.magicDefense = magicDefense;
		this.range = range;
		this.visionRange = visionRange;
		base = null;
	}
	
	/** Constructor for UnitStats from a base stats and a collection of modifiers.
	 */
	public UnitStats(UnitStats base, Collection<UnitModifier> modifiers) throws IllegalArgumentException{
		int maxHealth = base.maxHealth;
		int manaPerTurn = base.manaPerTurn;
		int attack = base.attack;
		AttackType attackType = base.attackType;
		double physicalDefense = base.physicalDefense;
		double magicDefense = base.magicDefense;
		int range = base.range;
		int visionRange = base.visionRange;
		
		//Process modifiers
		if(modifiers != null){
//			for( UnitModifier m : modifiers){
//				//TODO
//			}
		}
		
		this.base = base;
		this.maxHealth = maxHealth;
		this.manaPerTurn = manaPerTurn;
		this.attack = attack;
		this.attackType = attackType;
		this.physicalDefense = physicalDefense;
		this.magicDefense = magicDefense;
		this.range = range;
		this.visionRange = visionRange;
	}
	
	/** Returns true if this is a base stats (unmodified), false otherwise */
	public boolean isBase(){
		return base == null;
	}
	
	/**
	 * @return The base (pre-modification) stats this was calculated from, if any 
	 */
	public UnitStats getBase() {
		return base;
	}

	/**
	 * @return  The health cap for this unit. Actual health level is kept track of by unit.
	 * maxHealth >= 0.
	 */
	public int getMaxHealth() {
		return maxHealth;
	}

	/**
	 * @return The mana generation/cost of this unit. Positive is generation, negative is cost
	 */
	public int getManaPerTurn() {
		return manaPerTurn;
	}

	/**
	 * @return Damage dealt when attacking (Before applying defenses). attack >= 0
	 */
	public int getAttack() {
		return attack;
	}

	/**
	 * @return Type of damage dealt when attacking.
	 */
	public AttackType getAttackType() {
		return attackType;
	}

	/**
	 * @return Defense against physical damage -percent of physical attack damage prevented
	 * 1 > physicalDefense >= 0
	 */
	public double getPhysicalDefense() {
		return physicalDefense;
	}

	/**
	 * @return Defense against magic damage - percent of magic attack damage prevented
	 * 1 > magicDefense >= 0 
	 */
	public double getMagicDefense() {
		return magicDefense;
	}

	/**
	 * @return Distance at which another unit can be attacked, or at which units can be summoned. 
	 * Distance is measured using manhattan distance - 1.
	 * range >= 0.
	 */
	public int getRange() {
		return range;
	}
	
	/** @return Distance this unit can see and contributes to its owner's sight.
	 * Distance is measured using manhattan distance.
	 * visionRange >= 0.
	 */
	public int getVisionRange(){
		return visionRange;
	}

	/** Returns a new UnitStats with this (if this is a base) as the base
	 * or this' base if this is non-base, and the given modifiers */
	public UnitStats modifiedWith(Collection<UnitModifier> modifiers) {
		if(isBase())
			return new UnitStats(this, modifiers);
		else
			return new UnitStats(this.base, modifiers);
	}
	
	/** Basic toString impelementation that shows off the stats this represents */
	@Override
	public String toString(){
		return "Max Health : " + maxHealth + "; Mana Per Turn :" + manaPerTurn +
			"; Attack : " + attack + "; AttackType : " + attackType + "; Physical Defense : " +
			physicalDefense + "; Magic Defense : " + magicDefense + "; Range : " + range + 
			"; VisionRange : " + visionRange + " Base : " + isBase();
	}
}
