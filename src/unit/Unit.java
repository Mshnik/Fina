package unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import game.Player;
import board.Terrain;
import board.Tile;

/** Represents any thing that can be owned by a player.
 * Maintains its health and stats, and modifiers.
 * 
 * @author MPatashnik
 *
 */
public abstract class Unit{

	/** Text representing moving */
	public static final String MOVE = "Move";
	
	/** Text representing fighting */
	public static final String FIGHT = "Attack";
	
	/** Text representing summoning */
	public static final String SUMMON = "Summon";
	
	/** Text representing building (building summoning) */
	public static final String BUILD = "Build";
	
	/** The name of this unit */
	public final String name;
	
	/** The level of this unit - the age in which it was summoned.
	 * If less than the level of the owner, may be eligible for an upgrade
	 */
	private final int level;
	
	/** The mana spent to summon this unit */
	public final int manaCost;
	
	/** The player that owns this unit */
	public final Player owner;

	/** The tile this unit is on. Should not ever share a tile with
	 * another unit
	 */
	protected Tile location;

	/** The current health of this Unit. If 0 or negative, this is dead. Can't be above maxHealth */
	private int health;

	/** The current stats of this unit. These are updated whenever unitModifiers are added or removed.
	 * Should never be null, but may be empty-ish */
	Stats stats;
	
	/** The modifiers this is the source of */
	private LinkedList<UnitModifier> grantedModifiers;
	
	/** A set of modifiers that are currently affecting this unit */
	private LinkedList<UnitModifier> modifiers;

	/** Constructor for Unit.
	 * Also adds this unit to the tile it is on as an occupant, and
	 * its owner as a unit that player owns,
	 * Subtracts manaCost from the owner, but throws a runtimeException if 
	 * the owner doesn't have enough mana.
	 * Tile and owner can be null in a dummy (not on board) instance
	 * @param owner - the player owner of this unit
	 * @param name	- the name of this unit. Can be generic, multiple units can share a name
	 * @param level - the level of this unit - the age this belongs to
	 * @param manaCost - the cost of summoning this unit. Should be a positive number.
	 * @param tile - the tile this unit begins the game on. Also notifies the tile of this.
	 * @param stats - the base unmodified stats of this unit.
	 */
	public Unit(Player owner, String name, int level, int manaCost, Tile tile, Stats stats) 
			throws IllegalArgumentException, RuntimeException{
		if(manaCost < 0)
			throw new IllegalArgumentException("manaCosts should be provided as positive ints");
		if(manaCost > 0 && owner != null && owner.getMana() < manaCost)
			throw new RuntimeException(owner + " can't afford to summon unit with cost " + manaCost);
		this.owner = owner;
		this.name = name;
		this.manaCost = manaCost;
		this.stats = new Stats(stats, null);
		health = getMaxHealth();
		modifiers = new LinkedList<UnitModifier>();
		grantedModifiers = new LinkedList<UnitModifier>();
		
		if(tile != null){
			location = tile;
			location.addOccupyingUnit(this);
		}
		
		if(owner != null) {
			if(! (this instanceof Commander)) owner.getCommander().addMana(Math.min(0,-manaCost));
			owner.addUnit(this);
			if(owner.getLevel() < level)
				throw new RuntimeException(owner + " can't summon unit with higher level than it");
			this.level = level;
		} else{
			this.level = level;
		}
	}
	
	/** Returns a copy of this for the given player, on the given tile */
	public abstract Unit clone(Player owner, Tile location);
	
	/** Returns the level of this unit */
	public int getLevel(){
		return level;
	}
	
	/** Refreshes this' stats with the locally stored modifiers */
	public void refreshStats(){
		stats = stats.modifiedWith(modifiers);
	}
	
	/** Call at the beginning of every turn.
	 *  Can be overridden in subclasses, but those classes should call the super
	 *  version before doing their own additions.
	 * 		- ticks down modifiers and re-calculates stats, if necessary.
	 */
	public void refreshForTurn(){
		LinkedList<UnitModifier> deadModifiers = new LinkedList<UnitModifier>();
		for(UnitModifier m : modifiers){
			if(m.decRemainingTurns() || ! m.source.isAlive())
				deadModifiers.add(m);
		}
		if(! deadModifiers.isEmpty()){
			modifiers.removeAll(deadModifiers);
			refreshStats();
		}
	}
	
