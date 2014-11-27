package unit.dummy;

import game.Player;

import java.util.LinkedList;

import board.Terrain;
import board.Tile;
import unit.AttackType;
import unit.Combatant;
import unit.Pawn;
import unit.UnitStats;

public class DummyPawn extends Pawn {

	public static final UnitStats BASE_STATS = 
			new UnitStats(10, 2, 0, AttackType.NO_ATTACK, 0, 3, 3, 3, 3, 2);
	
	public DummyPawn(Player owner, Tile startingTile) {
		super(owner, startingTile, BASE_STATS);
	}

	@Override
	public int getMovementCap() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public int getMovementCost(Terrain t) {
		return 1;
	}

	@Override
	public void preMove(LinkedList<Tile> path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postMove(LinkedList<Tile> path) {
		this.refreshForTurn();
	}

	@Override
	public void preCounterFight(Combatant other) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCounterFight(Combatant other) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getImgFilename() {
		return "link.png";
	}

}
