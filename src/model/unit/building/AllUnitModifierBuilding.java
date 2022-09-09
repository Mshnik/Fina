package model.unit.building;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.modifier.ModifierBundle;
import model.unit.stat.Stats;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A building that grants all units the player controls a modifier as long as this building is
 * alive.
 *
 * @author Mshnik
 */
public final class AllUnitModifierBuilding extends Building<ModifierBundle> {

  /**
   * The modifiers granted to all units the player controls if this isn't on ancient ground
   */
  private final ModifierBundle nonAncientGroundModifierBundle;

  /**
   * The modifiers granted to all units the player controls if this is on ancient ground
   */
  private final ModifierBundle ancientGroundModifierBundle;

  /**
   * Constructor for Building. Also adds this model.unit to the tile it is on as an occupant, and
   * its owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana. Throws an illegalArgumentException if a
   * building is constructed on land other than AncientGround
   *
   * @param owner                          - the player owner of this model.unit
   * @param name                           - the name of this model.unit.
   * @param imageFilename                  - the image to draw when drawing this unit.
   * @param level                          - the level of this model.unit - the age this belongs to
   * @param manaCost                       - the cost of summoning this model.unit. Should be a positive number.
   * @param manaCostScaling                - the additional cost of summoning this model.unit for each copy beyond
   *                                       the first. Should be a non-negative number.
   * @param validTerrain                   - types of terrain this can be built on.
   * @param stats                          - the base unmodified stats of this model.unit. stats that remain used are
   * @param nonAncientGroundModifierBundle - the bundle of modifiers to give to all units if this
   *                                       isn't on ancient ground.
   * @param ancientGroundModifierBundle    - the bundle of modifiers to give to all units if this is on
   *                                       ancient ground.
   */
  AllUnitModifierBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats,
      ModifierBundle nonAncientGroundModifierBundle,
      ModifierBundle ancientGroundModifierBundle)
      throws RuntimeException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, stats);
    this.nonAncientGroundModifierBundle = nonAncientGroundModifierBundle;
    this.ancientGroundModifierBundle = ancientGroundModifierBundle;
  }

  @Override
  public List<ModifierBundle> getPossibleEffectsList() {
    LinkedList<ModifierBundle> list = new LinkedList<>();
    list.add(nonAncientGroundModifierBundle);
    list.add(ancientGroundModifierBundle);
    return Collections.unmodifiableList(list);
  }

  @Override
  public ModifierBundle getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundModifierBundle
        : nonAncientGroundModifierBundle;
  }

  /**
   * Applies the modifiers in this building to the given unit.
   */
  public void applyModifiersTo(Unit u) {
    getEffect().clone(u, this);
  }

  @Override
  protected Unit createClone(Player owner, Tile cloneLocation) {
    return new AllUnitModifierBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        getStats(),
        new ModifierBundle(nonAncientGroundModifierBundle),
        new ModifierBundle(ancientGroundModifierBundle));
  }

  /**
   * AllUnitModifierBuildings can't summon.
   */
  @Override
  public boolean canSummon() {
    return false;
  }
}
