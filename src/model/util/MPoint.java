package model.util;

import model.board.Direction;
import model.util.ExpandableCloud.ExpandableCloudType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * A class representing a point on a matrix, with row-col notation. Instances are kept by row-col
 * and returned as needed. New instances are only constructed as needed.
 */
public final class MPoint {

  /**
   * A map of all points, indexed by row, then column.
   */
  private static final Map<Integer, Map<Integer, MPoint>> points =
      Collections.synchronizedMap(new HashMap<>());

  /**
   * A point representing (0,0)
   */
  static final MPoint ORIGIN = get(0, 0);

  /**
   * The row represented by this point
   */
  public final int row;

  /**
   * The col represented by this point
   */
  public final int col;

  /**
   * Constructor for MPoint. Creates a point and adds it to points.
   *
   * @param r - row of point
   * @param c - col of point
   */
  private MPoint(int r, int c) {
    row = r;
    col = c;

    synchronized (points) {
      if (!points.containsKey(r)) {
        points.put(r, new HashMap<>());
      }
      points.get(r).put(c, this);
    }
  }

  /**
   * Returns a point for the given r, c, creating a new one only if necessary.
   *
   * @param r - row of point
   * @param c - col of point
   */
  public static MPoint get(int r, int c) {
    synchronized (points) {
      if (points.containsKey(r) && points.get(r).containsKey(c)) {
        return points.get(r).get(c);
      } else {
        return new MPoint(r, c);
      }
    }
  }

  /**
   * Returns a point from the given array of directions.
   */
  public static MPoint get(Direction... d) {
    return Arrays.stream(d).map(Direction::toPoint).reduce(MPoint.ORIGIN, MPoint::add);
  }

  /**
   * Creates a new point from adding the row and col to this.
   */
  public MPoint add(int r, int c) {
    return get(row + r, col + c);
  }

  /**
   * Creates a new point from adding the row and col components of p to this
   */
  public MPoint add(MPoint p) {
    return get(row + p.row, col + p.col);
  }

  /**
   * Returns a radial cloud centered at this
   */
  public Cloud radialCloud(int radius) {
    return ExpandableCloud.create(ExpandableCloudType.CIRCLE, radius).translate(this);
  }

  /**
   * Returns a line cloud from this to the given MPoint.
   */
  public Cloud getLineCloudTo(MPoint other) {
    // Check for single point line
    if (other.equals(this)) {
      return new Cloud(Collections.singleton(this));
    }

    // Check for vertical line
    if (col == other.col) {
      HashSet<MPoint> points = new HashSet<>();
      for (int r = row;
           row < other.row && r <= other.row || row > other.row && r >= other.row;
           r += (row < other.row ? 1 : -1)) {
        points.add(get(r, col));
      }

      return new Cloud(points);
    }

    return createLineCloud(col, other.col, row, other.row);
  }

  /**
   * Creates a line cloud corresponding to going from the starting x0,y0 to the given x1,y1. Taken
   * and modified from https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
   */
  private static Cloud createLineCloud(int x0, int x1, int y0, int y1) {
    // If more y change than x, flip coordinates, then flip back at the end.
    boolean needsFlip = Math.abs(x1 - x0) <= Math.abs(y1 - y0);
    if (needsFlip) {
      int tmp0 = x0;
      x0 = y0;
      y0 = tmp0;
      int tmp1 = x1;
      x1 = y1;
      y1 = tmp1;
    }

    HashSet<MPoint> points = new HashSet<>();
    int deltaX = x1 - x0;
    int deltaY = y1 - y0;
    double deltaErr = Math.abs((double) deltaY / (double) deltaX);
    double error = 0.0;
    int r = y0;
    for (int c = x0; x0 < x1 && c <= x1 || x0 > x1 && c >= x1; c += (x0 < x1 ? 1 : -1)) {
      points.add(get(r, c));
      error = error + deltaErr;
      if (error >= 0.5) {
        r = r + (deltaY > 0 ? 1 : -1);
        error -= 1.0;
      }
    }
    Cloud lineCloud = new Cloud(points);
    // Flip result back if needed.
    if (needsFlip) {
      return lineCloud.reflect();
    } else {
      return lineCloud;
    }
  }

  /**
   * Two points are equal if they have the same row and col
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MPoint)) return false;

    return ((MPoint) o).row == row && ((MPoint) o).col == col;
  }

  /**
   * Hashes an MPoint based on its row and col
   */
  public int hashCode() {
    return Objects.hash(row, col);
  }

  /**
   * A toString - {@code (row, col)}
   */
  public String toString() {
    return "(" + row + "," + col + ")";
  }
}
