package model.unit;

import java.util.HashMap;
import java.util.LinkedList;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.building.*;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.StatModifier;
import model.unit.stat.StatType;
import model.unit.stat.Stats;


/** Represents a Building on the model.board, controllable by a player */
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

	/** Constructor for Building.
	 * Also adds this model.unit to the tile it is on as an occupant, and
	 * its owner as a model.unit that player owns,
	 * Subtracts manaCost from the owner, but throws a runtimeException if 
	 * the owner doesn't have enough mana.
	 * Throws an illegalArgumentException if a building is constructed on land other than
	 * AncientGround
	 * @param owner - the player owner of this model.unit
	 * @param name	- the name of this model.unit.
	 * @param level - the level of this model.unit - the age this belongs to
	 * @param manaCost - the cost of summoning this model.unit. Should be a positive number.
	 * @param tile - the tile this model.unit begins the model.game on. Also notifies the tile of this.
	 * @param stats - the base unmodified stats of this model.unit.
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
	public int getMinAttack(){
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
		if (m instanceof StatModifier){
			StatType s = ((StatModifier) m).modifiedStat;
			return ! s.isAttackStat() && ! s.isMovementStat();
		}
		if (m instanceof CustomModifier){
			return ((CustomModifier) m).appliesToBuildings;
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
}
