package model.unit.modifier;

import model.unit.Unit;

/**
 * A modifier that is beyond the simple boundaries of a stat modification Usually works off of the
 * name, so names must be unique and well represented.
 *
 * @author MPatashnik
 */
public final class CustomModifier extends Modifier {

  /** The token used to replace percentages in descriptions. */
  private static final String REPLACE_PERCENTAGE_DESCRIPTION = "-x%-";

  /** The token used to replace other values in descriptions. */
  private static final String REPLACE_STANDARD_DESCRIPTION = "-x-";

  /** True if this applies to Buildings */
  public final boolean appliesToBuildings;

  /** True iff this applies to Commanders */
  public final boolean appliesToCommanders;

  /** True iff this applies to Combatants */
  public final boolean appliesToCombatants;

  /** A description of this Modifier - what it does, etc */
  public final String description;

  /** A numeric value for this modifier. If unneded, this will be 0 */
  public final Number val;

  /**
   * Constructor for dummy instance
   *
   * @param name - the name of this modifier
   * @param description - a description of this modifier
   * @param val - the magnitude of the modifier, as needed
   * @param turns - the total duration of this modifier (turns after this one). Can be
   *     Integer.MAX_VAL - interpreted as forever rather than the actual val
   * @param stackable - stack mode of this modifier.
   * @param buildings - true iff this modifier can apply to buildings
   * @param commanders - true iff this modifier can apply to commanders
   * @param combatants - true iff this modifier can apply to combatants
   */
  CustomModifier(
      String name,
      String description,
      Number val,
      int turns,
      StackMode stackable,
      boolean buildings,
      boolean commanders,
      boolean combatants) {
    super(name, turns, stackable);
    this.val = val;
    this.appliesToBuildings = buildings;
    this.appliesToCommanders = commanders;
    this.appliesToCombatants = combatants;

    if (val == null
        && (description.contains(REPLACE_PERCENTAGE_DESCRIPTION)
            || description.contains(REPLACE_STANDARD_DESCRIPTION))) {
      throw new RuntimeException(
          "Description shouldn't contain replace token with null value. Got: " + description);
    }
    if (description.contains(REPLACE_PERCENTAGE_DESCRIPTION) && !(val instanceof Double)) {
      throw new RuntimeException("If replacing percentage, expected a double. Got: " + val);
    }
    if (description.contains(REPLACE_PERCENTAGE_DESCRIPTION)
        && description.contains(REPLACE_STANDARD_DESCRIPTION)) {
      throw new RuntimeException(
          "Description can't contain both percentage and flat value. Got: " + description);
    }

    if (val != null && val.doubleValue() != 0) {
      if (description.contains(REPLACE_PERCENTAGE_DESCRIPTION)) {
        int percentVal = (int) (val.doubleValue() * 100);
        this.description = description.replaceAll(REPLACE_PERCENTAGE_DESCRIPTION, percentVal + "%");
      } else if (description.contains(REPLACE_STANDARD_DESCRIPTION)) {
        this.description = description.replaceAll(REPLACE_STANDARD_DESCRIPTION, val.toString());
      } else {
        this.description = description;
      }
    } else {
      this.description = description.replaceAll("-","");
    }
  }

  /**
   * Constructor for cloning instances
   *
   * @param unit - The model.unit this is modifying.
   * @param source - the model.unit this modifier is tied to.
   * @param dummy - the modifier to make a copy of
   */
  private CustomModifier(Unit unit, Unit source, CustomModifier dummy) {
    super(unit, source, dummy);
    this.description = dummy.description;
    this.val = dummy.val;
    this.appliesToBuildings = dummy.appliesToBuildings;
    this.appliesToCombatants = dummy.appliesToCombatants;
    this.appliesToCommanders = dummy.appliesToCommanders;
    attachToUnit();
  }

  /** Returns the value of this custom modifier. May be null. */
  public Number getValue() {
    return val;
  }

  @Override
  public Modifier uniqueCopy() {
    if (!isDummy()) {
      throw new RuntimeException("Shouldn't call uniqueCopy except on a dummy");
    }
    return new CustomModifier(
        name,
        description,
        val,
        getRemainingTurns(),
        stacking,
        appliesToBuildings,
        appliesToCommanders,
        appliesToCombatants);
  }

  @Override
  public Modifier uniqueCopy(int remainingTurns, StackMode stackMode) {
    if (!isDummy()) {
      throw new RuntimeException("Shouldn't call uniqueCopy except on a dummy");
    }
    return new CustomModifier(
        name,
        description,
        val,
        remainingTurns,
        stackMode,
        appliesToBuildings,
        appliesToCommanders,
        appliesToCombatants);
  }

  /**
   * Clones this for the given model.unit, source - creates a new customModifier(model.unit, source,
   * this)
   */
  @Override
  public Modifier clone(Unit unit, Unit source) {
    return new CustomModifier(unit, source, this);
  }

  @Override
  public String toStatString() {
    return description;
  }

  @Override
  public String toStringLong() {
    return toStringShort() + " " + description;
  }

  @Override
  public String toStringFull() {
    return toStringShort() + " " + description;
  }
}
