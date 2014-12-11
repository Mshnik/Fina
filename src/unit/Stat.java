package unit;

/** A single stat - a string, val tuple. Val can be int, attack type, or double */
public class Stat{
	/** The name of this stat */
	public final String name;
	/** The value of this stat. May need casting to use. */
	public final Object val;
	
	public Stat(String n, Object v){
		name = n;
		val = v;
	}
}