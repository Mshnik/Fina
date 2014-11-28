package unit;

import game.Player;

import board.Tile;

/** Represents a non-fighting combatant that generates mana
 * for the player that owns it.
 * @author MPatashnik
 *
 */
public abstract class Pawn extends MovingUnit {

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(Player owner, Tile tile, UnitStats stats) */
	public Pawn(Player owner, Tile startingTile, UnitStats stats) {
		super(owner, startingTile, stats);
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
