package model.unit.stat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.unit.AttackType;
import model.unit.modifier.Modifier;
import model.unit.modifier.StatModifier;

import util.Mth;

/**
 * Holder for the stats for a model.unit.
 * Unless otherwise noted, all stats are non-negative.
 */
public class Stats implements Iterable<Stat>{

	/** Base stats for everything, to be overridden as template.
	 * Initializes all stats to their base 0-ish vals.
	 * 
	 * //TODO - use?
	 */
	private static final HashMap<StatType, Object> TEMPLATE;

	static{
		TEMPLATE = new HashMap<>();
		//Base -> Null
		TEMPLATE.put(StatType.MAX_HEALTH, 0);
		TEMPLATE.put(StatType.MANA_PER_TURN, 0);
		TEMPLATE.put(StatType.ATTACK, 0);
		TEMPLATE.put(StatType.ATTACK_TYPE, AttackType.NO_ATTACK);
		TEMPLATE.put(StatType.PHYSICAL_DEFENSE, 0.0);
		TEMPLATE.put(StatType.MAGIC_DEFENSE, 0.0);
		TEMPLATE.put(StatType.ATTACK_RANGE, 0);
		TEMPLATE.put(StatType.SUMMON_RANGE, 0);
		TEMPLATE.put(StatType.VISION_RANGE, 0);
		TEMPLATE.put(StatType.MOVEMENT_TOTAL, 0);
		TEMPLATE.put(StatType.GRASS_COST, 0);
		TEMPLATE.put(StatType.WOODS_COST, 0);
		TEMPLATE.put(StatType.MOUNTAIN_COST, 0);
	}

	/** The stats maintained by this unitstats */
	private HashMap<StatType, Object> stats;

	/** Constructor for Stats.
	 * Can have a base or not.
	 * Input stats must not have no duplicates among type - will overwrite arbitrarily.
	 */
	public Stats(Stat... stats) {
		this.stats = new HashMap<>(TEMPLATE);
		for(Stat s : stats){
			this.stats.put(s.name, s.val);
		}
	}

	/**
	 * Constructor for Stats from a base stats and a collection of modifiers.
	 */
	public Stats(Stats base, Collection<Modifier> modifiers) throws IllegalArgumentException{
		this.stats = new HashMap<>(base.stats);
		stats.put(StatType.BASE, base);

		//Process modifiers - ignore non stat modifiers
		if(modifiers != null){
			for(Modifier m : modifiers){
				if(m instanceof StatModifier){
					StatModifier s = (StatModifier)m;
					StatType t = s.modifiedStat;
					if(s.modType == StatModifier.ModificationType.SET){
						stats.put(t, s.getModVal());
						continue;
					}
					Object newVal = stats.get(s.modifiedStat);
					if(s.getModVal() instanceof Integer){
						switch(s.modType){
						case ADD: newVal = (int) ((Integer)newVal + (int)s.getModVal());
						break;
						case MULTIPLY: newVal = (int) ((Integer)newVal * (double)s.getModVal());
						break;
						}
					}
					else if(s.getModVal() instanceof Double){
						switch(s.modType){
						case ADD: newVal = Mth.roundTo(((Double)newVal + (double)s.getModVal()), -2);
						break;
						case MULTIPLY: newVal = Mth.roundTo(((Double)newVal * (double)s.getModVal()), -2);
						break;
						}
					}
					stats.put(s.modifiedStat, newVal);
				}
			}
		}
	}

	/** Returns true if this is a base (has no base stat), false otherwise */
	private boolean isBase(){
		return ! stats.containsKey(StatType.BASE);
	}

	/** Returns the requested stat */
	public Object getStat(StatType type){
		return stats.get(type);
	}

	/**
	 * Returns a list of stats for the given array of stat types.
	 * Stats are returned in the same order they are queried in.
	 */
	private ArrayList<Stat> getStatsList(StatType[] t){
		ArrayList<Stat> s = new ArrayList<Stat>();
		for(StatType p : t){
			s.add(new Stat(p, stats.get(p)));
		}
		return s;
	}

	/**
	 * Returns an arrayList of the movement stats:
	 * 		- Move total
	 * 		- Grass cost
	 * 		- woods cost
	 * 		- mountain cost
	 */
	public ArrayList<Stat> getMovementStatsList(){
		StatType[] t = {StatType.MOVEMENT_TOTAL, StatType.GRASS_COST, 
				StatType.WOODS_COST, StatType.MOUNTAIN_COST};
		return getStatsList(t);
	}

	/**
	 * Returns an arrayList of the attack stats:
	 * 		- Attack
	 * 		- Attack Type
	 * 		- Attack Range
	 * 		- Physical Defense
	 * 		- Magic Defense
	 */
	public ArrayList<Stat> getAttackStatsList(){
		StatType[] t = {StatType.ATTACK, StatType.ATTACK_TYPE, 
				StatType.ATTACK_RANGE, StatType.PHYSICAL_DEFENSE, StatType.MAGIC_DEFENSE};
		return getStatsList(t);
	}

	/**
	 * Returns an arrayList of the standard stats:
	 * 		- Max Health
	 * 		- Mana Per Turn
	 * 		- Vision Range
	 * 		- Summon Range
	 */
	public ArrayList<Stat> getStandardStatsList(){
		StatType[] t = {StatType.MAX_HEALTH, StatType.MANA_PER_TURN, 
				StatType.VISION_RANGE, StatType.SUMMON_RANGE};
		return getStatsList(t);
	}

	/**
	 * Returns a new Stats with this (if this is a base) as the base
	 * or this' base if this is non-base, and the given modifiers
	 */
	public Stats modifiedWith(Collection<Modifier> modifiers) {
		if(isBase())
			return new Stats(this, modifiers);
		else
			return new Stats((Stats)getStat(StatType.BASE), modifiers);
	}

	/** Basic toString impelementation that shows off the stats this represents */
	@Override
	public String toString(){
		String s = "";
		Iterator<Stat> i = iterator();
		while(i.hasNext()){
			Stat st = i.next();
			if(st.name == StatType.BASE) continue;
			s += st.name + " : " + st.val + ", ";
		}
		return s;
	}

	/** Returns an iterator over these stats */
	public Iterator<Stat> iterator(){
		return new StatIterator();
	}

	/** An iterator over this stats that shows each stat in turn.
	 * Might not catch concurrent modification exceptions, so make sure
	 * to get a new iterator after the Stats has been modified. */
	private class StatIterator implements Iterator<Stat>{

		private int index;
		private ArrayList<Stat> statArr;

		private StatIterator(){
			index = 0;
			statArr = new ArrayList<Stat>();
			for(Map.Entry<StatType, Object> e : stats.entrySet()){
				statArr.add(new Stat(e.getKey(), e.getValue()));
			}
			Collections.sort(statArr);
		}

		/** Returns true if there is another stat to return */
		@Override
		public boolean hasNext() {
			return index < statArr.size();
		}

		/** Returns the next stat, in ordinal by type order */
		@Override
		public Stat next() {
			Stat s = statArr.get(index);
			index++;
			return s;
		}

		/** Removal not supported - throws runtime exception */
		@Override
		public void remove() {
			throw new RuntimeException("Not Supported");
		}

	}
}