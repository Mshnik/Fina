package model.unit.ability;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import model.board.Board;
import model.board.MPoint;
import model.board.Tile;
import model.game.Stringable;
import model.unit.Commander;
import model.unit.Unit;

/** Parent class of abilities useable by Commanders. */
public abstract class Ability implements Stringable{

	/** The commander that owns this ability */
	public final Commander caster;

	/** The name of this ability */
	public final String name;

	/** The cost of casting this Ability */
	public final int manaCost;

	/** True iff this ability can be cast multiple times in a single turn */
	public final boolean canBeCastMultipleTimes;

	/** True iff modifiers should be applied to allied units */
	public final boolean appliesToAllied;

	/** True iff modifiers should be applied to enemy units */
	public final boolean appliesToFoe;

	/** The effect range of this Ability, as a collection of locations
	 * relative to its origin location
	 */
	private final Set<MPoint> effectCloud;

	/** Distance from the commander this can be cast. 0 for requiring commander as origin */
	public final int castDist;

	/** Ability Constructor
	 * @param name				- the Name of this ability
	 * @param manaCost			- the mana cost of using this ability. 0 if passive
	 * @param caster			- the Commander who owns this ability
	 * @param castDist			- the distance from the commander this ability can be cast
	 * @param multiCast			- true iff this can be used multiple times in a turn
	 * @param appliesToAllied	- true iff this ability can affect allied units
	 * @param appliesToFoe		- true iff this ability can affect non-allied units
	 * @param cloud				- the cloud of points (around the target as (0,0)) that it affects.
	 * 								If this is null, has infinite (whole model.board) range
	 */
	public Ability(String name, int manaCost, Commander caster, int castDist, boolean multiCast, 
			boolean appliesToAllied, boolean appliesToFoe, Set<MPoint> cloud){
		this.name = name;
		this.appliesToAllied = appliesToAllied;
		this.appliesToFoe = appliesToFoe;
		if(cloud != null)
			this.effectCloud = new HashSet<MPoint>(cloud);
		else
			this.effectCloud = null;
		this.caster = caster;
		if(manaCost > 0){
			this.manaCost = manaCost;
			this.castDist = castDist;
			this.canBeCastMultipleTimes = multiCast;
		} else{
			//Passive ability. Must be cast on commander, can be cast multiple times in turn
			//Has 0 mana cost
			this.manaCost = 0;
			this.castDist = 0;
			this.canBeCastMultipleTimes = true;
		}
	}

	/** Returns true if this is a passive ability (manaCost == 0) */
	public boolean isPassive(){
		return manaCost == 0;
	}

	/** Returns a translation of the effectCloud, centered at the given tile's location.
	 * If effectCloud is empty, treats it as infinite range, returns all tiles in model.board */
	public ArrayList<Tile> getEffectCloud(Tile t){
		ArrayList<Tile> cloud = new ArrayList<Tile>();
		Board b = t.board;
		MPoint origin = t.getPoint();

		if(effectCloud == null){
			for(Tile t2 : b){
				cloud.add(t2);
			}
			return cloud;
		}
		else{
			for(MPoint p : effectCloud){
				try{
					cloud.add(b.getTileAt(origin.add(p)));
				}catch(IllegalArgumentException e){} //Do nothing - OOB
			}
			Collections.sort(cloud);

			return cloud;
		}
	}
	
	/** Returns the size of the generic (unplaced) effectCloud. Returns Integer.MAX_VALUE if
	 * it is null (infinite)
	 */
	public int getEffectCloudSize(){
		if(effectCloud == null)
			return Integer.MAX_VALUE;
		return effectCloud.size();
	}

	/**
	 * Casts this ability. Returns true if this call is ok and cast happens, throws exception otherwise.
	 * If this is passive, always cast with the commander's location as its location
	 */
	public boolean cast(Tile location) throws RuntimeException{
		//Check mana
		if(caster.getMana() < manaCost)
			throw new RuntimeException("Can't Cast " + this + "; OOM");

		//Check location
		if(location.manhattanDistance(caster.getLocation()) > castDist)
			throw new RuntimeException("Can't Cast " + this + " at " + location + "; too far away from commander");

		//Check multicast
		if(! canBeCastMultipleTimes && caster.getAbilitiesCastThisTurn().contains(this))
			throw new RuntimeException("Can't cast " + this + " twice in one turn");

		//Ok. Cast
		caster.addMana(- manaCost);
		ArrayList<Tile> cloud = getEffectCloud(location);
		for(Tile t : cloud){
			if(! t.isOccupied()) continue;

			Unit u = t.getOccupyingUnit();
			if(u.owner == caster.owner && appliesToAllied){
				affect(u);
			}
			if(u.owner != caster.owner && appliesToFoe){
				affect(u);
			}
		}
		return true;
	}

	/** Causes the effect of this Ability to take effect on the given model.unit. 
	 * Assume validation has already been done */
	protected abstract void affect(Unit u);
	
	/** Returns true iff this is affectig from the given model.unit. If this is a one off ability this can be unsupported */
	public abstract boolean isAffecting(Unit u);
	
	/** Removes from the given model.unit. If this is a one off ability this can be unsupported */
	public abstract void remove(Unit u);
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public String toStringShort(){
		return name + ":(" + manaCost + ")";
	}
}
