package model.unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.game.Stringable;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.Modifiers;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

/**
 * Represents any thing that can be owned by a player. Maintains its health and stats, and
 * modifiers.
 *
 * @author MPatashnik
 */
public abstract class Unit implements Stringable {

  /** The name of this model.unit */
  public final String name;

  /** The image to use when drawing this. */
  private final String imageFilename;

  /**
   * The level of this model.unit. If less than the level of the owner, may be eligible for an
   * upgrade
   */
  public final int level;

  /** The mana spent to summon this model.unit */
  public final int manaCost;

  /** The additional mana cost to summon each copy of this unit past the first. */
  protected final int manaCostScaling;

  /** The player that owns this model.unit */
  public final Player owner;

  /** The tile this model.unit is on. Should not ever share a tile with another model.unit */
  protected Tile location;

  /** The current health of this Unit. If 0 or negative, this is dead. Can't be above maxHealth */
  private int health;

  /**
   * The current stats of this model.unit. These are updated whenever unitModifiers are added or
   * removed. Should never be null, but may be empty-ish
   */
  Stats stats;

  /** The modifiers this is the source of */
  private LinkedList<Modifier> grantedModifiers;

  /** A set of modifiers that are currently affecting this model.unit */
  private LinkedList<Modifier> modifiers;

  /**
   * Constructor for Unit. Also adds this model.unit to the tile it is on as an occupant, and its
   * owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana. Tile and owner can be null in a dummy
   * (not on model.board) instance
   *
   * @param owner - the player owner of this model.unit
   * @param name - the name of this model.unit. Can be generic, multiple units can share a name
   * @param imageFilename - the image to use to represent this unit.
   * @param level - the level of this model.unit - the age this belongs to
   * @param manaCost - the cost of summoning this model.unit. Should be a positive number.
   * @param manaCostScaling - the additional cost to pay for each copy of this unit beyond the
   *     first.
   * @param stats - the base unmodified stats of this model.unit.
   */
  public Unit(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      Stats stats)
      throws IllegalArgumentException, RuntimeException {
    if (manaCost < 0)
      throw new IllegalArgumentException("manaCosts should be provided as positive ints");
    this.owner = owner;
    this.level = level;
    this.name = name;
    this.imageFilename = imageFilename;
    this.manaCost = manaCost;
    this.manaCostScaling = manaCostScaling;
    this.stats = new Stats(stats, null);
    health = getMaxHealth();
    modifiers = new LinkedList<>();
    grantedModifiers = new LinkedList<>();
  }

  /** Returns the mana cost of the given player summoning a copy of this, including scaling. */
  public int getManaCostWithScalingForPlayer(Player p) {
    return manaCost
        + (int) p.getUnits().stream().filter(u -> u.name.equals(name)).count() * manaCostScaling;
  }

  /** Returns a copy of this for the given player, on the given tile */
  public final Unit clone(Player owner, Tile location) {
    int cost = getManaCostWithScalingForPlayer(owner);
    if (cost > 0 && owner.getMana() < cost)
      throw new RuntimeException(owner + " can't afford to summon model.unit with cost " + cost);
    Unit u = createClone(owner);
    u.location = location;
    location.addOccupyingUnit(u);
    owner.addUnit(u);
    if (owner.getLevel() < u.level)
      throw new RuntimeException(owner + " can't summon model.unit with higher level than it");
    owner.getCommander().addMana(Math.min(0, -cost));
    return u;
  }

  /** Create a copy of this for the given player. Used internally in clone. */
  protected abstract Unit createClone(Player owner);

  /** Returns the level of this unit */
  public int getLevel() {
    return level;
  }

  /** Returns the mana cost of this unit. */
  public int getManaCost() {
    return manaCost;
  }

  /** Returns the mana cost scaling of this unit. */
  public int getManaCostScaling() {
    return manaCostScaling;
  }

