package model.unit.building;

import model.board.Direction;
import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.stat.Stats;
import model.util.Cloud;
import model.util.MPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An extension of building with that fans itself orthogonally to a certain radius.
 */
public final class FanOrthogonallyBuilding extends Building<Integer> {

  /**
   * How far this building can fan on non-ancient ground.
   */
  private final int nonAncientGroundRadius;

  /**
   * How far this building can fan on ancient ground.
   */
  private final int ancientGroundRadius;

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
   * @param nonAncientGroundRadius - The maximum radius of fan, in each direction, when not built on ancient ground.
   * @param ancientGroundRadius    - The maximum radius of fan, in each direction, when built on ancient ground.
   */
  FanOrthogonallyBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats,
      int nonAncientGroundRadius,
      int ancientGroundRadius)
      throws RuntimeException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, stats);
    this.nonAncientGroundRadius = nonAncientGroundRadius;
    this.ancientGroundRadius = ancientGroundRadius;
  }

  @Override
  public List<Integer> getPossibleEffectsList() {
    LinkedList<Integer> list = new LinkedList<>();
    list.add(nonAncientGroundRadius);
    list.add(ancientGroundRadius);
    return Collections.unmodifiableList(list);
  }

  @Override
  public Integer getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundRadius
        : nonAncientGroundRadius;
  }

  @Override
  protected Unit createClone(Player owner, Tile cloneLocation) {
    return new FanOrthogonallyBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        getStats(),
        nonAncientGroundRadius,
        ancientGroundRadius);
  }

  /**
   * NoEffectBuilding can't summon.
   */
  @Override
  public boolean canSummon() {
    return false;
  }

  @Override
  public Set<Tile> buildFanOut() {
    if (location == null) {
      return Collections.emptySet();
    }

    Set<Tile> fanSet = new HashSet<>();
    for (Direction direction : Direction.values()) {
      maybeFan(fanSet, location.getPoint(), direction);
    }
    return fanSet;
  }

  /**
   * Maybe fans out along direction from locationPoint. Fans as far as possible to an endpoint of the same building
   * type, so long as there are no invalid, unseen, or blocked tiles in the way.
   */
  private void maybeFan(Set<Tile> fanSet, MPoint locationPoint, Direction direction) {
    List<Tile> tiles = new ArrayList<>();
    for (int i = 1; i <= getEffect(); i++) {
      MPoint mPoint = locationPoint.add(direction.toPoint().times(i));
      if (location.board.isOnBoard(mPoint)) {
        tiles.add(location.board.getTileAt(mPoint));
      }
    }

    int maxIndex = 0;
    for (int i = 1; i < tiles.size(); i++) {
      Tile t = tiles.get(i);
      if (!canOccupy(t.terrain) || !owner.canSee(t)) {
        break;
      }

      if (t.isOccupied()) {
        if (t.getOccupyingUnit().owner != owner) {
          break;
        }

        if (t.getOccupyingUnit().name.equals(name)) {
          maxIndex = i;
        } else {
          break;
        }
      }
    }

    if (maxIndex > 0) {
      fanSet.addAll(tiles.subList(0, maxIndex));
    }
  }
}
