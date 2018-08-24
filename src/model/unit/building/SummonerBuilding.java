package model.unit.building;

import java.util.ArrayList;
import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.stat.Stats;

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
   * @param tile - the tile this model.unit begins the model.game on. Also notifies the tile of
   *     this.
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
      Tile tile,
      Stats stats,
      int nonAncientGroundSummonRadius,
      int ancientGroundSummonRadius)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, tile, stats);
    this.nonAncientGroundSummonRadius = nonAncientGroundSummonRadius;
    this.ancientGroundSummonRadius = ancientGroundSummonRadius;
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
  public Unit clone(Player owner, Tile location) {
    return new SummonerBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        location,
        getStats(),
        nonAncientGroundSummonRadius,
        ancientGroundSummonRadius);
  }
}
