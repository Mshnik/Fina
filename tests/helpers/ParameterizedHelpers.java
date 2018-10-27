package helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Helpers for writing Parameterized tests. */
public final class ParameterizedHelpers {
  private ParameterizedHelpers() {}

  /**
   * Generates all possible combinations of all elements in the given lists. Each object array in
   * the return will have length equal to {@code lists.size()}. Elements are ordered in the returned
   * arrays in the same order of the given lists.
   */
  public static Iterable<Object[]> generateData(List<?>... lists) {
    return generateDataHelper(lists, 0, new ArrayList<>());
  }

  /** Helper for {@link ParameterizedHelpers#generateData(List[])}. */
  private static Iterable<Object[]> generateDataHelper(
      List<?>[] lists, int index, List<Object[]> buildingList, Object... currentVals) {
    if (index >= lists.length) {
      buildingList.add(Arrays.copyOf(currentVals, currentVals.length));
      return buildingList;
    }
    for (Object val : lists[index]) {
      Object[] newVals = Arrays.copyOf(currentVals, currentVals.length + 1);
      newVals[currentVals.length] = val;
      generateDataHelper(lists, index + 1, buildingList, newVals);
    }
    return buildingList;
  }
}