  /** Refreshes this' stats with the locally stored modifiers */
  public void refreshStats() {
    stats = stats.modifiedWith(modifiers);
  }

  /**
   * Call at the beginning of every turn. Can be overridden in subclasses, but those classes should
   * call the super version before doing their own additions. - ticks down modifiers and
   * re-calculates stats, if necessary.
   */
  public void refreshForTurn() {
    LinkedList<Modifier> deadModifiers = new LinkedList<Modifier>();
    for (Modifier m : modifiers) {
      if (m.decRemainingTurns() || !m.source.isAlive()) deadModifiers.add(m);
    }
    if (!deadModifiers.isEmpty()) {
      modifiers.removeAll(deadModifiers);
      refreshStats();
    }

    // Start of turn modifiers.
    for (Modifier m : getModifiersByName(Modifiers.TOUGHNESS)) {
      changeHealth(((CustomModifier) m).val.intValue(), this);
    }
  }

  /**
   * Returns iff this model.unit can occupy the given type of terrain. For moving units, this is
   * true iff the cost of traveling the given terrain is less than its total movement cost For
   * buildings, this is true only for Ancient Ground
   */
  public abstract boolean canOccupy(Terrain t);

  /** Returns the tile this Unit is on */
  public Tile getLocation() {
    return location;
  }

  /** Returns whether this can move this turn. Non-movable things should always return false * */
  public abstract boolean canMove();

  /** Returns whether this can fight this turn. Non-fighting things should always return false * */
  public abstract boolean canFight();

  /**
   * Returns true if this model.unit can summon this turn. Returns false, should be overriden by
   * subclasses that can summon
   */
  public boolean canSummon() {
    return false;
  }

  /**
   * Returns true if this model.unit can cast this turn. Returns false, should be overriden by
   * subclasses that cast (Commanders)
   */
  public boolean canCast() {
    return false;
  }

  // STATS
  /** Returns the max health of this model.unit */
  public int getMaxHealth() {
    return (Integer) stats.getStat(StatType.MAX_HEALTH);
  }

  /** Returns the current health of this Unit */
  public int getHealth() {
    return health;
  }

  /** Returns the percent of health this currently has */
  public double getHealthPercent() {
    return (double) getHealth() / (double) getMaxHealth();
  }

  /**
   * Sets the current health of this model.unit (alters it by difference)
   *
   * @see #changeHealth(int, Unit) called with (desired - current, source)
   */
  protected final void setHealth(int newHealth, Unit source) {
    changeHealth(newHealth - health, source);
  }

  /**
   * Alters the current health of this model.unit. Maxes health at maxHealth. if health <= 0 because
   * of this call, calls died(). If a unit owned by the opposing player caused this change in health
   * and deltaHealth is negative, the owner gains research equal to the health lost, times
   * Commander.BONUS_DAMAGE_TO_RESEARCH_RATIO if this is a commander.
   *
   * @param deltaHealth - amount to change health by.
   * @param source - the model.unit causing this change in health
   */
  public final void changeHealth(int deltaHealth, Unit source) {
    health += deltaHealth;
    health = Math.min(health, getMaxHealth());
    if (source.owner != owner && deltaHealth < 0) {
      double bonusResearchRatio =
          (this instanceof Commander ? Commander.BONUS_DAMAGE_TO_RESEARCH_RATIO : 1);
      source.owner.getCommander().addResearch((int) (-deltaHealth * bonusResearchRatio));
    }
    if (health <= 0) died(source);
  }

  /** Returns true iff this is alive (health > 0) */
  public boolean isAlive() {
    return health > 0;
  }

  /**
   * Called when a change in health causes this to die. Removes it from its owner and tile, and
   * gives its killer research based on this' level.
   */
  private void died(Unit killer) {
    for (Modifier m : getGrantedModifiers()) {
      m.kill();
    }
    owner.removeUnit(this);
    location.removeOccupyingUnit();
    if (killer.owner != owner) {
      killer.owner.getCommander().addResearch((int) (level * Commander.LEVEL_TO_RESEARCH_RATIO));
    }
  }