	/** Returns iff this unit can occupy the given type of terrain.
	 * For moving units, this is true iff the cost of traveling the given terrain
	 * is less than its total movement cost
	 * For buildings, this is true only for Ancient Ground
	 */
	public abstract boolean canOccupy(Terrain t);
	
	/** Returns the tile this Unit is on */
	public Tile getLocation(){
		return location;
	}	
	
	/** Returns whether this can move this turn. Non-movable things should always return false *
	 */
	public abstract boolean canMove();
	
	/** Returns whether this can fight this turn. Non-fighting things should always return false *
	 */
	public abstract boolean canFight();
	
	/** Returns true if this unit can summon this turn. Returns false, should be overriden by
	 * subclasses that can summon
	 */
	public boolean canSummon(){
		return false;
	}

	//STATS
	/** Returns the max health of this unit */
	public int getMaxHealth(){
		return (Integer)stats.getStat(StatType.MAX_HEALTH);
	}
	
	/** Returns the current health of this Unit */
	public int getHealth(){
		return health;
	}
	
	/** Returns the percent of health this currently has */
	public double getHealthPercent(){
		return (double)getHealth() / (double)getMaxHealth();
	}
	
	/** Sets the current health of this unit (alters it by difference)
	 * @see changeHealth(desired - current, source)
	 */
	protected void setHealth(int newHealth, Unit source){
		changeHealth(newHealth - health, source);
	}

	/** Alters the current health of this unit. Maxes health at maxHealth.
	 * if health <= 0 because of this call, calls died().
	 * @param deltaHealth - amount to change health by.
	 * @param source - the unit causing this change in health
	 */
	protected void changeHealth(int deltaHealth, Unit source){
		health += deltaHealth;
		health = Math.min(health, getMaxHealth());
		if(health <= 0) died(source);
	}
	
	
	/** Returns true iff this is alive (health > 0) */
	public boolean isAlive(){
		return health > 0;
	}
	
	/** Called when a change in health causes this to die.
	 * Removes it from its owner and tile, and gives its killer research
	 * Can be overriden in subclasses to add additional behavior,
	 * but this method should be called somewhere in that overriden method */
	protected void died(Unit killer){
		for(UnitModifier m : getGrantedModifiers()){
			m.kill();
		}
		owner.removeUnit(this);
		location.removeOccupyingUnit();
		killer.owner.getCommander().addResearch((int)(manaCost * Commander.MANA_COST_TO_RESEARCH_RATIO));
	}
	
	/** Returns the full stats for this unit - returns by value, so altering the
	 * return won't do anything to this unit */
	public Stats getStats(){
		return new Stats(stats, null);
	}
	
	/** Returns the attack strength of this unit. 0 if this is not a combatant. */
	public int getAttack(){
		return (Integer)stats.getStat(StatType.ATTACK);
	}

	/** Returns the attack type of this unit. NO_ATTACK if this is not a combatant. */
	public AttackType getAttackType(){
		return (AttackType)stats.getStat(StatType.ATTACK_TYPE);
	}

	/** Returns the physical defense of this unit */
	public double getPhysicalDefense(){
		return (Double)stats.getStat(StatType.PHYSICAL_DEFENSE);
	}

	/** Returns the magic defense of this unit */
	public double getMagicDefense(){
		return (Double)stats.getStat(StatType.MAGIC_DEFENSE);
	}


	/** Returns the Defense of this stats for the given attack type.
	 * Infinite defense (prevent all of it) for NO_ATTACK type damage, no defense for TRUE damage */
	public double getDefense(AttackType type){
		switch(type){
		case NO_ATTACK: return 1.0;
		case PHYSICAL: return getPhysicalDefense();
		case MAGIC: return getMagicDefense();
		case TRUE: return 0.0;
		default: return 0.0;
		}
	}
	
	/** Returns the defense for the given attacker. Can be overriden
	 * in subclasses if they have different defense values than the default (against
	 * the attacker's type
	 */
	public double getDefenseAgainst(Combatant attacker){
		return getDefense(attacker.getAttackType());
	}
	
