package unit;

import game.Const;
import game.Player;
import board.Tile;

/** Represents a commander for a player. Each player should have one.
 * 
 * Commanders have mana and a level to maintain
 * @author MPatashnik
 *
 */
public abstract class Commander extends MovingUnit {

	/** The name of this commander */
	public final String name;

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
		super(owner, 0, startingTile, stats);
		this.name = name;
		level = 1;
		research = 0;
		recalculateScalingStats();
		setMana(Const.START_MANA);
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
		if(level == Const.MAX_LEVEL)
			return Integer.MAX_VALUE;
		return Const.RESEARCH_REQS[level - 1];
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
}
