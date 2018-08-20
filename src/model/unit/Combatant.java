package model.unit;

import java.util.ArrayList;

import model.board.Tile;
import model.game.Player;
import model.unit.commander.Bhen;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.StatModifier;
import model.unit.stat.StatType;
import model.unit.stat.Stats;



/**
 * Represents a moving and fighting model.unit
 * 
 * @author MPatashnik
 *
 */
public abstract class Combatant extends MovingUnit {

	/** true iff this can still fight this turn. Has an impact on how to draw this */
	private boolean canFight;

	/** Constructor for Combatant.
	 * Also adds this model.unit to the tile it is on as an occupant, and
	 * its owner as a model.unit that player owns,
	 * Subtracts manaCost from the owner, but throws a runtimeException if 
	 * the owner doesn't have enough mana.
	 * @param owner - the player owner of this model.unit
	 * @param name	- the name of this model.unit
	 * @param level - the level of this model.unit - the age this belongs to
	 * @param manaCost - the cost of summoning this model.unit. Should be a positive number.
	 * @param startingTile - the tile this model.unit begins the model.game on. Also notifies the tile of this.
	 * @param stats - the base unmodified stats of this model.unit.
	 */
	public Combatant(Player owner, String name, int level, int manaCost, Tile startingTile, Stats stats) 
			throws RuntimeException, IllegalArgumentException {
		super(owner, name, level, manaCost, startingTile, stats);

		if((int) stats.getStat(StatType.ATTACK) <= 0)
			throw new IllegalArgumentException("Combatant " + this + " can't have non-positive attack.");
	}

	/** Call at the beginning of every turn.
	 *  Can be overridden in subclasses, but those classes should call the super
	 *  version before doing their own additions.
	 * 		- ticks down modifiers and re-calculates stats, if necessary.
	 * 		- refreshes canMove and canFight
	 */
	@Override
	public void refreshForTurn(){
		super.refreshForTurn();
		canFight = true;
	}
	
	/** Refreshes just attack. Can be done mid-turn if by effect */
	public void refreshAttack(){
		canFight = true;
	}
	
	/** Combatants are ok with any kind of modifier except summon range */
	@Override
	public boolean modifierOk(Modifier m){
		if(m instanceof StatModifier){
			return ((StatModifier) m).modifiedStat != StatType.SUMMON_RANGE;
		}
		if(m instanceof CustomModifier){
			return ((CustomModifier) m).appliesToCombatants;
		}
		return false;
	}
	
	//FIGHTING
	/** Returns iff this can fight this turn */
	public boolean canFight(){
		return canFight;
	}
	
	/** Returns true iff there is at least one enemy model.unit within range and sight */
	public boolean hasFightableTarget(){
		ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getAttackRange() + 1);
		for(Tile t : tiles){
			if(t.isOccupied()){
				Unit u = t.getOccupyingUnit();
				if(u.owner != owner && owner.canSee(u)) return true;
			}
		}
		return false;
	}

	/** Processes a pre-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid. */
	public abstract void preFight(Unit other);

	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid and this is still alive.
	 */
	public abstract void postFight(Unit other);

	/** Causes this model.unit to fight the given model.unit.
	 * With this as the attacker and other as the defender.
	 * This will cause the health of the other to change
	 * @throws RuntimeException if...
	 * 		- this is dead
	 * 		- this can't attack currently
	 * @throws IllegalArgumentException for invalid fight when...
	 * 		- other is dead
	 * 		- both units belong to the same player
	 * 		- other is out of the range of this 
	 * 		- this' owner can't see other
	 * @return true iff other is killed because of this action
	 **/
	public final boolean fight(Unit other) throws IllegalArgumentException, RuntimeException{
		if(! isAlive()) 
			throw new RuntimeException (this + " can't fight, it is dead.");
		if(! other.isAlive()) 
			throw new IllegalArgumentException(other + " can't fight, it is dead.");
		if(owner == other.owner) 
			throw new IllegalArgumentException(this + " can't fight " + other + ", they both belong to " + owner);
		if(! canFight)
			throw new RuntimeException(this + " can't fight again this turn");
		if(! owner.canSee(other))
			throw new IllegalArgumentException(owner + " can't see " + other);

		int room = location.manhattanDistance(other.getLocation()) - 1; //Account for melee = 0 range
		if(room > getAttackRange())
			throw new IllegalArgumentException(this + " can't fight " + other + ", it is too far away.");

		int damage = (int)(getAttack() * (1 - other.getPhysicalDefense()));
		
		//True if a counterAttack is happening, false otherwise.
		boolean counterAttack = other.isAlive() && other.owner.canSee(this) && room <= other.getAttackRange()
								&& damage < other.getHealth() && other instanceof Combatant;

		preFight(other);
		if(counterAttack) other.preCounterFight(this);

		//This attacks other
		other.changeHealth(- damage, this);

		//If other is still alive, can see the first model.unit, 
		//and this is within range, other counterattacks
		if(counterAttack){
			changeHealth(- (int)(other.getAttack() * (1 - getPhysicalDefense())), other);
			counterAttack = true;
		}

		//This can't attack this turn again
		canFight = false;

		//Calls postFight on units that are still alive, able to counterAttack
		if(isAlive()){
			postFight(other);
		}
		if(other.isAlive() && counterAttack) {
			other.postCounterFight(this);
		}
		
		boolean otherIsDead = ! other.isAlive();
		
		//Check for killing modifiers
		Modifier mod1 = getModifierByName(Bhen.ABILITY_NAMES[0][0]);
		if(mod1 != null){
			addMovement(((CustomModifier)mod1).val.intValue());
		}
		
		return otherIsDead;
	}
	
	
	/** Returns Combatant */
	@Override
	public String getIdentifierString(){
		return "Combatant";
	}

}
