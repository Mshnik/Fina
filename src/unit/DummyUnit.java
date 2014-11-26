package unit;

import java.util.LinkedList;

import game.AbstractPlayer;
import board.Board;
import board.Terrain;
import board.Tile;

/** A dummy unit class to test the functionality of abstract unit */
public class DummyUnit extends Combatant {

	public DummyUnit(AbstractPlayer owner, Board b, Tile startingTile) {
		super(owner, b, startingTile);
		refreshMoveAndAttack();
	}

	@Override
	public int getConvertedMovement() {
		return 6;
	}

	@Override
	public int getMovementCost(Terrain t) {
		if(t.equals(Terrain.MOUNTAIN)) return Integer.MAX_VALUE;
		return 1;
	}

	@Override
	public int getManaCost() {
		return 0;
	}

	@Override
	public int getMaxHealth() {
		return 10;
	}

	@Override
	public int getAttack() {
		return 0;
	}

	@Override
	public int getCounterAttack() {
		return 0;
	}

	@Override
	public AttackType getAttackType() {
		return null;
	}

	@Override
	public int getPhysicalDefense() {
		return 0;
	}

	@Override
	public int getRangeDefense() {
		return 0;
	}

	@Override
	public int getMagicDefense() {
		return 0;
	}

	@Override
	public int getRange() {
		return 0;
	}

	@Override
	public void preFight(Combatant other) {

	}

	@Override
	public void postFight(Combatant other) {

	}
	
	public String getImgFilename(){
		return "chrono.gif";
	}

	@Override
	public void preMove(LinkedList<Tile> path) {		
	}

	@Override
	/** Dummy unit can move forever! */
	public void postMove(LinkedList<Tile> path) {		
		refreshMoveAndAttack();
	}

}
