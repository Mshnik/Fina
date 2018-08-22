package model.util;

import model.board.MPoint;

import java.util.Set;

/**
 * A set of points representing a cloud for use when selecting an ability target or
 * any cloud selection. The "center" of the cloud (the point of selection and rotation)
 * is always at (0,0), even if this point is not in the cloud.
 * @author Mshnik
 */
public abstract class Cloud {

  /** Types of clouds. */
  public enum CloudType {
    PLUS(false),
    CROSS(false),
    CIRCLE(false),
    WALL(true),
    CONE(true),
    SQUARE(false);

    private final boolean rotatable;

    CloudType(boolean rotatable) {
      this.rotatable = rotatable;
    }
  }

  /** The points in this cloud. May or may not contain (0,0). */
  private final Set<MPoint> points;

  /** The type of this cloud. Points in this cloud will make up the shape of the type. */
  protected final CloudType cloudType;

  /** Level of this cloud. Cloud will fit within square of size (level * 2) + 1. */
  protected final int level;

  /** Constructs a new cloud. Up to the constructor to enforce that the points set is correct. */
  protected Cloud(Set<MPoint> points, CloudType cloudType, int level) {
    this.points = points;
    this.cloudType = cloudType;
    this.level = level;
  }

  /** Returns true iff this cloud contains the given point. */
  public boolean contains(MPoint point) {
    return points.contains(point);
  }

  /**
   * Returns true if this cloud rotates when selecting a new point from reference of a commander,
   * false if it can just be translated.
   */
  public boolean isRotatable() {
    return cloudType.rotatable;
  }

  /** Returns a Cloud that is the same type as this with the given level delta. */
  public abstract Cloud changeLevel(int levelDelta);

}