	/** Returns true if this is a ranged unit (range > 0)
	 * false if this is melee (range == 0)
	 */
	public boolean isRanged(){
		return getAttackRange() > 0;
	}

	/** Returns the attack range of this unit. */
	public int getAttackRange(){
		return (Integer)stats.getStat(StatType.ATTACK_RANGE);
	}
	
	/** Returns the vision range of this unit. */
	public int getVisionRange(){
		return (Integer)stats.getStat(StatType.VISION_RANGE);
	}
	
	/** Returns the summon range of this unit. */
	public int getSummonRange(){
		return (Integer)stats.getStat(StatType.SUMMON_RANGE);
	}
	
	/** Returns the mana per turn this unit costs/generates */
	public int getManaPerTurn(){
		return (Integer)stats.getStat(StatType.MANA_PER_TURN);
	}

	/** Returns true if this is a generator (manaPerTurn > 0) */
	public boolean isGenerator(){
		return getManaPerTurn() > 0;
	}
	
	/** Returns true if this is a burden (manaPerTurn < 0) */
	public boolean isBurden(){
		return getManaPerTurn() < 0;
	}
	
	//MODIFIERS
	/** Returns the modifiers currently affecting this unit
	 * Pass-by-value, so editing the returned set doesn't do anything
	 */
	public LinkedList<UnitModifier> getModifiers(){
		return new LinkedList<UnitModifier>(modifiers);
	}
	
	/** Checks modifier m for applying to this unit */
	public abstract boolean modifierOk(UnitModifier m);
	
	/** Adds a new modifier to this unit. Also updates stats with the new modifiers,
	 * from its original base stats. Called by modifier during construction.
	 * Returns true if the modifier was applied, false otw */
	boolean addModifier(UnitModifier m){
		if(modifierOk(m)){
			modifiers.add(m);
			refreshStats();
			return true;
		} else{
			return false;
		}
	}
	
	/** Removes the given modifier from this unit. Also updates stats with new modifier
	 * from its original base stats. Called by modifier on death.
	 * Returns true if the modifier was applied, false otw */
	boolean removeModifier(UnitModifier m){
		if(modifierOk(m)){
			modifiers.remove(m);
			refreshStats();
			return true;
		} else{
			return false;
		}
	}
	
	/** Returns the modifiers this is currently granting.
	 * Pass-by-value, so editing the returned set doesn't do anything
	 */
	public HashSet<UnitModifier> getGrantedModifiers(){
		return new HashSet<UnitModifier>(grantedModifiers);
	}
	
	/** Adds the given modifier to the modifiers this is granting. Called by modifier on construciton */
	void addGrantedModifier(UnitModifier m){
		grantedModifiers.add(m);
	}
	
	/** Removes the given modifier from its designated unit. Called by modifier on death */
	void removeGrantedModifier(UnitModifier m){
		grantedModifiers.remove(m);
	}
	
	//FIGHTING
	/** Processes a pre-counter-fight action (this was attacked) 
	 * that may be caused by modifiers.
	 * Still only called when the fight is valid, called after other.preFight().
	 * Only called if this will be able to counterAttack. */
	public abstract void preCounterFight(Combatant other);
	
	/** Processes a post-counter-fight (this was attacked) 
	 * action that may be caused by modifiers.
	 * Only called when the fight is valid, called after other.postFight()
	 * Only called if this was able to counterAttack and is still alive.
	 */
	public abstract void postCounterFight(Combatant other);
	
	//DRAWING
	/** Returns the name of the file that represents this unit as an image */
	public abstract String getImgFilename();
	
	/** Returns a simple string describing this type of unit 
	 * Either Commander, Building, or Combatant
	 */
	public abstract String getIdentifierString();
	
	//UTIL
	/** Returns an Arraylist of the given units, sorted by name */
	public static ArrayList<Unit> sortedList(Collection<? extends Unit> units){
		ArrayList<Unit> arr = new ArrayList<Unit>(units);
		Collections.sort(arr, new Comparator<Unit>(){
			public int compare(Unit o1, Unit o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		return arr;
	}
}
