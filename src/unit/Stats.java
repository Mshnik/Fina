package unit;

import java.util.Collection;

/** Holder for stats for any kind of unit.
 * @author MPatashnik
 *
 * @param <T>
 */
public interface Stats {

	/** Returns a new instance of the same dymanic type as this
	 * with the given modifier list with this as a base.
	 */
	public Stats modifiedWith(Collection<UnitModifier> modifiers);
	
	/** Returns true if this is a base stats (unmodified), false otherwise */
	public boolean isBase();

	/** @return the base stats these stats are a modification of, null otherwise.
	 * Should be of the same dynamic type as this
	 */
	public Stats getBase();

	/** @return the maxHealth stat
	 */
	public int getMaxHealth();

	/** @return the manaPerTurn stat
	 */
	public int getManaPerTurn();

	/** @return the attack stat
	 */
	public int getAttack();

	/**
	 * @return the attackType
	 */
	public AttackType getAttackType();

	/**
	 * @return the counterattack
	 */
	public int getCounterattack();

	/**
	 * @return the physicalDefense
	 */
	public int getPhysicalDefense();

	/**
	 * @return the rangeDefense
	 */
	public int getRangeDefense();

	/**
	 * @return the magicDefense
	 */
	public int getMagicDefense();

	/**
	 * @return the range
	 */
	public int getRange();
	
}
