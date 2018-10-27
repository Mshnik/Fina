package model.util;

import static helpers.Asserts.assertThat;

import helpers.ParameterizedHelpers;
import java.util.Arrays;
import java.util.stream.IntStream;
import model.util.ExpandableCloud.ExpandableCloudType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public final class ExpandableCloudTest {

  @Parameters(name = "{0},{1}")
  public static Iterable<Object[]> types() {
    return ParameterizedHelpers.generateData(
        Arrays.asList(ExpandableCloudType.values()), Arrays.asList(0, 1, 2, 3, 4));
  }

  private final ExpandableCloud cloud;
  private final ExpandableCloud expandedCloud;
  private final int expectedSize;

  public ExpandableCloudTest(ExpandableCloudType type, int radius) {
    cloud = ExpandableCloud.create(type, radius);
    expandedCloud = ExpandableCloud.create(type, radius + 1);
    switch (type) {
      case PLUS:
      case CROSS:
        expectedSize = radius * 4 + 1;
        break;
      case WALL:
        expectedSize = radius * 2 + 1;
        break;
      case CONE:
        expectedSize = (radius + 1) * (radius + 1);
        break;
      case CIRCLE:
        expectedSize = IntStream.range(1, radius + 1).sum() * 4 + 1;
        break;
      case SQUARE:
        expectedSize = (2 * radius + 1) * (2 * radius + 1);
        break;
      default:
        throw new RuntimeException("Unknown type: " + type);
    }
  }

  @Test
  public void allCloudsContainOrigin() {
    assertThat(cloud.contains(MPoint.ORIGIN)).isTrue();
  }

  @Test
  public void hasExpectedSize() {
    assertThat(cloud.getSize()).isEqualTo(expectedSize);
  }

  @Test
  public void expandedCloudHasCorrectPoints() {
    assertThat(cloud.expand(1).getPoints()).containsExactlyElementsIn(expandedCloud.getPoints());
  }

  @Test
  public void expandedCloudIsSameInstance() {
    assertThat(cloud.expand(1)).isSameInstanceAs(expandedCloud);
  }
}
