package model.unit.building;

import java.util.ArrayList;
import model.board.Tile;
import model.game.Player;
import model.unit.Building;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.stat.Stats;

/**
 * A building that can summon new units (but not buildings) like a commander.
 *
 * @author Mshnik
 */
public final class SummonerBuilding extends Building implements Summoner {

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
   * @param tile - the tile this model.unit begins the model.game on. Also notifies the tile of
   *     this.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   */
  public SummonerBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      Tile tile,
      Stats stats)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, tile, stats);
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
        owner, name, getImgFilename(), level, manaCost, location, getStats());
  }
}
