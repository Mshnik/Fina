package unit;

import game.Player;
import board.Tile;

/** Represents a Building on the board, controllable by a player */
public abstract class Building extends Unit {

	/** Constructor for Building.
	 * Also adds this unit to the tile it is on as an occupant, and
	 * its owner as a unit that player owns,
	 * Subtracts manaCost from the owner, but throws a runtimeException if 
	 * the owner doesn't have enough mana.
	 * @param owner - the player owner of this unit
	 * @param manaCost - the cost of summoning this unit. Should be a positive number.
	 * @param tile - the tile this unit begins the game on. Also notifies the tile of this.
	 * @param stats - the base unmodified stats of this unit.
	 */
	public Building(Player owner, int manaCost, Tile tile, UnitStats stats) 
			throws RuntimeException, IllegalArgumentException {
		super(owner, manaCost, tile, stats);
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

}
