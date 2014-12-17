package unit;

import java.util.HashMap;
import java.util.Map;

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
		1000, 3000, 6000, 10000
	};
	
	/** The highest level commanders can achieve */
	public static final int MAX_LEVEL = RESEARCH_REQS.length + 1;
	
	/** The ratio of manaCost -> research for the owner of the killing unit */
	public static final double MANA_COST_TO_RESEARCH_RATIO = 0.4;
	
	/** The extra defense (applied first) against ranged units granted to commanders */
	public static final double RANGED_DEFENSE = 0.5;

	/** The current mana level of this commander. Varies over the course of the game. */
	private int mana;

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
	 * 							the attack, counterattack, and attackType
	 * 							Attributes are unused, because they are either unnecessary 
	 * 							or calculated elsewhere.
	 */
	public Commander(String name, Player owner, Tile startingTile, UnitStats stats)
			throws RuntimeException, IllegalArgumentException {
		super(owner,name, 0, startingTile, stats);
		level = 1;
		research = 0;
		setMana(START_MANA);
		setHealth(getMaxHealth(), this);
	}
	
	/** Throws a runtime exception - commanders are not clonable */
	@Override
	public Unit clone(Player owner, Tile t){
		throw new RuntimeException("Can't clone commander " + this);
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
	
	/** Returns Commander */
	@Override
	public String getIdentifierString(){
		return "Commander";
	}

	//HEALTH and MANA
	/** Returns the current mana of this commander */
	public int getMana(){
		return mana;
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
		if(research == getResearchRequirement())
			levelUp();
	}

	/** Called by Player class when this levels up.
	 * Can be overriden by subclass to cause affect when leveled up, 
	 * but this super method should be called first.
	 * resets research, recalculates manaPerTurn and health of commander, updates stats */
	private void levelUp(){
		research = 0;
		level++;
		addModifier(new UnitModifier(this, this, Integer.MAX_VALUE, 
				StatType.MANA_PER_TURN, UnitModifier.ModificationType.ADD, SCALE_MANA_PT));
		addModifier(new UnitModifier(this, this, Integer.MAX_VALUE, 
				StatType.MAX_HEALTH, UnitModifier.ModificationType.ADD, SCALE_HEALTH));
		setHealth(getHealth() + SCALE_HEALTH, this);
		owner.updateManaPerTurn();
		owner.game.getFrame().repaint();
	}
	
	//SUMMONING
	/** Returns the list of units this can summon. Can be overridden to add additional units.
	 * Base Return is FileUnits for this' level. */
	public Map<String, Combatant> getSummonables(){
		HashMap<String, Combatant> units = new HashMap<String, Combatant>();
		for(int i = 1; i <= level; i++){
			for(FileCombatant c : FileCombatant.getCombatantsForAge(i)){
				units.put(c.name, c);
			}
		}
		return units;
	}
	
	/** Returns the list of buildings this can build. Can be overriden to add additional buildings */
	public Map<String, Building> getBuildables(){
		HashMap<String, Building> units = new HashMap<String, Building>();
		for(Building b : Building.getBuildings()){
			units.put(b.name, b);
		}
		return units;

	}
}
