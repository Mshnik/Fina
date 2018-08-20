package model.unit.building;

import model.board.Tile;
import model.game.Player;
import model.unit.*;
import model.unit.modifier.Modifier;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.StatModifier;
import model.unit.modifier.StatModifier.ModificationType;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

public final class Temple extends Building {
	
	/** Buffs, by number of temples other than this (0 indexed) */
	private static final ModifierBundle[] BUFFS = {
		//1 temple
		new ModifierBundle(
				new StatModifier("Temple Attack", Integer.MAX_VALUE, false, StatType.ATTACK, ModificationType.ADD, 50)
				),
		
		//2 temples
		new ModifierBundle(
		  new StatModifier("Temple Mag Def", Integer.MAX_VALUE, false, StatType.MAGIC_DEFENSE, ModificationType.ADD, 0.05),
		  new StatModifier("Temple Phys Def", Integer.MAX_VALUE, false, StatType.PHYSICAL_DEFENSE, ModificationType.ADD, 0.05)
		),
		
		//3 temples  
		new ModifierBundle(
		  new StatModifier("Temple Vision", Integer.MAX_VALUE, false, StatType.VISION_RANGE, ModificationType.ADD, 2),
		  new StatModifier("Temple Movement", Integer.MAX_VALUE, false, StatType.MOVEMENT_TOTAL, ModificationType.ADD, 2)
		), 
		
		//4 temples
		new ModifierBundle(
		  new StatModifier("Temple Range", Integer.MAX_VALUE, false, StatType.ATTACK_RANGE, ModificationType.ADD, 1),
		  new StatModifier("Temple Type", Integer.MAX_VALUE, false, StatType.ATTACK_TYPE, ModificationType.SET, AttackType.TRUE)
		),
		
		//5 temples  
		new ModifierBundle(
		  new StatModifier("Temple Ult1",Integer.MAX_VALUE, false, StatType.ATTACK, ModificationType.MULTIPLY, 1.5),
		  new StatModifier("Temple Ult2",Integer.MAX_VALUE, false, StatType.PHYSICAL_DEFENSE, ModificationType.MULTIPLY, 1.5),
		  new StatModifier("Temple Ult3",Integer.MAX_VALUE, false, StatType.MAGIC_DEFENSE, ModificationType.MULTIPLY, 1.5)
		)
	};
	
	/** Maximum number of temples a player can own */
	public static final int MAX_TEMPLES = BUFFS.length;
	
	/** The in model.game name of a Temple */
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
			BUFFS[index].clone(u, this);
		}
	}

}