package game;

import java.awt.Color;
import java.util.HashSet;

import board.*;
import unit.*;

/** An instance is a player (not the commander piece).
 * Extended to be either human controlled or AI.
 * @author MPatashnik
 *
 */
public abstract class Player {

	/** The Game this player is playing in */
	public final Game game;

	/** The index of this player in the game, where player 1 is the first player */
	public final int index;

	/** The Units this player controls */
	private HashSet<Unit> units;

	/** The commander belonging to this player */
	private Commander commander;

	/** The Tiles in the board this player has vision of */
	private HashSet<Tile> visionCloud;

	/** The sum of all the mana per turn generation/costs this player owns */
	private int manaPerTurn;

	/** Constructor for Player class with just game.
	 * @param g
	 * @param c
	 */
	public Player(Game g, Color c){
		game = g;
		this.index = g.addPlayer(this, c);
		units = new HashSet<Unit>();
		visionCloud = new HashSet<Tile>();
	}

	/** A very simple toString that returns the player index of this player.
	 * Can be overriden in subclasses for more specific behavior
	 */
	@Override
	public String toString(){
		return "Player " + index;
	}

	/** Returns true if it is this player's turn, false if some other player */
	public boolean isMyTurn(){
		return game.getCurrentPlayer() == this;
	}

	/** Returns true if this is a human player, false otherwise */
	public boolean isHumanPlayer(){
		return this instanceof HumanPlayer;
	}

	//HEALTH AND MANA
	/** Returns the current health for this player (the health of the commander) */
	public int getHealth(){
		return commander.getHealth();
	}

	/** Returns the max health for this player (the max health of the commander */
	public int getMaxHealth(){
		return commander.getMaxHealth();
	}

	/** Returns the current mana for this player */
	public int getMana(){
		return commander.getMana();
	}

	/** Returns the manaPerTurn this player generates */
	public int getManaPerTurn(){
		return manaPerTurn;
	}

	/** Updates the manaPerTurn this player generates. Should be called at least at the start
	 * of every turn
	 */
	public void updateManaPerTurn(){
		manaPerTurn = 0;
		for(Unit u : units){
			manaPerTurn += u.getManaPerTurn();
		}
	}

	/** Returns the current level (not exp) of this player */
	public int getLevel(){
		return commander.getLevel();
	}

	/** Returns the current amount of research this commander has accrewed */
	public int getResearch(){
		return commander.getResearch();
	}

	/** Returns the amount of research necessary to get to the next level */
	public int getResearchRequirement(){
		return commander.getResearchRequirement();
	}

	/** Returns the amount of research still necessary to get to the next level */
	public int getResearchRemaining(){
		return commander.getResearchRequirement();
	}

	/** Adds the given amount of research to resarch, capping at the requirement.
	 * Input must be positive (you can only gain research)
	 */
	public void addResearch(int deltaResearch) throws IllegalArgumentException{
		commander.addResearch(deltaResearch);
	}

	//UNITS
	/** Returns the units belonging to this player.
	 * passed-by-value, so editing this hashSet won't do anything
	 */
	public HashSet<Unit> getUnits(){
		return new HashSet<Unit>(units);
	}

	/** The commander belonging to this player */
	public Commander getCommander(){
		return commander;
	}

	/** Adds the given unit to this player's units.
	 * Call whenever a unit is constructed.
	 * If commander is null and u is a commander, sets commander to u.
	 * @throws IllegalArgumentException - If u is a commander and commander isn't null.
	 */
	public void addUnit(Unit u) throws IllegalArgumentException {
		units.add(u);
		if(u instanceof Commander){
			if(commander == null) commander = (Commander)u;
			else throw new IllegalArgumentException("Can't set " + u + " to commander for " + this);
		}
		refreshVisionCloud();
		updateManaPerTurn();
	}

	/** Removes the given unit from this player's units.
	 * If the given unit is this player's commander, sets commander to null.
	 */
	public void removeUnit(Unit u){
		units.remove(u);
		if(u instanceof Commander && (Commander)u == commander) commander = null;
		refreshVisionCloud();
		updateManaPerTurn();
	}

	//VISION
	/** Return true iff this player's vision contains tile T */
	public boolean canSee(Tile t){
		return visionCloud.contains(t);
	}

	/** Return true iff the tile u occupies is in this Player's vision */
	public boolean canSee(Unit u){
		return canSee(u.getLocation());
	}

	/** Returns the tiles this player can see.
	 * Pass-by-value, so editing the returned hashset will do nothing
	 */
	public HashSet<Tile> getVisionCloud(){
		return new HashSet<Tile>(visionCloud);
	}

	/** Refreshes this player's vision cloud based on its units */
	public void refreshVisionCloud(){
		visionCloud.clear();
		for(Unit u : units){
			visionCloud.addAll(game.board.getRadialCloud(u.getLocation(), u.getVisionRange()));
		}
	}

	//TURN
	/** Called when it becomes this player's turn. Does start of turn processing. 
	 * 		- calls refresh on each unit (no particular order).
	 * Return true if this player can start their turn - commander is alive, false otherwise */
	protected final boolean turnStart(){
		try{
			//Refresh for turn
			for(Unit u : units){
				u.refreshForTurn();
			}
			//Add mana Perturn
			updateManaPerTurn();
			commander.addMana(manaPerTurn);

			//If mana < 0, force player to choose units to sacrifice instead.
			if(commander.getMana() < 0){
				//TODO
			}
			return true;
		} catch(NullPointerException e){
			return false;
		}
	}

	/** Called when it becomes this player's turn to do things. Passes control to player.
	 * Shouldn't be recursive, and should terminate when it finishes doing things. */
	protected abstract void turn();

	/** Called by the someone (the player / the game) when this player's turn should end. */
	public abstract void turnEnd();
}
