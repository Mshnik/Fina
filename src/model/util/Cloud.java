package model.util;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A set of points representing a cloud for use when selecting an ability target or any cloud
 * selection. Extended to give additional functionality.
 *
 * @author Mshnik
 */
public class Cloud {

  /** The points in this cloud. May or may not contain (0,0). */
  final Set<MPoint> points;

  /** Constructs a new cloud. Up to the constructor to enforce that the points set is correct. */
  Cloud(Set<MPoint> points) {
    this.points = points;
  }

  /** Returns the set of points in this cloud. */
  public Set<MPoint> getPoints() {
    return Collections.unmodifiableSet(points);
  }

  /** Returns true iff this cloud contains the given point. */
  public boolean contains(MPoint point) {
    return points.contains(point);
  }

  /** Returns a cloud translated to have the new center point. */
  public Cloud translate(MPoint center) {
    return new Cloud(
        points.stream().map(p -> p.add(center)).collect(Collectors.toSet()));
  }

  /** Returns a cloud reflected over y=x by reversing coordinates. */
  public Cloud reflect() {
    return new Cloud(
        points.stream().map(p -> new MPoint(p.col, p.row)).collect(Collectors.toSet()));
  }

  /** Returns a cloud rotated 90 degrees clockwise about (0,0). */
  public Cloud rotate() {
    // TODO
    return null;
  }
}
