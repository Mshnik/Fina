package unit.dummy;

import game.Player;

import java.util.LinkedList;

import board.Terrain;
import board.Tile;
import unit.AttackType;
import unit.Combatant;
import unit.Commander;
import unit.UnitStats;

public class DummyCommander extends Commander {

	public static final UnitStats BASE_STATS = 
			new UnitStats(0, 0, 0, AttackType.NO_ATTACK, 5, 5, 5, 3);

	
	public DummyCommander(Player owner, Tile startingTile) {
		super("Dummy", owner, startingTile, BASE_STATS);
	}

	@Override
	public void recalculateScalingStats() {
		maxHealth = Commander.BASE_HEALTH + Commander.SCALE_HEALTH * getLevel();
		manaPerTurn = Commander.BASE_MANA_PT + Commander.SCALE_MANA_PT * getLevel();
	}

	@Override
	public int getMovementCap() {
		return 5;
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
		// TODO Auto-generated method stub
		return "chrono.gif";
	}

}
