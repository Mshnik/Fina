package unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/** Holder for the stats for a unit.
 * Unless otherwise noted, all stats are non-negative. */
public class UnitStats implements Iterable<Stat>{

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
	
	/** Number of non-base fields this stat represents */
	private static final int NUMB_FIELDS = 8;

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
	
	/** An iterator over this stats that shows each stat in turn.
	 * Might not catch concurrent modification exceptions, so make sure
	 * to get a new iterator after the UnitStats has been modified. */
	class StatIterator implements Iterator<Stat>{

		private int index;
		private ArrayList<Stat> stats;
		
		private StatIterator(){
			index = 0;
			stats = new ArrayList<Stat>();
			stats.add(new Stat("Max Health", maxHealth));
			stats.add(new Stat("Mana / Turn", manaPerTurn));
			stats.add(new Stat("Attack", attack));
			stats.add(new Stat("Attack Type", attackType));
			stats.add(new Stat("Phys. Defense", physicalDefense));
			stats.add(new Stat("Magic Defense", magicDefense));
			stats.add(new Stat("Range", range));
			stats.add(new Stat("Vision Range", visionRange));
		}
		
		/** Returns true if there is another stat to return */
		@Override
		public boolean hasNext() {
			return index < NUMB_FIELDS;
		}

		/** Returns the next stat, in standard order */
		@Override
		public Stat next() {
			Stat s = stats.get(index);
			index++;
			return s;
		}

		/** Removal not supported - throws runtime exception */
		@Override
		public void remove() {
			throw new RuntimeException("Not Supported");
		}
		
	}

	/** Returns a new iterator over these stats.
	 * Won't catch a concurrent modification exception, so don't alter stats while iterating
	 */
	@Override
	public Iterator<Stat> iterator() {
		return new StatIterator();
	}
}
