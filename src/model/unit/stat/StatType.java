package model.unit.stat;

/** The different stats that are present in a unitstats */
public enum StatType {
  /** The base (pre-modification) stats this was calculated from, if any */
  BASE,
  /**
   * The health cap for this model.unit. Actual health level is kept track of by model.unit.
   * maxHealth >= 0.
   */
  MAX_HEALTH,
  /** The mana generation/cost of this model.unit, if any. Positive is generation */
  MANA_PER_TURN,
  /** The number of actions per turn this unit can perform, if any. Usually 0. */
  ACTIONS_PER_TURN,
  /**
   * Min damage dealt when attacking. attack >= 0. If a unit's attack == 0, this unit cannot attack.
   */
  MIN_ATTACK,
  /**
   * Max damage dealt when attacking. attack >= 0. If a unit's attack == 0, this unit cannot attack.
   */
  MAX_ATTACK,
  /** Reduction on damage taken. Damage Reduction >= 0. */
  DAMAGE_REDUCTION,
  /**
   * Min distance at which another model.unit can be attacked, inclusive. Distance is measured using
   * manhattan distance - 1. Units with range 0 are melee, others are ranged range >= 0.
   */
  MIN_ATTACK_RANGE,
  /**
   * Max distance at which another model.unit can be attacked, inclusive. Distance is measured using
   * manhattan distance - 1. Units with range 0 are melee, others are ranged range >= 0.
   */
  MAX_ATTACK_RANGE,
  /**
   * Distance at which another model.unit can be summoned Distance is measured using manhattan
   * distance - 1. range >= 0.
   */
  SUMMON_RANGE,
  /** Total movement a model.unit can do in a turn */
  MOVEMENT_TOTAL,
  /** Vision radius of this model.unit */
  VISION_RANGE,
  /** Grass movement cost */
  GRASS_COST,
  /** Woods movement cost */
  WOODS_COST,
  /** Mountain movement cost */
  MOUNTAIN_COST,
  /** Sea movement cost */
  SEA_COST;

  /** Overrides traditional enum tostring - returns camel case, no underscores */
  @Override
  public String toString() {
    String s = super.toString();
    s = s.replace('_', ' ').toLowerCase();
    String finishedS = "";
    do {
      int i = s.indexOf(' ');
      if (i == -1) i = s.length();
      finishedS += Character.toUpperCase(s.charAt(0)) + s.substring(1, i);
      if (i != s.length()) {
        finishedS += ' ';
        s = s.substring(i + 1);
      } else {
        break;
      }
    } while (true);

    return finishedS;
  }

  /** Returns true iff this stat is an attack-related stat. */
  public boolean isAttackStat() {
    return this == MIN_ATTACK
        || this == MAX_ATTACK
        || this == MIN_ATTACK_RANGE
        || this == MAX_ATTACK_RANGE
        || this == DAMAGE_REDUCTION;
  }

  /** Returns true iff this stat is a movement-related stat. */
  public boolean isMovementStat() {
    return this == MOVEMENT_TOTAL
        || this == GRASS_COST
        || this == WOODS_COST
        || this == MOUNTAIN_COST
        || this == SEA_COST;
  }

  /**
   * Returns true iff this stat should be omittied if it is 0, false if it should always be shown.
   */
  public boolean omitIfZero() {
    switch (this) {
      case BASE:
      case MAX_HEALTH:
      case MAX_ATTACK:
      case MAX_ATTACK_RANGE:
      case MOVEMENT_TOTAL:
      case VISION_RANGE:
        return false;
      case ACTIONS_PER_TURN:
      case MIN_ATTACK:
      case MIN_ATTACK_RANGE:
      case DAMAGE_REDUCTION:
      case MANA_PER_TURN:
      case SUMMON_RANGE:
      case GRASS_COST:
      case WOODS_COST:
      case MOUNTAIN_COST:
      case SEA_COST:
        return true;
      default:
        return false;
    }
  }

  /** Returns the class of the given type */
  public static Class<?> getClassOfType(StatType t) {
    switch (t) {
      case MIN_ATTACK:
      case MAX_ATTACK:
      case MIN_ATTACK_RANGE:
      case MAX_ATTACK_RANGE:
      case GRASS_COST:
      case DAMAGE_REDUCTION:
      case MANA_PER_TURN:
      case ACTIONS_PER_TURN:
      case MAX_HEALTH:
      case MOUNTAIN_COST:
      case MOVEMENT_TOTAL:
      case SUMMON_RANGE:
      case VISION_RANGE:
      case WOODS_COST:
      case SEA_COST:
        return Integer.class;
      case BASE:
        return Stats.class;
      default:
        return null;
    }
  }
}
