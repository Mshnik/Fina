package model.board;

import controller.selector.CastSelector;
import controller.selector.PathSelector;
import controller.selector.SummonSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import model.game.Player;
import model.game.Stringable;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.ability.SpacialShift;
import model.unit.commander.Commander;
import model.util.MPoint;

/**
 * A Board represents the whole model.board state for the model.game as a matrix of tiles and other
 * information. <br>
 * <br>
 *
 * @author MPatashnik
 */
public final class Board implements Iterable<Tile>, Stringable {

  /**
   * Distance value given to the starting location of the unit when using {@link
   * #getMovementCloudWholeBoard(MovingUnit, Tile)}.
   */
  private static final int WHOLE_BOARD_CLOUD_STARTING_DISTANCE = 1000;

  /** The csv file this board was read from. */
  public final String filepath;

  /** The tiles that make up this model.board. Must be rectangular (non-jagged) */
  private final Tile[][] tiles;

  /**
   * The locations where commanders start. Use up to as many as needed. (May be longer than
   * necessary.
   */
  private final List<MPoint> commanderStartLocations;

  /**
   * The most recent pathComputationId. Set to a new value whenever a new movement cloud is
   * computed.
   */
  private int pathComputationId;

  /**
   * Construct a simple model.board of just terrain Throws IllegalArgumentException if input array
   * is jagged.
   */
  public Board(
      String filepath,
      Terrain[][] terrain,
      String[][] additionalInfo,
      List<MPoint> commanderStartLocations)
      throws IllegalArgumentException {
    this.filepath = filepath;
    tiles = new Tile[terrain.length][terrain[0].length];
    for (int i = 0; i < terrain.length; i++) {

      if (terrain[i].length != terrain[0].length)
        throw new IllegalArgumentException(
            "Jagged Array passed into model.board constructor " + Arrays.deepToString(terrain));

      for (int j = 0; j < terrain[i].length; j++) {
        tiles[i][j] =
            new Tile(
                this,
                i,
                j,
                terrain[i][j],
                terrain[i][j] == Terrain.SEA ? Integer.parseInt(additionalInfo[i][j]) : -1);
      }
    }
    pathComputationId = 0;
    this.commanderStartLocations = Collections.unmodifiableList(commanderStartLocations);
  }

  /** Returns the height (# rows) of this Board */
  public int getHeight() {
    return tiles.length;
  }

  /** Returns the width (# columns) of this Board */
  public int getWidth() {
    return tiles[0].length;
  }

  /** Returns true iff the given r,c is on the board. */
  public boolean isOnBoard(int r, int c) {
    return r >= 0 && r < tiles.length && c >= 0 && c < tiles[r].length;
  }

  /** Returns true iff the given point is on the board. */
  public boolean isOnBoard(MPoint p) {
    return p.row >= 0 && p.row < tiles.length && p.col >= 0 && p.col < tiles[p.row].length;
  }

  /** Returns the tile at the given index, throws IllegalArgumentException */
  public Tile getTileAt(int r, int c) throws IllegalArgumentException {
    if (r < 0 || r >= tiles.length || c < 0 || c >= tiles[r].length)
      throw new IllegalArgumentException(
          "Can't get tile from " + this + " at index (" + r + "," + c + ")");

    return tiles[r][c];
  }

  /** Returns the tile at the given location. Loc expected in (col, row). */
  public Tile getTileAt(MPoint loc) {
    return getTileAt(loc.row, loc.col);
  }

  /**
   * Return the tile in the given direction from this tile. If oob, returns null or if direction
   * invalid.
   */
  private Tile getTileInDirection(Tile t, Direction... directions) {
    int r = t.row;
    int c = t.col;
    for (Direction d : directions) {
      r += d.dRow();
      c += d.dCol();
    }
    if (isOnBoard(r, c)) {
      return getTileAt(r, c);
    } else {
      return null;
    }
  }

