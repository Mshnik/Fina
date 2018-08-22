package model.unit.ability;

import model.board.MPoint;
import model.unit.Commander;
import model.unit.Unit;
import model.unit.modifier.Modifier;
import model.unit.modifier.ModifierBundle;

import java.util.Set;

public class ModifierAbility extends Ability {

  /** The bundle of modifiers that make up this ability. Should be dummies in here. */
  private final ModifierBundle modifiers;

  /**
   * ModifierAbility Constructor
   *
   * @param name - the Name of this ability
   * @param manaCost - the mana cost of using this ability. 0 if passive
   * @param caster - the Commander who owns this ability
   * @param castDist - the distance from the commander this ability can be cast
   * @param multiCast - true iff this can be used multiple times in a turn
   * @param appliesToAllied - true iff this ability can affect allied units
   * @param appliesToFoe - true iff this ability can affect non-allied units
   * @param cloud - the cloud of points (around the target as (0,0)) that it affects
   * @param modifiers - a bundle of modifiers to apply when this ability is cast
   */
  public ModifierAbility(
      String name,
      int manaCost,
      Commander caster,
      int castDist,
      boolean multiCast,
      boolean appliesToAllied,
      boolean appliesToFoe,
      Set<MPoint> cloud,
      ModifierBundle modifiers) {

    super(name, manaCost, caster, castDist, multiCast, appliesToAllied, appliesToFoe, cloud);

    if (!modifiers.isDummyBundle())
      throw new IllegalArgumentException("Can't make ability of non-dummy abilities");
    this.modifiers = modifiers;
  }

  /** Returns the modifierBundle this is wrapping. No editing please! */
  public ModifierBundle getModifiers() {
    return modifiers;
  }

  /** Applies this modifierAbility to the given model.unit */
  @Override
  protected void affect(Unit u) {
    modifiers.clone(u, caster);
  }

  /** Returns the modifiers granted through this ability from model.unit u */
  @Override
  public void remove(Unit u) {
    modifiers.removeFrom(u);
  }

  /** Returns true iff the given model.unit has the modifiers in this bundle */
  public boolean isAffecting(Unit u) {
    return modifiers.isAffecting(u);
  }

  @Override
  public String toStringLong() {
    String s = toStringShort() + ": [";
    for (Modifier m : modifiers) {
      s += m.toStringShort() + " ";
    }
    return s + "]";
  }

  @Override
  public String toStringFull() {
    String s = toStringShort() + ": [";
    for (Modifier m : modifiers) {
      s += m.toStringShort() + " ";
    }
    return s + "]";
  }
}