  /**
   * Returns the full stats for this model.unit - returns by value, so altering the return won't do
   * anything to this model.unit
   */
  public Stats getStats() {
    return new Stats(stats, null);
  }

  /** Returns the min attack strength of this model.unit. 0 if this is not a combatant. */
  public int getMinAttack() {
    return (Integer) stats.getStat(StatType.MIN_ATTACK);
  }

  /** Returns the min attack of this unit, scaled by its current health percentage. */
  public final int getMinAttackScaled() {
    return (int) (getMinAttack() * getHealthPercent());
  }

  /** Returns the max attack strength of this model.unit. 0 if this is not a combatant. */
  public int getMaxAttack() {
    return (int) (Integer) stats.getStat(StatType.MAX_ATTACK);
  }

  /** Returns the max attack of this unit, scaled by its current health percentage. */
  public final int getMaxAttackScaled() {
    return (int) (getMaxAttack() * getHealthPercent());
  }

  /** Returns true if this is a ranged model.unit (range > 0) false if this is melee (range == 0) */
  public boolean isRanged() {
    return getAttackRange() > 0;
  }

  /** Returns the attack range of this model.unit. */
  public int getAttackRange() {
    return (Integer) stats.getStat(StatType.ATTACK_RANGE);
  }

  /** Returns the vision range of this model.unit. */
  public int getVisionRange() {
    return (Integer) stats.getStat(StatType.VISION_RANGE);
  }

  /** Returns the summon range of this model.unit. */
  public int getSummonRange() {
    return (Integer) stats.getStat(StatType.SUMMON_RANGE);
  }

  /** Returns the mana per turn this model.unit costs/generates */
  public int getManaPerTurn() {
    return (Integer) stats.getStat(StatType.MANA_PER_TURN);
  }

  /** Returns the actions per turn this model.unit costs/generates */
  public int getCommanderActionsPerTurn() {
    return (Integer) stats.getStat(StatType.COMMANDER_ACTIONS_PER_TURN);
  }

  // MODIFIERS
  /** Copies personal modifiers from the given unit - useful for using in newly summoned unit. */
  public void copyPersonalModifiersFrom(Unit source) {
    source.grantedModifiers.stream().filter(m -> m.unit == source).forEach(m -> m.clone(this));
  }

  /**
   * Returns the modifiers currently affecting this model.unit Pass-by-value, so editing the
   * returned set doesn't do anything
   */
  public LinkedList<Modifier> getModifiers() {
    return new LinkedList<>(modifiers);
  }

  /** Returns true if this unit has a modifier matching the given name, false otherwise. */
  public boolean hasModifierByName(Modifier modifier) {
    return getModifierByName(modifier.name) != null;
  }

  /**
   * Returns the first modifier modifying this with the given name, if any. Returns null otherwise
   */
  public Modifier getModifierByName(String name) {
    for (Modifier m : modifiers) {
      if (m.name.equals(name)) return m;
    }
    return null;
  }

  /**
   * Returns a list of modifiers modifying this with the given name, if any. Returns an empty list
   * otherwise
   */
  public List<Modifier> getModifiersByName(Modifier modifier) {
    return modifiers
        .stream()
        .filter(m -> m.name.equals(modifier.name))
        .collect(Collectors.toList());
  }

  /** Checks modifier m for applying to this model.unit */
  public abstract boolean modifierOk(Modifier m);

