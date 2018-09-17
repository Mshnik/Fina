package model.unit.modifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import model.unit.Unit;
import model.unit.modifier.Modifier.StackMode;

/** A set of modifiers applied to a single unit */
public final class ModifierBundle implements Collection<Modifier> {

  private final List<Modifier> modifiers;

  /**
   * Constructs a new Modifier Bundle
   *
   * @param m - the modifiers to put in this bundle. Doesn't deep copy - adds the actual modifiers
   *     to this bundle
   */
  public ModifierBundle(Modifier... m) {
    this(Arrays.asList(m));
  }

  /**
   * Constructs a new Modifier Bundle
   *
   * @param modifiers- the modifiers to put in this bundle. Doesn't deep copy - adds the actual
   *     modifiers to this bundle
   */
  public ModifierBundle(Iterable<Modifier> modifiers) {
    this.modifiers = Collections.synchronizedList(new LinkedList<>());
    synchronized (this.modifiers) {
      for (Modifier mod : modifiers) {
        add(mod);
      }
    }
  }

  /**
   * Constructs a new Modifier Bundle from copying the given bundle along with the given modifiers.
   */
  public ModifierBundle(ModifierBundle bundle, Modifier... m) {
    modifiers = Collections.synchronizedList(new LinkedList<>());
    synchronized (this.modifiers) {
      for (Modifier mod : bundle.modifiers) {
        add(mod);
      }
      for (Modifier mod : m) {
        add(mod);
      }
    }
  }

  /**
   * Adds the given modifier to this bundle.
   *
   * @throws IllegalArgumentException if m doesn't match other modifiers already in this bundle in
   *     number of turns or stackability to prevent bundles from breaking up
   */
  public boolean add(Modifier m) throws IllegalArgumentException {
    synchronized (modifiers) {
      if (!modifiers.isEmpty()) {
        Modifier base = modifiers.get(0);
        if (m.stacking != base.stacking || m.getRemainingTurns() != base.getRemainingTurns())
          throw new IllegalArgumentException(m.name + " incompatible with " + this);
      }
      modifiers.add(m);
      m.setBundle(this);
      return true;
    }
  }

  /** Calls clone(unit, unit). */
  public ModifierBundle clone(Unit unit) {
    return clone(unit, unit);
  }

  /**
   * Clones each modifier in this bundle and adds them to a new ModifierBundle. Clones for
   * model.unit model.unit, from model.unit source. Returns that bundle
   */
  public ModifierBundle clone(Unit unit, Unit source) {
    ModifierBundle b = new ModifierBundle();
    synchronized (modifiers) {
      for (Modifier m : modifiers) {
        Modifier m2 = m.clone(unit, source);
        b.add(m2);
      }
    }
    return b;
  }

  /** Returns the modifier at the given index. */
  public Modifier getModifier(int index) {
    synchronized (modifiers) {
      return modifiers.get(index);
    }
  }

  /** Returns a non-modifiable view of the list represented by this modifierBundle */
  public List<Modifier> getModifiers() {
    synchronized (modifiers) {
      return Collections.unmodifiableList(modifiers);
    }
  }

  /** Returns true iff the given model.unit is affected by modifiers cloned from this bundle */
  public boolean isAffecting(Unit u) {
    synchronized (modifiers) {
      for (Modifier m : u.getModifiers()) {
        if (modifiers.contains(m.clonedFrom)) return true;
      }
      return false;
    }
  }

  /** Removes all modifiers clone from dummies in this bundle from the given model.unit */
  public void removeFrom(Unit u) {
    synchronized (modifiers) {
      for (Modifier m : u.getModifiers()) {
        if (modifiers.contains(m.clonedFrom)) m.kill();
      }
    }
  }

  /** Returns true iff every modifier this bundle wraps is a dummy */
  public boolean isDummyBundle() {
    synchronized (modifiers) {
      for (Modifier m : this) {
        if (!m.isDummy()) return false;
      }
      return true;
    }
  }

  /** Returns an iterator voer the modifers in this Bundle */
  @Override
  public Iterator<Modifier> iterator() {
    synchronized (modifiers) {
      return modifiers.iterator();
    }
  }

  /**
   * Returns the turns remaining in this bundle, which is hopefully constant throughout modifiers in
   * bundle.
   */
  public int getTurnsRemaining() {
    synchronized (modifiers) {
      return modifiers.get(0).getRemainingTurns();
    }
  }

  /** Returns true iff modifiers in this bundle. Hopefully constant throughout. */
  public StackMode getStackMode() {
    synchronized (modifiers) {
      return modifiers.get(0).stacking;
    }
  }

  /** Returns the toStrings of the modifiers in this bundle */
  public String toString() {
    synchronized (modifiers) {
      String s = "";
      for (Modifier m : modifiers) {
        s += m.toString() + "  ";
      }
      return s;
    }
  }

  /** Returns the toStatStrings of the modifiers in this bundle */
  public String toStatString() {
    synchronized (modifiers) {
      String s = "";
      for (Modifier m : modifiers) {
        s += m.toStatString() + "  ";
      }
      return s;
    }
  }

  @Override
  public int size() {
    synchronized (modifiers) {
      return modifiers.size();
    }
  }

  @Override
  public boolean isEmpty() {
    synchronized (modifiers) {
      return modifiers.isEmpty();
    }
  }

  @Override
  public boolean contains(Object o) {
    synchronized (modifiers) {
      return modifiers.contains(o);
    }
  }

  @Override
  public Object[] toArray() {
    synchronized (modifiers) {
      return modifiers.toArray();
    }
  }

  @Override
  public <T> T[] toArray(T[] a) {
    synchronized (modifiers) {
      return modifiers.toArray(a);
    }
  }

  /** Unsupported */
  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Removal not supported by ModifierBundle");
  }

  /** Safe removal called during a modifier's kill routine */
  void removeSafe(Modifier m) {
    synchronized (modifiers) {
      modifiers.remove(m);
    }
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    synchronized (modifiers) {
      for (Object o : c) {
        if (!contains(o)) return false;
      }
      return true;
    }
  }

  @Override
  public boolean addAll(Collection<? extends Modifier> c) throws IllegalArgumentException {
    synchronized (modifiers) {
      for (Modifier m : c) {
        add(m);
      }
      return true;
    }
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    synchronized (modifiers) {
      for (Object o : c) {
        remove(o);
      }
      return true;
    }
  }

  /** Unsupported */
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("Retention not supported in ModifierBundle");
  }

  /** Clears this bundle by killing all modifiers in it */
  @Override
  public void clear() {
    synchronized (modifiers) {
      for (Modifier m : getModifiers()) {
        m.kill();
      }
      modifiers.clear();
    }
  }
}
