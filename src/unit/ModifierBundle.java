package unit;

import java.util.Iterator;
import java.util.LinkedList;

public class ModifierBundle implements Iterable<Modifier>{

	private LinkedList<Modifier> modifiers;
	
	/** Constructs a new Modifier Bundle
	 * @param m - the modifiers to put in this bundle. Doesn't deep copy - adds the actual modifiers
	 * to this bundle
	 */
	public ModifierBundle(Modifier... m){
		modifiers = new LinkedList<Modifier>();
		for(Modifier mod : m){
			add(mod);
		}
	}
	
	/** Adds the given modifier to this bundle */
	private void add(Modifier m){
		modifiers.add(m);
		m.bundle = this;
	}
	
	/** Clones each modifier in this bundle and adds them to a new ModifierBundle.
	 * Clones for unit unit, from unit source.
	 * Returns that bundle
	 */
	public ModifierBundle clone(Unit unit, Unit source){
		ModifierBundle b = new ModifierBundle();
		for(Modifier m : modifiers){
			Modifier m2 = m.clone(unit, source);
			b.add(m2);
		}
		return b;
	}

	/** Returns an iterator voer the modifers in this Bundle */
	@Override
	public Iterator<Modifier> iterator() {
		return modifiers.iterator();
	}
	
	/** Returns the toStrings of the modifiers in this bundle */
	public String toString(){
		String s = "";
		for(Modifier m : modifiers){
			s += m.toString() + "  ";
		}
		return s;
	}
}
