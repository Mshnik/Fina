package unit;

import game.AbstractPlayer;
import board.Board;
import board.Terrain;
import board.Tile;

/** A dummy unit class to test the functionality of abstract unit */
public class DummyUnit extends AbstractUnit {

	public DummyUnit(AbstractPlayer owner, Board b, Tile startingTile) {
		super(owner, b, startingTile);
	}

	@Override
	public int getConvertedMovement() {
		return 6;
	}

	@Override
	public int getMovementCost(Terrain t) {
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
	public void preFight(AbstractUnit other) {

	}

	@Override
	public void postFight(AbstractUnit other) {

	}
	
	public String getImgFilename(){
		return "chrono.gif";
	}

}
