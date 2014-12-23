package unit;

import java.util.HashMap;
import java.util.LinkedList;

import game.Player;
import board.Terrain;
import board.Tile;
import unit.buildings.*;
import unit.modifier.Modifier;
import unit.modifier.StatModifier;
import unit.stat.StatType;
import unit.stat.Stats;

/** Represents a Building on the board, controllable by a player */
public abstract class Building extends Unit {

	/** Index of available buildings, from level -> available buildings
	 *		- Well
	 *		- Baracks
	 *		- Temple
	 */
	private static final HashMap<Integer, LinkedList<Building>> BUILDINGS;

	static{

		//All available buildings start here
		Building[] rawBuildings = {
				new Well(null, null), 
				new Baracks(null, null), 
				new Temple(null, null)
		};

		//Put by level
		BUILDINGS = new HashMap<Integer, LinkedList<Building>>();
		for(Building b : rawBuildings){
			if(BUILDINGS.containsKey(b.getLevel())) BUILDINGS.get(b.getLevel()).add(b);
			else{
				LinkedList<Building> e = new LinkedList<Building>();
				e.add(b);
				BUILDINGS.put(b.getLevel(), e);
			}
		}
	}

	/** Returns a copy of the building index - all available buildings for the given level */
	public static LinkedList<Building> getBuildings(int level){
		LinkedList<Building> buildings = BUILDINGS.get(level);
		if(buildings == null) return new LinkedList<Building>();
		else return new LinkedList<Building>(buildings);
	}

	/** Image root for Building images, inside of global image root */
	public static final String IMAGE_ROOT = "building/";

	/** Constructor for Building.
	 * Also adds this unit to the tile it is on as an occupant, and
	 * its owner as a unit that player owns,
	 * Subtracts manaCost from the owner, but throws a runtimeException if 
	 * the owner doesn't have enough mana.
	 * Throws an illegalArgumentException if a building is constructed on land other than
	 * AncientGround
	 * @param owner - the player owner of this unit
	 * @param name	- the name of this unit.
	 * @param level - the level of this unit - the age this belongs to
	 * @param manaCost - the cost of summoning this unit. Should be a positive number.
	 * @param tile - the tile this unit begins the game on. Also notifies the tile of this.
	 * @param stats - the base unmodified stats of this unit.
	 * 					stats that remain used are maxHealth, physicalDefense, 
	 * 					magic defense, range, and visionRange
	 */
	public Building(Player owner, String name, int level, int manaCost, Tile tile, Stats stats) 
			throws RuntimeException, IllegalArgumentException {
		super(owner, name, level, manaCost, tile, stats);
		if(tile != null && tile.terrain != Terrain.ANCIENT_GROUND){
			throw new IllegalArgumentException("Can't construct building on non Ancient Ground terrain");
		}
	}

	//RESTRICTIONS
	/** Restricted attack - has val 0. */
	@Override
	public int getAttack(){
		return 0;
	}

	/** Restricted movement - buildings can't move */
	@Override
	public boolean canMove(){
		return false;
	}

	/** Commanders can't fight */
	public boolean canFight(){
		return false;
	}

	/** Buildings can only occupy Ancient Ground */
	public boolean canOccupy(Terrain t){
		return t.equals(Terrain.ANCIENT_GROUND);
	}

	/** Modifiers can't add movement or attack */
	@Override
	public boolean modifierOk(Modifier m){
		if(m instanceof StatModifier){
			StatType s = ((StatModifier) m).modifiedStat;
			return s != StatType.ATTACK && s != StatType.ATTACK_RANGE && s != StatType.ATTACK_TYPE
					&& s != StatType.MOVEMENT_TOTAL && s != StatType.GRASS_COST && s != StatType.WOODS_COST
					&& s != StatType.MOUNTAIN_COST;
		}
		return false;
	}

	/** Returns Building */
	@Override
	public String getIdentifierString(){
		return "Building";
	}

	/** Buildings don't do anything before a fight */
	@Override
	public void preCounterFight(Combatant other){}

	/** Buildings don't do anything after a fight */
	@Override
	public void postCounterFight(Combatant other){}

	/** Restricted attackType - has val NO_ATTACK. */
	@Override
	public AttackType getAttackType(){
		return AttackType.NO_ATTACK;
	}

}
