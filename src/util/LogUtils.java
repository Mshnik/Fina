package util;

import ai.delegates.Delegate;
import ai.delegating.DelegatingAIControllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class (outside of the game) for log parsing and rewriting.
 */
public final class LogUtils {

  public static void main(String[] args) throws Exception {
    reduceConfigsAndResults();
  }

  /**
   * Copies the contents of configs.txt into configs_reduced.txt with text removed.
   */
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

    List<Delegate> delegates =
        DelegatingAIControllers.randomWeightsDelegatingAIController().getDelegates();

    StringBuilder builder = new StringBuilder();
    Pattern weightPattern = Pattern.compile("-?[0-9]+\\.[0-9]+");
    builder.append("ID,WinLossValue");
    for (Delegate d : delegates) {
      String delegateName = d.getClass().getSimpleName();
      builder.append(',');
      builder.append(delegateName);
      for (String subweightHeader : d.getSubweightsHeaders()) {
        builder.append(',');
        builder.append(delegateName);
        builder.append('-');
        builder.append(subweightHeader);
      }
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
