package unit.stat;

import java.util.Objects;

/** A single stat - a type, val tuple. Val can be int, attack type, or double.
 * Stats sortable by their name, ordinally */
public class Stat implements Comparable<Stat>{
	/** The name of this stat */
	public final StatType name;
	/** The value of this stat. May need casting to use. */
	public final Object val;
	
	public Stat(StatType n, Object v){
		name = n;
		val = v;
	}

	/** Stats are comparable by their names' ordinal in StatType */
	@Override
	public int compareTo(Stat o) {
		return name.ordinal() - o.name.ordinal();
	}
	
	/** Simple toString implementation of a stat */
	@Override
	public String toString(){
		return name + ":" + val;
	}
	
	/** Two stats are equal if they have the same type and same val */
	@Override
	public boolean equals(Object o){
		if(! (o instanceof Stat)) return false;
		Stat s = (Stat)o;
		return name.equals(s.name) && val.equals(s.val);
	}
	
	/** Hashes a stat based on its name and val */
	@Override
	public int hashCode(){
		return Objects.hash(name, val);
	}
}