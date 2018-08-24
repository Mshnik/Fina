package model.unit.building;

import java.util.List;
import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.stat.Stats;

/** An extension of building with no additional effects. */
public final class NoEffectBuilding extends Building<Void> {

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
   * @param validTerrain - types of terrain this can be built on.
   * @param tile - the tile this model.unit begins the model.game on. Also notifies the tile of
   *     this.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   */
  NoEffectBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Tile tile,
      Stats stats)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, tile, stats);
  }

  @Override
  public Void getEffect() {
    return null;
  }

  @Override
  public Unit clone(Player owner, Tile location) {
    return new NoEffectBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        location,
        getStats());
  }
}
