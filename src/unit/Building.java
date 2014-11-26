package unit;

import game.AbstractPlayer;
import board.Board;
import board.Tile;

/** Represents a Building on the board, controllable by a player */
public abstract class Building extends Unit {

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(AbstractPlayer owner, Board b, Tile tile, UnitStats stats) */
	public Building(AbstractPlayer owner, Board b, Tile tile, UnitStats stats) throws IllegalArgumentException {
		super(owner, b, tile, stats);
		
		if(! stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Building " + this + " must have attackType NO_ATTACK");
	}

}
