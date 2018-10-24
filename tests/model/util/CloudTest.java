package model.util;

import static helpers.Asserts.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CloudTest {

  private Cloud createCloud(List<MPoint> list) {
    return new Cloud(new HashSet<>(list));
  }

  @Test
  public void constructorSetsPoints() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    assertThat(createCloud(points).getPoints()).containsExactlyElementsIn(points);
  }

  @Test
  public void containsReturnsTrueForContainedPoint() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    assertThat(createCloud(points).contains(MPoint.get(1, 1))).isTrue();
    assertThat(createCloud(points).contains(MPoint.get(1, -1))).isFalse();
  }

  @Test
  public void translateMovesPoints() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    MPoint delta = MPoint.get(2, 5);
    List<MPoint> translatedPoints = points.stream().map(delta::add).collect(Collectors.toList());
    assertThat(createCloud(points).translate(delta).getPoints())
        .containsExactlyElementsIn(translatedPoints);
  }

  @Test
  public void reflectReflectsPoints() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    List<MPoint> reflectedPoints =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(2, 1), MPoint.get(3, 1), MPoint.get(4, 2));
    assertThat(createCloud(points).reflect().getPoints())
        .containsExactlyElementsIn(reflectedPoints);
  }

  @Test
  public void rotateRotatesPoints_Clockwise() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    List<MPoint> rotatedPoints =
        Arrays.asList(MPoint.get(1, -1), MPoint.get(2, -1), MPoint.get(3, -1), MPoint.get(4, -2));
    assertThat(createCloud(points).rotate(true).getPoints())
        .containsExactlyElementsIn(rotatedPoints);
  }

  @Test
  public void rotateRotatesPoints_CounterClockwise() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    List<MPoint> rotatedPoints =
        Arrays.asList(MPoint.get(-1, 1), MPoint.get(-2, 1), MPoint.get(-3, 1), MPoint.get(-4, 2));
    assertThat(createCloud(points).rotate(false).getPoints())
        .containsExactlyElementsIn(rotatedPoints);
  }

  @Test
  public void differenceRemovesPoints() {
    List<MPoint> points =
        Arrays.asList(MPoint.get(1, 1), MPoint.get(1, 2), MPoint.get(1, 3), MPoint.get(2, 4));
    List<MPoint> pointsToRemove = points.subList(0, 2);
    List<MPoint> remainingPoints = points.subList(2, points.size());
    assertThat(createCloud(points).difference(createCloud(pointsToRemove)).getPoints())
        .containsExactlyElementsIn(remainingPoints);
  }
}
