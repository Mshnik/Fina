package unit;

import game.AbstractPlayer;

import java.util.LinkedList;

import board.Board;
import board.Terrain;
import board.Tile;

/** Represents a non-fighting combatant that generates mana
 * for the player that owns it.
 * @author MPatashnik
 *
 */
public abstract class Pawn extends Combatant {

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(AbstractPlayer owner, Board b, Tile tile, UnitStats stats) */
	public Pawn(AbstractPlayer owner, Board b, Tile startingTile, UnitStats stats) {
		super(owner, b, startingTile, stats);
		
		if(! stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Pawn " + this + " must have attackType NO_ATTACK");
	}

}