  /**
   * Adds a new modifier to this model.unit. Also updates stats with the new modifiers, from its
   * original base stats. Called by modifier during construction. Returns true if the modifier was
   * applied, false otw
   */
  public boolean addModifier(Modifier m) {
    // Check if modifier is allowed. If not, don't apply and return false.
    if (!modifierOk(m)) {
      return false;
    }
    // Handle stacking mode.
    Modifier clone = m.cloneInCollection(modifiers);
    switch (m.stacking) {
      case NONE_DO_NOT_APPLY:
        // No stacking - if there is a clone, can't apply this.
        if (clone != null) {
          return false;
        }
        modifiers.add(m);
        refreshStats();
        return true;
      case DURATION_MAX:
        // Max stacking - if no clone, apply.
        // Otherwise apply only if duration > other duration.
        if (clone == null || m.getRemainingTurns() > clone.getRemainingTurns()) {
          modifiers.add(m);
          refreshStats();
          return true;
        } else {
          return false;
        }
      case DURATION_ADD:
        // Add stacking 0 if no clone, apply.
        // Otherwise alter old modifier's turns and count that as applying this one.
        if (clone == null) {
          modifiers.add(m);
          refreshStats();
        } else {
          clone.changeRemainingTurns(m.getRemainingTurns());
        }
        return true;
      case STACKABLE:
        modifiers.add(m);
        refreshStats();
        return true;
      default:
        throw new RuntimeException("Unsupported stacking type " + m.stacking);
    }
  }

  /**
   * Removes the given modifier from this model.unit. Also updates stats with new modifier from its
   * original base stats. Called by modifier on death. Returns true if the modifier was applied,
   * false otw
   */
  public boolean removeModifier(Modifier m) {
    if (modifiers.contains(m)) {
      modifiers.remove(m);
      refreshStats();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the modifiers this is currently granting. Pass-by-value, so editing the returned set
   * doesn't do anything
   */
  public HashSet<Modifier> getGrantedModifiers() {
    return new HashSet<>(grantedModifiers);
  }

  /**
   * Adds the given modifier to the modifiers this is granting. Called by modifier on construciton
   */
  public void addGrantedModifier(Modifier m) {
    grantedModifiers.add(m);
  }

  /** Removes the given modifier from its designated model.unit. Called by modifier on death */
  public void removeGrantedModifier(Modifier m) {
    grantedModifiers.remove(m);
  }

  // FIGHTING
  /**
   * Processes a pre-counter-fight action (this was attacked) that may be caused by modifiers. Still
   * only called when the fight is valid, called after other.preFight(). Only called if this will be
   * able to counterAttack.
   */
  public abstract void preCounterFight(Combatant other);

  /**
   * Processes a post-counter-fight (this was attacked) action that may be caused by modifiers. Only
   * called when the fight is valid, called after other.postFight() Only called if this was able to
   * counterAttack and is still alive. damageDealt and damageTaken will be 0 if the respective unit
   * could not deal damage.
   */
  public abstract void postCounterFight(int damageDealt, Combatant other, int damageTaken);

  // DRAWING
  /** Returns the name of the file that represents this model.unit as an image */
  public String getImgFilename() {
    return imageFilename;
  }

  /**
   * Returns a simple string describing this type of model.unit Either Commander, Building, or
   * Combatant
   */
  public abstract String getIdentifierString();

  // UTIL
  /** Returns an Arraylist of the given units, sorted by name */
  public static ArrayList<Unit> sortedList(Collection<? extends Unit> units) {
    ArrayList<Unit> arr = new ArrayList<Unit>(units);
    Collections.sort(
        arr,
        new Comparator<Unit>() {
          public int compare(Unit o1, Unit o2) {
            return o1.name.compareTo(o2.name);
          }
        });
    return arr;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public String toStringShort() {
    return name + " at " + location.toStringShort();
  }

  @Override
  public String toStringLong() {
    return name + " at " + location.toStringShort() + "; " + health + "/" + getMaxHealth();
  }

  @Override
  public String toStringFull() {
    String s = name + " at " + location.toStringShort() + "; " + health + "/" + getMaxHealth();
    s += " Owned by " + owner.toStringShort();
    for (Stat st : getStats()) {
      s += st.toString() + " ";
    }
    return s;
  }
}
