package unit;

import game.AbstractPlayer;
import board.Tile;

/** Represents a moving and fighting unit
 * 
 * @author MPatashnik
 *
 */
public abstract class Combatant extends MovingUnit {

	/** true iff this can still fight this turn. Has an impact on how to draw this */
	private boolean canFight;

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(AbstractPlayer owner, Board b, Tile tile, UnitStats stats) */
	public Combatant(AbstractPlayer owner,Tile startingTile, UnitStats stats) {
		super(owner, startingTile, stats);

		if(stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Combatant " + this + " can't have attackType NO_ATTACK");
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
	
	//FIGHTING
	/** Returns iff this can fight this turn */
	public boolean canFight(){
		return canFight;
	}

	/** Processes a pre-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid. */
	public abstract void preFight(Unit other);

	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid and this is still alive.
	 */
	public abstract void postFight(Unit other);

	/** Causes this unit to fight the given unit.
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
	public final boolean fight(MovingUnit other) throws IllegalArgumentException, RuntimeException{
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
		if(room > getRange())
			throw new IllegalArgumentException(this + " can't fight " + other + ", it is too far away.");

		//True if a counterAttack is happening, false otherwise.
		boolean counterAttack = other.isAlive() && other.owner.canSee(this) && room <= other.getRange();

		preFight(other);
		if(counterAttack) other.preCounterFight(this);

		//This attacks other
		other.changeHealth(- (getAttack() - other.getDefense(getAttackType())));

		//If other is still alive, can see the first unit, 
		//and this is within range, other counterattacks
		if(counterAttack){
			changeHealth(- (other.getCounterattack() - getDefense(other.getAttackType())));
			counterAttack = true;
		}

		//This can't attack this turn again
		canFight = false;

		if(isAlive()) postFight(other);
		if(other.isAlive() && counterAttack) other.postCounterFight(this);
		return ! other.isAlive();
	}

}
