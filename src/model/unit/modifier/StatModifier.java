package model.unit.modifier;

import model.unit.Unit;
import model.unit.stat.StatType;

/** A modifier that alters a unit's stat. */
public final class StatModifier extends Modifier {

  /** The stat this is modifying */
  public final StatType modifiedStat;

  /** Types of modifications a stat modifier can apply. */
  public enum ModificationType {
    SET_MIN,
    SET_MAX,
    ADD,
    MULTIPLY;

    /** Returns a symbol string that corresponds to this enum value */
    public String toSymbolString() {
      switch (this) {
        case ADD:
          return "+";
        case MULTIPLY:
          return "*";
        case SET_MIN:
        case SET_MAX:
          return "->";
        default:
          return "";
      }
    }
  }

  /** The type of modification - set, add, mult */
  public final ModificationType modType;

  /** The set/added/multed value of the stat */
  private Object val;

  /**
   * Constructor for dummy instance
   *
   * @param name - the name of this statModifier
   * @param turns - the total duration of this modifier (turns after this one). Can be
   *     Integer.MAX_VAL - interpreted as forever rather than the actual val
   * @param stackable - stack mode of this StatModifier.
   * @param stat - the stat to modify
   * @param modType - the operation on stat to perform
   * @param modVal - the value to modify by
   */
  public StatModifier(
      String name,
      int turns,
      StackMode stackable,
      StatType stat,
      ModificationType modType,
      Object modVal) {
    super(name, getImageFilename(stat, ((Number) modVal).doubleValue()), turns, stackable);
    modifiedStat = stat;
    this.modType = modType;
    val = modVal;
  }

  /**
   * Helper method for construction to get the image filename depending on the modified stat and the
   * mod value.
   */
  private static String getImageFilename(StatType statType, double modVal) {
    switch (statType) {
      case MAX_HEALTH:
        return modVal > 0 ? "spell_2_0.png" : "spell_3_0.png";
      case MIN_ATTACK:
        return modVal > 0 ? "spell_2_2.png" : "spell_3_2.png";
      case MAX_ATTACK:
        return modVal > 0 ? "spell_2_2.png" : "spell_3_2.png";
      case DAMAGE_REDUCTION:
        return modVal > 0 ? "spell_2_3.png" : "spell_3_3.png";
      case MOVEMENT_TOTAL:
        return modVal > 0 ? "spell_2_6.png" : "spell_3_6.png";
      case VISION_RANGE:
        return modVal > 0 ? "spell_2_8.png" : "spell_3_8.png";
      case WOODS_COST:
        return "spell_29_3.png";
      case MOUNTAIN_COST:
        return "spell_29_4.png";
      default:
        return null;
    }
  }

  /**
   * Constructor for cloning instances
   *
   * @param unit - The model.unit this is modifying.
   * @param source - the model.unit this modifier is tied to.
   * @param dummy - the StatModifier to make a copy of
   */
  private StatModifier(Unit unit, Unit source, StatModifier dummy) {
    super(unit, source, dummy);
    modifiedStat = dummy.modifiedStat;
    modType = dummy.modType;
    val = dummy.val;
    attachToUnit();
  }

  /** Returns the mod val - for what to do with it, see modType. Don't modify this, plz */
  public Object getModVal() {
    return val;
  }

  /** Returns the value of this custom modifier. */
  public Object getValue() {
    return val;
  }

  /** Returns a new dummy copy of this that's unique from a memory standpoint. */
  @Override
  public Modifier uniqueCopy() {
    if (!isDummy()) {
      throw new RuntimeException("Shouldn't call uniqueCopy except on a dummy");
    }
    return new StatModifier(name, getRemainingTurns(), stacking, modifiedStat, modType, val);
  }

  /**
   * Returns a new dummy copy of this that's unique from a memory standpoint with the given
   * remaining turns and stack mode.
   */
  @Override
  public Modifier uniqueCopy(int remainingTurns, StackMode stackMode) {
    if (!isDummy()) {
      throw new RuntimeException("Shouldn't call uniqueCopy except on a dummy");
    }
    return new StatModifier(name, remainingTurns, stackMode, modifiedStat, modType, val);
  }

  /** Returns a StatModifier clone of this with the given model.unit and source */
  @Override
  public Modifier clone(Unit unit, Unit source) {
    if (this.getClass() != StatModifier.class) {
      throw new RuntimeException("Clone method of " + this + " not overriden in subclass");
    }

    return new StatModifier(unit, source, this);
  }

  @Override
  public String toStatString() {
    if (modType == ModificationType.MULTIPLY) {
      return String.format(
          "%s%d%% %s", (double) val < 0 ? "" : "+", (int) ((double) val * 100 - 100), modifiedStat);
    } else {
      return String.format("%s%s %s", modType.toSymbolString(), val, modifiedStat);
    }
  }

  @Override
  public String toStringLong() {
    return toStringShort() + " " + toStatString();
  }

  @Override
  public String toStringFull() {
    return toStringShort() + " " + toStatString();
  }
}
