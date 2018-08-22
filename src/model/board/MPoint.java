package model.board;

import model.util.Cloud;
import model.util.Clouds;

import java.awt.*;
import java.util.Objects;

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

  /** Creates a new point from adding the row and col components of p to this */
  public MPoint add(MPoint p) {
    return new MPoint(row + p.row, col + p.col);
  }

  /** Returns a radial cloud centered at this */
  public Cloud radialCloud(int radius) {
    return Clouds.getCloud(Cloud.CloudType.CIRCLE, radius).translate(this);
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
