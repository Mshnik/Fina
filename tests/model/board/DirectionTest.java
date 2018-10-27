package model.board;

import static helpers.Asserts.assertThat;

import model.util.MPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DirectionTest {

  @Test
  public void toPointReturnsPoint() {
    for (Direction d : Direction.values()) {
      assertThat(d.toPoint())
          .isEqualTo(MPoint.get(d.dRow(), d.dCol()));
    }
  }

  @Test
  public void fromUnknownKeycodeReturnsNull() {
    assertThat(Direction.fromKeyCode(-1)).isNull();
  }
}
