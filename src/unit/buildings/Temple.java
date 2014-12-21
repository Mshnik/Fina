package unit.buildings;

import game.Player;
import board.Tile;
import unit.AttackType;
import unit.Building;
import unit.Stat;
import unit.StatType;
import unit.Unit;
import unit.UnitModifier;
import unit.UnitModifier.ModificationType;
import unit.UnitStats;

public class Temple extends Building {
	
	/** Buffs, by number of temples other than this (0 indexed) */
	private static final UnitModifier[][] BUFFS = {
		//1 temple
		{ new UnitModifier(Integer.MAX_VALUE, StatType.ATTACK, ModificationType.ADD, 50) },
		
		//2 temples
		{ new UnitModifier(Integer.MAX_VALUE, StatType.MAGIC_DEFENSE, ModificationType.ADD, 0.05),
		  new UnitModifier(Integer.MAX_VALUE, StatType.PHYSICAL_DEFENSE, ModificationType.ADD, 0.05)},
		
		//3 temples  
		{ new UnitModifier(Integer.MAX_VALUE, StatType.VISION_RANGE, ModificationType.ADD, 2),
		  new UnitModifier(Integer.MAX_VALUE, StatType.MOVEMENT_TOTAL, ModificationType.ADD, 2)}, 
		
		//4 temples
		{ new UnitModifier(Integer.MAX_VALUE, StatType.ATTACK_RANGE, ModificationType.ADD, 1),
		  new UnitModifier(Integer.MAX_VALUE, StatType.ATTACK_TYPE, ModificationType.SET, AttackType.TRUE)},
		
		//5 temples  
		{ new UnitModifier(Integer.MAX_VALUE, StatType.ATTACK, ModificationType.MULTIPLY, 1.5),
		  new UnitModifier(Integer.MAX_VALUE, StatType.PHYSICAL_DEFENSE, ModificationType.MULTIPLY, 1.5),
		  new UnitModifier(Integer.MAX_VALUE, StatType.MAGIC_DEFENSE, ModificationType.MULTIPLY, 1.5)} 
	};
	
	/** Maximum number of temples a player can own */
	public static final int MAX_TEMPLES = BUFFS.length;
	
	/** The in game name of a Temple */
	public static final String NAME = "Temple";
	
	/** Minimum level for building a Temple */
	public static final int LEVEL = 2;
	
	/** Mana cost of constructing a Temple */
	public static final int COST = 1500;
	
	/** Stats for maxHealth, defenses, range and visionRange of baracks */
	private static final UnitStats STATS = new UnitStats(
			new Stat(StatType.MAX_HEALTH, 1200),
			new Stat(StatType.MAGIC_DEFENSE, 0.1),
			new Stat(StatType.PHYSICAL_DEFENSE, 0.1)
	);
	
	
	public Temple(Player owner, Tile tile) throws RuntimeException, IllegalArgumentException {
		super(owner, NAME, LEVEL, COST, tile, STATS);
	}


	/** Creates a new Temple for the given owner, on the given location */
	@Override
	public Unit clone(Player owner, Tile location) {
		return new Temple(owner, location);
	}

	
	@Override
	public String getImgFilename() {
		return "temple.png";
	}
	
	/** Returns the index of this in its owners' temples */
	public int getIndex(){
		return owner.getTempleIndex(this);
	}
	
	/** Refreshes this on index i of its owner's temples */
	public void refreshForIndex(){
		int index = getIndex();
		//Remove old modifiers
		for(UnitModifier m : getGrantedModifiers()){
			m.kill();
		}
		
		//Add new modifiers based on index to all units this owns
		for(Unit u : owner.getUnits()){
			for(UnitModifier dummy : BUFFS[index]){
				new UnitModifier(u, this, dummy);
			}
		}
	}

}
