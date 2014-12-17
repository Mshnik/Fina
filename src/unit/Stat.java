package unit;

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
}