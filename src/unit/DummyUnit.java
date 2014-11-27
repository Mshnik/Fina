package unit;

import java.util.LinkedList;


import game.Player;
import board.Terrain;
import board.Tile;

/** A dummy unit class to test the functionality of abstract unit */
public class DummyUnit extends MovingUnit {

	public DummyUnit(Player owner, Tile startingTile) {
		super(owner, startingTile, new UnitStats(0, 0, 0, AttackType.NO_ATTACK, 0, 0, 0, 0, 0));
		this.refreshForTurn();
	}

	@Override
	public int getMovementCap() {
		return 6;
	}

	@Override
	public int getMovementCost(Terrain t) {
		if(t.equals(Terrain.MOUNTAIN)) return Integer.MAX_VALUE;
		return 1;
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
		this.refreshForTurn();
	}

	@Override
	protected void died() {
	}

	@Override
	public void preCounterFight(Combatant other) {
	}

	@Override
	public void postCounterFight(Combatant other) {
	}

	@Override
	public boolean canFight() {
		return false;
	}

}
