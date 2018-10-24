package model.util;

import static helpers.Asserts.assertThat;

import model.board.Direction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class MPointTest {

  @Test
  public void getCreatesPointWithRowAndCol() {
    int row = 1;
    int col = 2;
    assertThat(MPoint.get(row, col).row).isEqualTo(row);
    assertThat(MPoint.get(row, col).col).isEqualTo(col);
  }

  @Test
  public void originHasRowAndCol() {
    assertThat(MPoint.ORIGIN.row).isEqualTo(0);
    assertThat(MPoint.ORIGIN.col).isEqualTo(0);
  }

  @Test
  public void addAddsRowAndCol() {
    assertThat(MPoint.get(1, 2).add(1, 2).row).isEqualTo(2);
    assertThat(MPoint.get(1, 2).add(1, 3).col).isEqualTo(5);
  }

  @Test
  public void addPointAddsRowAndCol() {
    assertThat(MPoint.get(1, 2).add(MPoint.get(1, 2)).row).isEqualTo(2);
    assertThat(MPoint.get(1, 2).add(MPoint.get(1, 3)).col).isEqualTo(5);
  }

  @Test
  public void addDoesNotChangeInstanceValue() {
    MPoint p1 = MPoint.get(1, 2);
    p1.add(1, 2);
    assertThat(p1.row).isEqualTo(1);
    assertThat(p1.col).isEqualTo(2);
  }

  @Test
  public void pointsArePooled() {
    MPoint p1 = MPoint.get(1, 2);
    MPoint p2 = MPoint.get(1, 2);
    assertThat(p1).isSameInstanceAs(p2);
  }

  @Test
  public void emptyGetReturnsOrigin() {
    assertThat(MPoint.get()).isEqualTo(MPoint.ORIGIN);
  }

  @Test
  public void getSumsDirections() {
    assertThat(MPoint.get(Direction.DOWN, Direction.RIGHT)).isEqualTo(MPoint.get(1, 1));
  }

  @Test
  public void equalPointsReturnsEqualsTrue() {
    assertThat(MPoint.get(1, 2)).isEqualTo(MPoint.get(1, 2));
  }

  @Test
  public void nonEqualPointsReturnsEqualsFalse() {
    assertThat(MPoint.get(1, 2)).isNotEqualTo(MPoint.get(1, 3));
  }

  @Test
  public void equalPointsHaveSameHashcode() {
    assertThat(MPoint.get(1, 2).hashCode()).isEqualTo(MPoint.get(1, 2).hashCode());
  }

  // TODO - test on:
  // - get radial cloud
  // - line cloud to
}
