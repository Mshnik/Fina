package unit.buildings;

import game.Player;
import board.Tile;
import unit.*;
import unit.StatModifier.ModificationType;

public class Temple extends Building {
	
	/** Buffs, by number of temples other than this (0 indexed) */
	private static final StatModifier[][] BUFFS = {
		//1 temple
		{ new StatModifier(Integer.MAX_VALUE, false, StatType.ATTACK, ModificationType.ADD, 50) },
		
		//2 temples
		{ new StatModifier(Integer.MAX_VALUE, false, StatType.MAGIC_DEFENSE, ModificationType.ADD, 0.05),
		  new StatModifier(Integer.MAX_VALUE, false, StatType.PHYSICAL_DEFENSE, ModificationType.ADD, 0.05)},
		
		//3 temples  
		{ new StatModifier(Integer.MAX_VALUE, false, StatType.VISION_RANGE, ModificationType.ADD, 2),
		  new StatModifier(Integer.MAX_VALUE, false, StatType.MOVEMENT_TOTAL, ModificationType.ADD, 2)}, 
		
		//4 temples
		{ new StatModifier(Integer.MAX_VALUE, false, StatType.ATTACK_RANGE, ModificationType.ADD, 1),
		  new StatModifier(Integer.MAX_VALUE, false, StatType.ATTACK_TYPE, ModificationType.SET, AttackType.TRUE)},
		
		//5 temples  
		{ new StatModifier(Integer.MAX_VALUE, false, StatType.ATTACK, ModificationType.MULTIPLY, 1.5),
		  new StatModifier(Integer.MAX_VALUE, false, StatType.PHYSICAL_DEFENSE, ModificationType.MULTIPLY, 1.5),
		  new StatModifier(Integer.MAX_VALUE, false, StatType.MAGIC_DEFENSE, ModificationType.MULTIPLY, 1.5)} 
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
	private static final Stats STATS = new Stats(
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
		for(Modifier m : getGrantedModifiers()){
			m.kill();
		}
		
		//Add new modifiers based on index to all units this owns
		for(Unit u : owner.getUnits()){
			for(StatModifier dummy : BUFFS[index]){
				new StatModifier(u, this, dummy);
			}
		}
	}

}
