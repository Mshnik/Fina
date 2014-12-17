package unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Holder for the stats for a unit.
 * Unless otherwise noted, all stats are non-negative. */
public class UnitStats{
	
	/** Base stats for everything, to be overridden as template.
	 * Initializes all stats to their base 0-ish vals
	 */
	private static final HashMap<StatType, Object> TEMPLATE;
	
	static{
		TEMPLATE = new HashMap<StatType, Object>();
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
	}
	
	/** The stats maintained by this unitstats */
	private HashMap<StatType, Object> stats;

	/** Constructor for UnitStats.
	 * Can have a base or not.
	 * Input stats must not have no duplicates among type - will overwrite arbitrarily.
	 */
	public UnitStats(Stat... stats) {
		this.stats = new HashMap<StatType, Object>();
		for(Stat s : stats){
			this.stats.put(s.name, s.val);
		}
	}
	
	/** Constructor for UnitStats from a base stats and a collection of modifiers.
	 */
	public UnitStats(UnitStats base, Collection<UnitModifier> modifiers) throws IllegalArgumentException{
		this.stats = new HashMap<StatType, Object>(base.stats);
		
		//Process modifiers
		if(modifiers != null){
//			for( UnitModifier m : modifiers){
//				//TODO
//			}
		}
	}
	
	/** Returns true if this is a base (has no base stat), false otherwise */
	public boolean isBase(){
		return ! stats.containsKey(StatType.BASE);
	}
	
	/** Returns the requested stat */
	public Object getStat(StatType type){
		return stats.get(type);
	}

	/** Returns a new UnitStats with this (if this is a base) as the base
	 * or this' base if this is non-base, and the given modifiers */
	public UnitStats modifiedWith(Collection<UnitModifier> modifiers) {
		if(isBase())
			return new UnitStats(this, modifiers);
		else
			return new UnitStats((UnitStats)getStat(StatType.BASE), modifiers);
	}
	
	/** Basic toString impelementation that shows off the stats this represents */
	@Override
	public String toString(){
		String s = "";
		Iterator<Stat> i = iterator();
		while(i.hasNext()){
			Stat st = i.next();
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
	 * to get a new iterator after the UnitStats has been modified. */
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
