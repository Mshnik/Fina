package model.unit.building;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.modifier.PlayerModifier;
import model.unit.stat.Stats;

/**
 * An extension of building that grants the player (not a particular unit) a bonus.
 */
public final class PlayerModifierBuilding extends Building<List<PlayerModifier>> {

  /**
   * Effects this building grants if this isn't on ancient ground.
   */
  public final List<PlayerModifier> nonAncientGroundEffect;

  /**
   * Effects this building grants if this is on ancient ground.
   */
  public final List<PlayerModifier> ancientGroundEffect;

  /**
   * Constructor for Building. Also adds this model.unit to the tile it is on as an occupant, and
   * its owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana. Throws an illegalArgumentException if a
   * building is constructed on land other than AncientGround
   *
   * @param owner                  - the player owner of this model.unit
   * @param name                   - the name of this model.unit.
   * @param imageFilename          - the image to draw when drawing this unit.
   * @param level                  - the level of this model.unit - the age this belongs to
   * @param manaCost               - the cost of summoning this model.unit. Should be a positive number.
   * @param manaCostScaling        - the additional cost of summoning this model.unit for each copy beyond
   *                               the first. Should be non-negative.
   * @param validTerrain           - types of terrain this can be built on.
   * @param stats                  - the base unmodified stats of this model.unit. stats that remain used are
   * @param nonAncientGroundEffect - the effect this gives if this isn't on ancient ground.
   * @param ancientGroundEffect    - the effect this gives if this is on ancient ground.
   */
  PlayerModifierBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats,
      List<PlayerModifier> nonAncientGroundEffect,
      List<PlayerModifier> ancientGroundEffect)
      throws RuntimeException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, stats);
    this.nonAncientGroundEffect = nonAncientGroundEffect;
    this.ancientGroundEffect = ancientGroundEffect;
  }

  @Override
  public List<List<PlayerModifier>> getPossibleEffectsList() {
    LinkedList<List<PlayerModifier>> list = new LinkedList<>();
    list.add(nonAncientGroundEffect);
    list.add(ancientGroundEffect);
    return Collections.unmodifiableList(list);
  }

  @Override
  public List<PlayerModifier> getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundEffect
        : nonAncientGroundEffect;
  }

  @Override
  protected Unit createClone(Player owner, Tile cloneLocation) {
    return new PlayerModifierBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        getStats(),
        nonAncientGroundEffect,
        ancientGroundEffect);
  }

  /**
   * PlayerModifierBuilding can't summon.
   */
  @Override
  public boolean canSummon() {
    return false;
  }
}
