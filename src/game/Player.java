package game;

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
	
	/** The Units this player controls */
	private HashSet<Unit> units;
	
	/** The commander belonging to this player */
	private Commander commander;
	
	/** The Tiles in the board this player has vision of */
	private HashSet<Tile> visionCloud;
	
	/** Constructor for Player class with just game.
	 * @param g
	 * @param c
	 */
	public Player(Game g){
		game = g;
		g.addPlayer(this);
		units = new HashSet<Unit>();
		visionCloud = new HashSet<Tile>();
	}
	
	/** Returns true if it is this player's turn, false if some other player */
	public boolean isMyTurn(){
		return game.getCurrentPlayer() == this;
	}
	
	/** Returns true if this is a human player, false otherwise */
	public boolean isHumanPlayer(){
		return this instanceof HumanPlayer;
	}
	
	/** Returns the current mana for this player */
	public int getMana(){
		return commander.getMana();
	}
	
	/** Returns the maximum mana for this player */
	public int getMaxMana(){
		return commander.getMaxMana();
	}
	
	/** Returns the current level (not exp) of this player */
	public int getLevel(){
		return commander.getLevel();
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
	}
	
	/** Removes the given unit from this player's units.
	 * If the given unit is this player's commander, sets commander to null.
	 */
	public void removeUnit(Unit u){
		units.remove(u);
		refreshVisionCloud();
		if(u instanceof Commander && (Commander)u == commander) commander = null;
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
	 * 		- calls refresh on each unit (no particular order) */
	protected final void turnStart(){
		//Refresh for turn and add manaPerTurn
		for(Unit u : units){
			u.refreshForTurn();
			commander.addMana(u.getManaPerTurn());
		}
		
		
		//If mana < 0, force player to choose units to sacrifice instead.
		if(commander.getMana() < 0){
			//TODO
		}
	}
	
	/** Called when it becomes this player's turn to do things. Passes control to player.
	 * Shouldn't be recursive, and should terminate when it finishes doing things. */
	protected abstract void turn();
	
	/** Called when this player's turn ends. Does end of turn processing */
	protected final void turnEnd(){
		
	}
}
