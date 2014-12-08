package unit;

import java.util.LinkedList;
import java.util.List;

import unit.combatant.FileCombatant;

import game.Player;
import board.Tile;

/** Represents a commander for a player. Each player should have one.
 * 
 * Commanders have mana and a level to maintain
 * @author MPatashnik
 *
 */
public abstract class Commander extends MovingUnit {

	/** Base level of health for lvl1 commanders */
	public static final int BASE_HEALTH = 1000;
	
	/** Base amount of max health gained per level */
	public static final int SCALE_HEALTH = 200;
	
	/** Base starting amount of mana for lvl1 commanders */
	public static final int START_MANA = 500;
	
	/** Base starting level of mana per turn for lvl1 commanders */
	public static final int BASE_MANA_PT = 500;
	
	/** Base amount of mana per turn gained per level */
	public static final int SCALE_MANA_PT = 250;
	
	/** The amount of research required to get to the next level for free.
	 * Index i = cost to get from level i+1 to i+2 (because levels are 1 indexed). */
	public static final int[] RESEARCH_REQS = {
		1000, 2500, 6000, 15000
	};
	
	/** The highest level commanders can achieve */
	public static final int MAX_LEVEL = RESEARCH_REQS.length + 1;
	
	/** The ratio of manaCost -> research for the owner of the killing unit */
	public static final double MANA_COST_TO_RESEARCH_RATIO = 0.3;
	
	/** The extra defense (applied first) against ranged units granted to commanders */
	public static final double RANGED_DEFENSE = 0.5;

	/** The max health for this commander. Increases with level.
	 * Specifically, this means the maxHealth stat attribute is unused. */
	protected int maxHealth;

	/** The current mana level of this commander. Varies over the course of the game. */
	private int mana;

	/** The current mana per turn generation of this commander. Increases with level. */
	protected int manaPerTurn;

	/** The current level of this player.
	 * Starts at 1, increases by 1 every time the player performs a levelup action.
	 * Capped at 5
	 */
	private int level;

	/** The amount of research towards the next level this commander has accrewed.
	 * Alwasy in the range [0, RESEARCH_REQS[level-1]] */
	private int research;

	/** Constructor for Commander.
	 * Also adds this unit to the tile it is on as an occupant, and
	 * its owner as a unit that player owns,
	 * Commanders have a manaCost of 0.
	 * @param owner - the player owner of this unit
	 * @param tile - the tile this unit begins the game on. Also notifies the tile of this.
	 * @param stats			- the stats of this commander.
	 * 							Notably, because of restrictions on commander,
	 * 							the maxHealth, attack, manaPerTurn, counterattack, and attackType
	 * 							Attributes are unused, because they are either unnecessary 
	 * 							or calculated elsewhere.
	 */
	public Commander(String name, Player owner, Tile startingTile, UnitStats stats)
			throws RuntimeException, IllegalArgumentException {
		super(owner,name, 0, startingTile, stats);
		level = 1;
		research = 0;
		recalculateScalingStats();
		setMana(START_MANA);
		setHealth(getMaxHealth(), this);
	}
	/** Commanders can always summon */
	@Override
	public boolean canSummon(){
		return true;
	}
	
	//RESTRICTIONS
	/** Restricted attack - has val 0. */
	@Override
	public int getAttack(){
		return 0;
	}

	/** Commanders can't fight */
	public boolean canFight(){
		return false;
	}

	/** Restricted attackType - has val NO_ATTACK. */
	@Override
	public AttackType getAttackType(){
		return AttackType.NO_ATTACK;
	}

	//HEALTH and MANA
	/** Returns the max health of this commander.
	 * Overrides Unit.getMaxHealth so level can affect maxHealth.
	 * This does mean the stats.maxHealth attribute is unused for commanders */
	@Override
	public int getMaxHealth(){
		return maxHealth;
	}

	/** Returns the current mana of this commander */
	public int getMana(){
		return mana;
	}

	/** Returns the mana per turn this commander generates
	 * Overrides Unit.getMaxHealth so level can affect maxHealth.
	 * This does mean the stats.maxHealth attribute is unused for commanders */
	@Override
	public int getManaPerTurn(){
		return manaPerTurn;
	}

	/** Sets to the given amount of mana. Input must be non-negative */
	protected void setMana(int newMana) throws IllegalArgumentException{
		if(newMana < 0)
			throw new IllegalArgumentException("Can't set mana to " + newMana + " - OOB");
		addMana(newMana - mana);
	}

	/** adds the given amount of mana
	 * If this would make mana negative, instead sets mana to 0,
	 * but returns the amount of mana below 0 this change would put.
	 * If the mana is still positive, returns the new value of mana.
	 */
	public int addMana(int deltaMana){
		mana = mana + deltaMana;
		int nMana = mana;
		if(mana < 0) mana = 0;
		return nMana;
	}

	/** Re-calculates maxHealth, maxMana, and manaPerTurn off of level and other factors.
	 * Should start with base formulae based off of static constants
	 * 	stat = BASE_STAT + SCALE_STAT * (lvl - 1)
	 * If max health or mana increase this way, increase health and mana by
	 * same amount */
	public abstract void recalculateScalingStats();
	
	/** Commanders get bonus defense against ranged units */
	@Override
	public double getDefenseAgainst(Unit attacker){
		return RANGED_DEFENSE + (1 - RANGED_DEFENSE) * super.getDefenseAgainst(attacker);
	}

	//LEVEL AND RESEARCH
	/** Returns the level of this player */
	public int getLevel(){
		return level;
	}

	/** Returns the current amount of research this commander has accrewed */
	public int getResearch(){
		return research;
	}

	/** Returns the amount of research necessary to get to the next level */
	public int getResearchRequirement(){
		if(level == MAX_LEVEL)
			return Integer.MAX_VALUE;
		return RESEARCH_REQS[level - 1];
	}

	/** Returns the amount of research still necessary to get to the next level */
	public int getResearchRemaining(){
		return research - getResearchRequirement();
	}

	/** Adds the given amount of research to resarch, capping at the requirement.
	 * Input must be positive (you can only gain research)
	 */
	public void addResearch(int deltaResearch) throws IllegalArgumentException{
		if(deltaResearch < 0)
			throw new IllegalArgumentException("Can't add negative amount of research to " + this);

		research = Math.min(research + deltaResearch, getResearchRequirement());
	}

	/** Called by Player class when this levels up.
	 * Can be overriden by subclass to cause affect when leveled up, 
	 * but this super method should be called first.
	 * resets research, recalculates manaPerTurn and health of commander */
	public void levelUp(){
		research = 0;
		recalculateScalingStats();
	}
	
	//SUMMONING
	/** Returns the list of units this can summon. Can be overridden to add additional units.
	 * Base Return is FileUnits for this' level */
	public List<Unit> getSummonables(){
		LinkedList<Unit> units = new LinkedList<Unit>(FileCombatant.getCombatantsForAge(level));
		return units;
	}
}
