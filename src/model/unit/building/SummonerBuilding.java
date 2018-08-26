package model.unit.building;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.stat.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A building that can summon new units (but not buildings) like a commander.
 *
 * @author Mshnik
 */
public final class SummonerBuilding extends Building<Integer> implements Summoner {

  /** Summon radius for this if this isn't on ancient ground. */
  private final int nonAncientGroundSummonRadius;

  /** Summon radius for this if this is on ancient ground. */
  private final int ancientGroundSummonRadius;

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
   * @param manaCost - the cost of summoning this model.unit. Should be a positive number. * @param
   * @param manaCostScaling - the additional cost of summoning this model.unit for each copy beyond
   *     the first. Should be a non-negative number.
   * @param validTerrain - types of terrain this can be built on.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   * @param nonAncientGroundSummonRadius - summon radius for this if this is on ancient ground.
   * @param ancientGroundSummonRadius - summon radius for this if this is on ancient ground.
   */
  SummonerBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats,
      int nonAncientGroundSummonRadius,
      int ancientGroundSummonRadius)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, stats);
    this.nonAncientGroundSummonRadius = nonAncientGroundSummonRadius;
    this.ancientGroundSummonRadius = ancientGroundSummonRadius;
  }

  @Override
  public List<Integer> getPossibleEffectsList() {
    LinkedList<Integer> list = new LinkedList<>();
    list.add(nonAncientGroundSummonRadius);
    list.add(ancientGroundSummonRadius);
    return Collections.unmodifiableList(list);
  }

  @Override
  public Integer getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundSummonRadius
        : nonAncientGroundSummonRadius;
  }

  /** Override summon range to be determined by effect instead of stat. */
  @Override
  public int getSummonRange() {
    return getEffect();
  }

  @Override
  public boolean hasSummonSpace() {
    ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getSummonRange());
    for (Tile t : tiles) {
      if (!t.isOccupied()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasBuildSpace() {
    // SummonerBuilding can only summon, not build.
    return false;
  }

  @Override
  public boolean canSummon() {
    return true;
  }

  @Override
  protected Unit createClone(Player owner) {
    return new SummonerBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        getStats(),
        nonAncientGroundSummonRadius,
        ancientGroundSummonRadius);
  }
}
