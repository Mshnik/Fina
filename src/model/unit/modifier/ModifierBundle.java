package model.unit.modifier;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import model.unit.Unit;

/** A set of modifiers applied to a single unit */
public final class ModifierBundle implements Collection<Modifier>{

	private LinkedList<Modifier> modifiers;
	
	/** Constructs a new Modifier Bundle
	 * @param m - the modifiers to put in this bundle. Doesn't deep copy - adds the actual modifiers
	 * to this bundle
	 */
	public ModifierBundle(Modifier... m){
		modifiers = new LinkedList<>();
		for(Modifier mod : m){
			add(mod);
		}
	}
	
	/** Adds the given modifier to this bundle.
	 * @throws IllegalArgumentException if m doesn't match other modifiers
	 * already in this bundle in number of turns or stackability to prevent
	 * bundles from breaking up */
	public boolean add(Modifier m) throws IllegalArgumentException{
		if(! modifiers.isEmpty()){
			Modifier base = modifiers.get(0);
			if(m.stackable != base.stackable || m.getRemainingTurns() != base.getRemainingTurns())
				throw new IllegalArgumentException(m + " incompatible with " + this);
		}
		modifiers.add(m);
		m.bundle = this;
		return true;
	}
	
	/** Safely removes the given modifier from this bundle as part of its kill() procedure
	 * @throws IllegalArgumentException if m doesn't belong to this bundle */
	void removeModifier(Modifier m) throws IllegalArgumentException{
		if(m.bundle != this)
			throw new IllegalArgumentException(m + " doesn't belong to " + this);
		modifiers.remove(m);
		m.bundle = null;
	}
	
	/** Clones each modifier in this bundle and adds them to a new ModifierBundle.
	 * Clones for model.unit model.unit, from model.unit source.
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
	
	/** Returns a copy of the list represented by this modifierBundle */
	public LinkedList<Modifier> getModifiers(){
		return new LinkedList<Modifier>(modifiers);
	}
	
	/** Returns true iff the given model.unit is affected by modifiers cloned from this bundle
	 */
	public boolean isAffecting(Unit u){
		for(Modifier m : u.getModifiers()){
			if(modifiers.contains(m.clonedFrom))
				return true;
		}
		return false;
	}
	
	
	/** Removes all modifiers clone from dummies in this bundle from the given model.unit 
	 */
	public void removeFrom(Unit u){
		for(Modifier m : u.getModifiers()){
			if(modifiers.contains(m.clonedFrom))
				m.kill();
		}
	}
	
	/** Returns true iff every modifier this bundle wraps is a dummy */
	public boolean isDummyBundle(){
		for(Modifier m : this){
			if(! m.isDummy()) return false;
		}
		return true;
	}

	/** Returns an iterator voer the modifers in this Bundle */
	@Override
	public Iterator<Modifier> iterator() {
		return modifiers.iterator();
	}
	
	/** Returns the turns remaining in this bundle, which is hopefully constant throughout
	 * modifiers in bundle.
	 */
	public int getTurnsRemaining(){
		return modifiers.get(0).getRemainingTurns();
	}
	
	/** Returns true iff modifiers in this bundle. Hopefully constant throughout.
	 * 
	 */
	public boolean isStackable(){
		return modifiers.get(0).stackable;
	}
	
	/** Returns the toStrings of the modifiers in this bundle */
	public String toString(){
		String s = "";
		for(Modifier m : modifiers){
			s += m.toString() + "  ";
		}
		return s;
	}

	@Override
	public int size() {
		return modifiers.size();
	}

	@Override
	public boolean isEmpty() {
		return modifiers.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return modifiers.contains(o);
	}

	@Override
	public Object[] toArray() {
		return modifiers.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return modifiers.toArray(a);
	}

	/** Unsupported */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Removal not supported by ModifierBundle");
	}
	
	/** Safe removal called during a modifier's kill routine */
	void removeSafe(Modifier m){
		modifiers.remove(m);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object o : c){
			if(! contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Modifier> c) throws IllegalArgumentException{
		for(Modifier m : c){
			add(m);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for(Object o : c){
			remove(o);
		}
		return true;
	}

	/** Unsupported */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Retention not supported in ModifierBundle");
	}

	/** Clears this bundle by killing all modifiers in it */
	@Override
	public void clear() {
		for(Modifier m : getModifiers()){
			m.kill();
		}
		modifiers.clear();
	}
}