  /**
   * Returns an array of neighbors of the given tile, null for oob spaces. If includeDiagonal is
   * false, Returns in order [left, up, right, down]. If true, returns [left, left-up, up, right-up,
   * right, down-right, down, down-left].
   */
  private Tile[] getTileNeighbors(Tile t, boolean includeDiagonal) {
    if (includeDiagonal) {
      return new Tile[] {
        getTileInDirection(t, Direction.LEFT),
        getTileInDirection(t, Direction.UP, Direction.LEFT),
        getTileInDirection(t, Direction.UP),
        getTileInDirection(t, Direction.UP, Direction.RIGHT),
        getTileInDirection(t, Direction.RIGHT),
        getTileInDirection(t, Direction.DOWN, Direction.RIGHT),
        getTileInDirection(t, Direction.DOWN),
        getTileInDirection(t, Direction.LEFT, Direction.DOWN),
      };
    } else {
      return new Tile[] {
        getTileInDirection(t, Direction.LEFT),
        getTileInDirection(t, Direction.UP),
        getTileInDirection(t, Direction.RIGHT),
        getTileInDirection(t, Direction.DOWN)
      };
    }
  }

  /** Returns a set of tiles that have the given terrain type. */
  public Set<Tile> getTilesWithTerrainType(Terrain terrain) {
    return stream().filter(t -> t.terrain == terrain).collect(Collectors.toSet());
  }

  /** Returns the maximum number of players this board can support. */
  public int getMaxPlayers() {
    return commanderStartLocations.size();
  }

  /** Returns true if the given location is a commander starting location. */
  public boolean isCommanderStartLocation(int row, int col) {
    return commanderStartLocations.contains(new MPoint(row, col));
  }

  /**
   * Returns the commander start location for the given player. Throws an exception if this board
   * doesn't support the given player's index.
   */
  public Tile getCommanderStartLocation(Player player) {
    return getTileAt(commanderStartLocations.get(player.index - 1));
  }

  /** Returns a set of tiles that are a contiguous mountain range. Diagonals do not count. */
  public Set<Tile> getContiguousMountainRange(Tile center) {
    if (center.terrain != Terrain.MOUNTAIN) {
      throw new RuntimeException("Must start on mountain to get range");
    }
    if (center.mountainRange != null) {
      return center.mountainRange;
    }

    Set<Tile> set = new HashSet<>();
    LinkedList<Tile> exploreQueue = new LinkedList<>();
    exploreQueue.add(center);

    while (!exploreQueue.isEmpty()) {
      Tile t = exploreQueue.poll();
      set.add(t);
      for (Direction d : Direction.values()) {
        Tile neighbor = getTileInDirection(t, d);
        if (neighbor != null && neighbor.terrain == Terrain.MOUNTAIN && !set.contains(neighbor)) {
          exploreQueue.add(neighbor);
        }
      }
    }
    set = Collections.unmodifiableSet(set);
    // Store references to this computation for later.
    for (Tile t : set) {
      t.mountainRange = set;
    }
    return set;
  }

  /**
   * Return a set of tiles of radius radius centered at the given tile center. A radius of 0 will
   * return a set containing only center. Doesn't check terrain or current occupants at all.
   */
  public ArrayList<Tile> getRadialCloud(Tile center, int radius) {
    ArrayList<Tile> tiles = new ArrayList<Tile>();
    for (MPoint p : center.getPoint().radialCloud(radius).getPoints()) {
      try {
        tiles.add(getTileAt(p));
      } catch (IllegalArgumentException e) {
      } // OOB Tile - do nothing
    }
    Collections.sort(tiles);
    return tiles;
  }

  /** Returns the set of tiles the given summoner unit could choose to summon a new unit. */
  public <U extends Unit & Summoner> List<Tile> getSummonCloud(U summoner, Unit toSummon) {
    ArrayList<Tile> radialTiles = getRadialCloud(summoner.getLocation(), summoner.getSummonRange());
    HashSet<Tile> toRemove = new HashSet<Tile>();
    for (Tile t : radialTiles) {
      if (!summoner.owner.canSee(t) || t.isOccupied() || !toSummon.canOccupy(t.terrain))
        toRemove.add(t);
    }
    radialTiles.removeAll(toRemove);
    return radialTiles;
  }

  /** Returns the set of tiles the given summon selector could choose to summon a new model.unit */
  public List<Tile> getSummonCloud(SummonSelector<?> ss) {
    return getSummonCloud(ss.summoner, ss.toSummon);
  }

