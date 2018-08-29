package model.unit.building;

import model.board.Terrain;
import model.game.Player;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.StatModifier;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Building on the model.board, controllable by a player. E is the effect type for this
 * building. May be Void for none.
 */
public abstract class Building<E> extends Unit {

  /** Types of terrain this building can be built on. */
  private final List<Terrain> validTerrain;

  /**
   * Constructor for Building. Also adds this model.unit to the tile it is on as an occupant, and
   * its owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana. Throws an illegalArgumentException if a
   * building is constructed on land other than AncientGround
   *
   * @param owner - the player owner of this model.unit
   * @param name - the name of this model.unit.
   * @param imageFilename - the image to draw when drawing this unit.
   * @param level - the level of this model.unit - the age this belongs to
   * @param manaCost - the cost of summoning this model.unit. Should be a positive number.
   * @param manaCostScaling - the additional cost of summoning this model.unit for each copy beyond
   *     the first. Should be non-negative.
   * @param validTerrain - types of terrain this building can be built on.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   */
  public Building(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, stats);
    this.validTerrain = Collections.unmodifiableList(validTerrain);
  }

  /**
   * Return a list of possible effects this building can give. Used to draw info in menu. In game
   * use getEffect() below to get actually provided effect.
   */
  public abstract List<E> getPossibleEffectsList();

  /**
   * Return the effect this building gives. May vary based on the tile this is built on or other
   * buildings its player owns.
   */
  public abstract E getEffect();

  /** Returns the types of terrain this can be built on. */
  public List<Terrain> getValidTerrain() {
    return validTerrain;
  }

  // RESTRICTIONS
  /** Restricted attack - has val 0. */
  @Override
  public int getMinAttack() {
    return 0;
  }

  @Override
  public int getMaxAttack() {
    return 0;
  }

  @Override
  public int getMinAttackRange() {
    return 0;
  }

  @Override
  public int getMaxAttackRange() {
    return 0;
  }

  /** Restricted movement - buildings can't move */
  @Override
  public boolean canMove() {
    return false;
  }

  /** Buildings can't fight */
  public boolean canFight() {
    return false;
  }

  /** Buildings can occupy any terrain in their valid terrain. */
  public boolean canOccupy(Terrain t) {
    return validTerrain.contains(t);
  }

  /** All modifiers are visible. */
  public List<Modifier> getVisibleModifiers() {
    return getModifiers();
  }

  /** Modifiers can't add movement or attack */
  @Override
  public boolean modifierOk(Modifier m) {
    if (m instanceof StatModifier) {
      StatType s = ((StatModifier) m).modifiedStat;
      return !s.isAttackStat() && !s.isMovementStat();
    }
    if (m instanceof CustomModifier) {
      return ((CustomModifier) m).appliesToBuildings;
    }
    return false;
  }

  /** Returns Building */
  @Override
  public String getIdentifierString() {
    return "Building";
  }

  /** Buildings don't do anything before a fight */
  @Override
  public void preCounterFight(Combatant other) {}

  /** Buildings don't do anything after a fight */
  @Override
  public void postCounterFight(int damageDealt, Combatant other, int damageTaken) {}
}
