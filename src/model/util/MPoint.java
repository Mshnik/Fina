package model.util;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import model.util.ExpandableCloud.ExpandableCloudType;

public final class MPoint {

  /** A point representing (0,0) */
  public static final MPoint ORIGIN = new MPoint(0, 0);

  /** The row represented by this point */
  public final int row;

  /** The col represented by this point */
  public final int col;

  /**
   * Constructor for MPoint
   *
   * @param r - row of point
   * @param c - col of point
   */
  public MPoint(int r, int c) {
    row = r;
    col = c;
  }

  /**
   * Duplication constructor
   *
   * @param p - the point to clone
   */
  public MPoint(MPoint p) {
    row = p.row;
    col = p.col;
  }

  /**
   * Conversion constructor
   *
   * @param p - the Point to convert. (x,y) -> (col, row)
   */
  public MPoint(Point p) {
    row = p.y;
    col = p.x;
  }

  /** Creates a new point from adding the row and col to this. */
  public MPoint add(int r, int c) {
    return new MPoint(row + r, col + c);
  }

  /** Creates a new point from adding the row and col components of p to this */
  public MPoint add(MPoint p) {
    return new MPoint(row + p.row, col + p.col);
  }

  /** Returns a radial cloud centered at this */
  public Cloud radialCloud(int radius) {
    return ExpandableCloud.create(ExpandableCloudType.CIRCLE, radius).translate(this);
  }

  /** Returns a line cloud from this to the given MPoint. */
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
        points.add(new MPoint(r, col));
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
      points.add(new MPoint(r, c));
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

  /** Two points are equal if they have the same row and col */
  public boolean equals(Object o) {
    if (!(o instanceof MPoint)) return false;

    return ((MPoint) o).row == row && ((MPoint) o).col == col;
  }

  /** Hashes an MPoint based on its row and col */
  public int hashCode() {
    return Objects.hash(row, col);
  }

  /** A toString - {@code (row, col)} */
  public String toString() {
    return "(" + row + "," + col + ")";
  }
}