  /** Returns the set of tiles the given casting unit could choose to cast the given spell. */
  public List<Tile> getCastCloud(Commander caster, Ability toCast) {
    int castDist =
        toCast.castDist + (toCast.canBeCloudBoosted ? 0 : caster.owner.getCastSelectBoost());

    List<Tile> cloud = getRadialCloud(caster.getLocation(), castDist);
    // If cast dist is greater than 0, can't cast on commander location.
    if (castDist > 0) {
      cloud.remove(caster.getLocation());
    }
    List<Tile> toRemove = new ArrayList<>();
    for (Tile t : cloud) {
      if (toCast
          .getTranslatedEffectCloud(caster, t, caster.owner.getCastCloudBoost())
          .stream()
          .noneMatch(
              tile ->
                  tile.isOccupied()
                      && caster.owner.canSee(tile)
                      && toCast.wouldAffect(tile.getOccupyingUnit(), caster))) {
        toRemove.add(t);
      }
    }

    // If this is SpacialShift, cant cast on a unit on a mountain since commanders's can't occupy
    // them.
    if (toCast instanceof SpacialShift) {
      for (Tile t : cloud) {
        if (!caster.canOccupy(t.terrain)) {
          toRemove.add(t);
        }
      }
    }

    cloud.removeAll(toRemove);
    return cloud;
  }

  /** Returns the set of tiles the given cast selector could choose to cast a spell. */
  public List<Tile> getCastCloud(CastSelector cs) {
    return getCastCloud(cs.caster, cs.toCast);
  }

  /** Returns the most recent pathComputationId, from a call to getMovementCloud(..) */
  public int getPathComputationId() {
    return pathComputationId;
  }

  /**
   * Helper for two getMovementCloud methods, using the given starting tile and unit. Assumes dist
   * and prev fields of start have been set.
   */
  private ArrayList<Tile> getMovementCloud(Tile start, MovingUnit unit) {
    // Initialize non-start tile.
    for (Tile t : this) {
      if (t != start) {
        t.dist = Integer.MIN_VALUE;
        t.prev = null;
      }
    }

    // frontier sorts with higher distance earlier
    PriorityQueue<Tile> frontier =
        new PriorityQueue<Tile>(
            1,
            new Comparator<Tile>() {
              @Override
              /* Use inverse of regular comparison (higher distance first) */
              public int compare(Tile o1, Tile o2) {
                return -(o1.dist - o2.dist);
              }
            });
    frontier.add(start);
    ArrayList<Tile> settled = new ArrayList<Tile>();

    // Iteration
    while (!frontier.isEmpty()) {
      Tile current = frontier.poll();
      settled.add(current);
      for (Tile neighbor : getTileNeighbors(current, false)) {
        if (neighbor != null) {
          int nDist = current.dist - unit.getMovementCost(neighbor.terrain);
          boolean unitObstacle =
              neighbor.isOccupied()
                  && neighbor.getOccupyingUnit().owner != unit.owner
                  && unit.owner.canSee(neighbor.getOccupyingUnit());
          if (nDist >= 0 && !unitObstacle && nDist > neighbor.dist) {
            neighbor.dist = nDist;
            neighbor.prev = current;
            frontier.remove(neighbor);
            frontier.add(neighbor);
          }
        }
      }
    }

    // Just return settled tiles as possible movement. Remove duplicates as possible.
    int i = 0;
    while (i < settled.size()) {
      if (settled.indexOf(settled.get(i)) != i) {
        settled.remove(i);
      } else {
        i++;
      }
    }
    pathComputationId++;
    return settled;
  }

  /**
   * Returns the set of tiles the given MovingUnit could move to from the given location with its
   * movement cap. Only counts enemy units as obstacles if they are visible.
   */
  public ArrayList<Tile> getMovementCloud(
      MovingUnit u, Tile startTile, boolean useMaxMovementInsteadOfCurrentMovement) {
    u.getLocation().prev = null;
    u.getLocation().dist =
        useMaxMovementInsteadOfCurrentMovement ? u.getMovementCap() : u.getMovement();
    return getMovementCloud(startTile, u);
  }

  /**
   * Returns the set of tiles the given MovingUnit could move to from the given location with the
   * given arbitrary movement cap. Useful for determining how close tiles on the board are without
   * worrying about max movement in a single turn.
   */
  private ArrayList<Tile> getMovementCloudWithArbitraryMovementCap(
      MovingUnit u, Tile startTile, int moveCap) {
    u.getLocation().prev = null;
    u.getLocation().dist = moveCap;
    return getMovementCloud(startTile, u);
  }

  /**
   * Returns the set of tiles the given MovingUnit could move to from its current location with its
   * movement cap. Only counts enemy units as obstacles if they are visible.
   */
  public ArrayList<Tile> getMovementCloud(
      MovingUnit u, boolean useMaxMovementInsteadOfCurrentMovement) {
    u.getLocation().prev = null;
    u.getLocation().dist =
        useMaxMovementInsteadOfCurrentMovement ? u.getMovementCap() : u.getMovement();
    return getMovementCloud(u.getLocation(), u);
  }

