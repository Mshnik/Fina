package util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A utility class (outside of the game) for log parsing and rewriting. */
public final class LogUtils {

  public static void main(String[] args) throws Exception {
    reduceConfigsAndResults();
  }

  /** Copies the contents of configs.txt into configs_reduced.txt with text removed. */
  private static void reduceConfigsAndResults() throws Exception {
    List<String> configLineList = TextIO.readToArray(new File(ResultsPrinter.CONFIGS_FILEPATH));
    System.out.println("Read config contents to list");
    List<String> resultLineList = TextIO.readToArray(new File(ResultsPrinter.RESULTS_FILEPATH));
    System.out.println("Read result contents to list");
    Pattern idPattern = Pattern.compile("[0-9]+-[0-9]+");
    Pattern winLossPattern = Pattern.compile("\\[.*]");

    Map<String, Integer> winLossMap = new HashMap<>();
    for (String str : resultLineList) {
      Matcher idMatcher = idPattern.matcher(str);
      idMatcher.find();
      Matcher winLossMatcher = winLossPattern.matcher(str);
      winLossMatcher.find();

      String id = idMatcher.group();
      int winLoss = winLossMatcher.group().equals("[WIN]") ? 1 : -1;
      winLossMap.compute(id, (key, val) -> val == null ? winLoss : val + winLoss);
    }

    StringBuilder builder = new StringBuilder();
    Pattern weightPattern = Pattern.compile("-?[0-9]+\\.[0-9]+");
    int weightAtrCount = configLineList.get(0).split(",").length + 8;
    builder.append("ID,WinLossValue");
    for (int i = 0; i < weightAtrCount; i++) {
      builder.append(',');
      builder.append("Weight");
      builder.append(i);
    }
    builder.append("\n");
    int i = 0;
    for (String str : configLineList) {
      Matcher idMatcher = idPattern.matcher(str);
      idMatcher.find();
      String id = idMatcher.group();
      builder.append(id);
      builder.append(',');
      builder.append(winLossMap.get(id));
      Matcher weightMatcher = weightPattern.matcher(str);
      while (weightMatcher.find()) {
        builder.append(',');
        builder.append(weightMatcher.group());
      }
      builder.append('\n');
      System.out.println("Parsed row " + (i++));
    }
    TextIO.write(new File(ResultsPrinter.REDUCED_FILEPATH), builder.toString());
  }
}
