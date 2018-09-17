package model.unit.modifier;

import java.util.Collection;
import model.game.Stringable;
import model.unit.Unit;

/** A Modifier for a model.unit - a buff or nerf, etc. */
public abstract class Modifier implements Stringable {

  /**
   * The bundle this is a member of, if any. Once assigned to a bundle, shouldn't be assigned to
   * another one.
   */
  private ModifierBundle bundle;

  /** The name of this modifier */
  public final String name;

  /** The image filename to draw for this modifier. Only draws if it's the first in the bundle. */
  public final String imageFilename;

  /** The model.unit this is modifying */
  public final Unit unit;

  /** The source of this modification */
  public final Unit source;

  /** The remaining turns for this Modifier. If 0, on its last turn. */
  private int remainingTurns;

  public enum StackMode {
    /** No stacking - do not apply new modifier if unit already has one. */
    NONE_DO_NOT_APPLY,
    /** No stacking - extend duration to max of old,new durations if unit already has one. */
    DURATION_MAX,
    /** No stacking - add durations if unit already has one. */
    DURATION_ADD,
    /** Stackable - unit has both modifications concurrently. */
    STACKABLE,
  }

  /** The stacking mode of this modifier. */
  public final StackMode stacking;

  /** True once this has been attached to a model.unit */
  private boolean attached;

  /** The modifier this was cloned from. Null if this is a dummy */
  final Modifier clonedFrom;

  /**
   * Constructor for dummy instance
   *
   * @param name - the name of this modifier
   * @param imageFilename - the image to draw for this modifier. Only draws if this is the first in
   *     the bundle.
   * @param turns - the total duration of this modifier (turns after this one). Can be
   *     Integer.MAX_VAL - interpreted as forever rather than the actual val
   * @param stacking - the stack mode of this Modifier that determines how it interacts with
   *     modifiers of the same name.
   */
  public Modifier(String name, String imageFilename, int turns, StackMode stacking) {
    this.name = name;
    this.imageFilename = imageFilename;
    unit = null;
    remainingTurns = turns;
    source = null;
    attached = false;
    this.stacking = stacking;
    clonedFrom = null;

    if (turns == Integer.MAX_VALUE
        && (stacking == StackMode.DURATION_ADD || stacking == StackMode.DURATION_MAX)) {
      throw new RuntimeException(
          "Infinite duration modifiers can't have duration add or duration max");
    }
  }

  /**
   * Constructor for cloning instances
   *
   * @param unit - The model.unit this is modifying.
   * @param source - the model.unit this modifier is tied to.
   * @param dummy - the unitModifier to make a copy of
   */
  public Modifier(Unit unit, Unit source, Modifier dummy) {
    attached = false;
    this.name = dummy.name;
    this.imageFilename = dummy.imageFilename;
    this.unit = unit;
    this.source = source;
    this.stacking = dummy.stacking;
    remainingTurns = dummy.remainingTurns;
    clonedFrom = dummy;
  }

  /** Returns the value of this modifier. May be null if not used. */
  public abstract Object getValue();

  /**
   * Returns the value of this modifier formatted nicely as a string. If getValue() returns null,
   * this returns the empty string.
   */
  String getValueFormatted() {
    Object val = getValue();
    if (val instanceof Double) {
      return String.format(
          "%d%%", (int) (((double) val) * 100) - (this instanceof StatModifier ? 100 : 0));
    } else if (val instanceof Integer && ((int) val) < 0) {
      return Integer.toString(-(int) val);
    } else if (val != null) {
      return val.toString();
    } else {
      return "";
    }
  }

  /** Checks whether this Modifier has an associated bundle. */
  synchronized boolean hasBundle() {
    return bundle != null;
  }

  /** Returns the associated bundle, if any. Null otherwise. */
  synchronized ModifierBundle getBundle() {
    return bundle;
  }

  /** Sets the associated bundle. */
  synchronized void setBundle(ModifierBundle bundle) {
    this.bundle = bundle;
  }

  /**
   * Returns a new copy of this that is also a dummy - allows for separate clone chains of identical
   * Modifiers. Should only be called on a dummy.
   */
  public abstract Modifier uniqueCopy();

  /**
   * Returns a new copy of this that is also a dummy with some values overriden - allows for
   * separate clone chains of identical Modifiers. Should only be called on a dummy.
   */
  public abstract Modifier uniqueCopy(int turns, StackMode stacking);

  /** Calls clone(unit, unit). */
  public final Modifier clone(Unit unit) {
    return clone(unit, unit);
  }

  /**
   * Clones a copy of this. Should fully produce a clone of this, and do model.unit attaching. Must
   * be overriden in subclasses to use their constructors instead
   */
  public abstract Modifier clone(Unit unit, Unit source);

  /** Call when construction of a non-dummy instance is done - adds to affected model.unit */
  void attachToUnit() {
    if (isDummy() || attached)
      throw new RuntimeException("Can't attach a dummy or already attached modifier");
    boolean ok = unit.addModifier(this);
    if (ok) {
      source.addGrantedModifier(this);
      if (unit.owner != null && unit.owner.game.getController().hasFrame()) {
        unit.owner.game.getController().frame.getGamePanel().refreshModifierIconFor(unit);
      }
    }
  }

  /**
   * Returns a copy if another copy of this exists within the given collection Checks each member of
   * the given collection for having the same clonedFrom as this. Otherwise returns null If this is
   * a dummy, throws runtimeException
   */
  public Modifier cloneInCollection(Collection<? extends Modifier> elms) throws RuntimeException {
    if (isDummy()) throw new RuntimeException("Can't check if dummy is in list");
    for (Modifier m : elms) {
      if (m.clonedFrom == clonedFrom) return m;
    }
    return null;
  }

  /** Returns true if this is a dummy (model.unit is null ) */
  public boolean isDummy() {
    return unit == null;
  }

  /** Returns true iff this Modifier has infinite duration. */
  public boolean isInfiniteDuration() {
    return remainingTurns == Integer.MAX_VALUE;
  }

  /** Returns the remaining turns of this modifier */
  public int getRemainingTurns() {
    return remainingTurns;
  }

  /** Alters the remaining turns by the given delta. */
  public void changeRemainingTurns(int delta) {
    remainingTurns += delta;
  }

  /**
   * Decrements the remaining turns. Doesn't dec if is infinite (Integer.MAX_VAL) or is dummy Method
   * calling this method should use the return of the method to maybe remove this modifier.
   *
   * @return true iff this is is now dead (remainingTurns <= 0)
   */
  public boolean decRemainingTurns() {
    if (isInfiniteDuration() || isDummy()) return false;
    remainingTurns--;
    return remainingTurns <= 0;
  }

  /**
   * Kills this modifier - removes it from its source and its model.unit. If already removed from
   * model.unit and source Throws exception if dummy
   */
  public void kill() {
    if (isDummy()) throw new RuntimeException("Can't kill a dummy modifier");
    unit.removeModifier(this);
    source.removeGrantedModifier(this);
    if (bundle != null) {
      bundle.removeSafe(this);
    }
  }

  /** Returns a stat string for InfoPanel painting. Subclass should finish implementation */
  public abstract String toStatString();

  @Override
  public String toString() {
    return (isDummy()
        ? "Dummy Modifier"
        : (attached ? "Unattached Modifier " : "Modifier on" + unit.toString()));
  }

  @Override
  public String toStringShort() {
    return (isDummy()
            ? "Dummy Modifier"
            : (attached ? "Unattached Modifier " : "Modifier on" + unit.toString()))
        + remainingTurns
        + " turns remaining";
  }
}