  /**
   * Returns the set of tiles the given path selector could move to from its current location with
   * its movement cap. Only counts enemy units as obstacles if they are visible.
   */
  public ArrayList<Tile> getMovementCloud(PathSelector ps) {
    MovingUnit unit = ps.unit;
    Tile start = ps.getPath().getLast();

    // Uses dist to hold remainingDistance as possible.
    if (ps.getPath().getLast() != ps.unit.getLocation())
      start.dist = unit.getMovement() - unit.getTotalMovementCost(ps.getPath());
    else start.dist = unit.getMovement();
    return getMovementCloud(start, unit);
  }

  /**
   * Returns the set of tiles the given MovingUnit could move to from its current location with any
   * amount of movement. (uses an arbitrarily high movement value). Only counts enemy units as
   * obstacles if they are visible.
   */
  public ArrayList<Tile> getMovementCloudWholeBoard(MovingUnit u, Tile t) {
    u.getLocation().prev = null;
    u.getLocation().dist = WHOLE_BOARD_CLOUD_STARTING_DISTANCE;
    return getMovementCloud(t, u);
  }

  /**
   * Returns the path to the given tile from the last computed movement cloud, using the given
   * pathComputationId. Throws an exception if the given tile wasn't computed in the last movement
   * cloud computation.
   */
  public List<Tile> getMovementPath(int pathComputationId, Tile destTile) {
    if (pathComputationId != this.pathComputationId) {
      throw new RuntimeException(
          "Expected pathComputationId is out of date, is at " + pathComputationId);
    }
    if (destTile.prev == null) {
      throw new RuntimeException(destTile + " wasn't in last movement cloud computation");
    }
    LinkedList<Tile> path = new LinkedList<>();
    while (destTile != null) {
      path.push(destTile);
      destTile = destTile.prev;
    }
    return path;
  }

  /**
   * Returns the computed distance of the given tile from the last computed movement cloud, using
   * the given pathComputationId.
   */
  public int getDist(int pathComputationId, Tile t) {
    if (pathComputationId != this.pathComputationId) {
      throw new RuntimeException(
          "Expected pathComputationId is out of date, is at " + pathComputationId);
    }
    return t.dist;
  }

  /** Returns a stream over the tiles in this Board. */
  public Stream<Tile> stream() {
    Iterator<Tile> iterator = iterator();
    return Stream.generate(iterator::next).limit(getWidth() * getHeight());
  }

  /** Returns an iterator over the tiles in this Board */
  @Override
  public Iterator<Tile> iterator() {
    return new BoardIterator();
  }

  /** An iterator over boards - goes along rows for its iteration */
  private class BoardIterator implements Iterator<Tile> {

    private int r;
    private int c;

    /** Constructs a new model.board iterator, with row and column set to 0 */
    private BoardIterator() {
      r = 0;
      c = 0;
    }

    /** Return true iff r < tiles.length && c < tiles[r].length */
    @Override
    public boolean hasNext() {
      return r < tiles.length && c < tiles[r].length;
    }

    /**
     * Gets the current tile, then advances one to the right. If this goes off the row, goes to the
     * next row and resets the column counter
     */
    @Override
    public Tile next() {
      Tile t = getTileAt(r, c);
      c++;
      if (c == getWidth()) {
        r++;
        c = 0;
      }
      return t;
    }

    /** Not supported - do not call */
    @Override
    public void remove() {
      throw new RuntimeException("Remove Operation Not Supported in Board Iterators");
    }
  }

  @Override
  public String toString() {
    return "Board of size " + getWidth() + "x" + getHeight();
  }

  @Override
  public String toStringShort() {
    return "Board of size " + getWidth() + "x" + getHeight();
  }

  @Override
  public String toStringLong() {
    return "Board of size " + getWidth() + "x" + getHeight();
  }

  @Override
  public String toStringFull() {
    String s = "Board of size " + getWidth() + "x" + getHeight() + ":";
    s += "[";
    for (int i = 0; i < getHeight(); i++) {
      s += "[";
      for (int j = 0; j < getWidth(); j++) {
        s += tiles[i][j].toStringLong() + " ";
      }
      s += "] ";
    }
    return s + "]";
  }
}
