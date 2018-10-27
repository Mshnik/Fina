package model.board;

import static helpers.Asserts.assertThat;

import java.util.function.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TerrainTest {

  @Test
  public void valueOfShortIsFirstChar() {
    for (Terrain t : Terrain.values()) {
      assertThat(Terrain.valueOfShort(t.name().substring(0, 1))).isEqualTo(t);
    }
  }

  @Test
  public void valueOfShortThrowsForUnknownString() {
    assertThat((Runnable) () -> Terrain.valueOfShort("Z"))
        .throwsExceptionThat(RuntimeException.class)
        .hasMessageThat()
        .isEqualTo("Unknown short terrain: Z");
  }
}
