package unit.dummy;

import game.Player;

import java.util.LinkedList;

import board.Terrain;
import board.Tile;
import unit.Combatant;
import unit.Commander;
import unit.Stat;
import unit.StatType;
import unit.UnitStats;

public class DummyCommander extends Commander {

	/** Stats for maxHealth, defenses, range and visionRange of dummy commander */
	private static final UnitStats STATS = new UnitStats(
			new Stat(StatType.MAGIC_DEFENSE, 0.1),
			new Stat(StatType.PHYSICAL_DEFENSE, 0.25),
			new Stat(StatType.SUMMON_RANGE, 2),
			new Stat(StatType.VISION_RANGE, 3)
	);

	
	public DummyCommander(Player owner, Tile startingTile) {
		super("Dummy", owner, startingTile, STATS);
	}

	@Override
	public void recalculateScalingStats() {
		maxHealth = Commander.BASE_HEALTH + Commander.SCALE_HEALTH * getLevel();
		manaPerTurn = Commander.BASE_MANA_PT + Commander.SCALE_MANA_PT * getLevel();
		
	}

	@Override
	public void refreshForTurn(){
		super.refreshForTurn();
		setHealth(getHealth() + 50, this);
	}
	
	@Override
	public int getMovementCap() {
		return 5;
	}

	@Override
	public int getMovementCost(Terrain t) {
		switch(t){
		case ANCIENT_GROUND:	return 1;
		case GRASS:				return 1;
		case MOUNTAIN:			return 9999;
		case WOODS:				return 2;
		default:				return 9999;
		}
	}

	@Override
	public void preMove(LinkedList<Tile> path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postMove(LinkedList<Tile> path) {
		
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
