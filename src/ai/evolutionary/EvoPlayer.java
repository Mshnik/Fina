package ai.evolutionary;

import ai.delegates.Delegate;
import ai.delegating.DelegatingAIController;
import ai.delegating.DelegatingAIControllerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * An evolutionary player, across multiple games. Maintains the arguments necessary to spawn a new
 * player, along with a number of points.
 */
final class EvoPlayer {

  /** The amount of points to be knocked out. */
  private static final int KNOCKOUT_POINTS = 0;

  /** The amount of points an EvoPlayer starts with. */
  private static final int STARTING_POINTS = 1;

  /** The amount of points needed to split. */
  private static final int SPLIT_POINTS = STARTING_POINTS * 2;

  /** The amount of points a player gains when winning and loses when losing. */
  private static final int DELTA_POINTS = 1;

  /** The chance of a mutation on weight of a delegate. */
  private static final double WEIGHT_MUTATION_CHANCE_ON_SPLIT = 0.3;

  /** The chance of a mutation on a subweight of a delegate. */
  private static final double SUBWEIGHT_MUTATION_CHANCE_ON_SPLIT = 0.2;

  /**
   * The max value of a mutation (in absolute value) on a mutated attribute. Actual value determined
   * by uniform random over [-max, max].
   */
  private static final double MUTATION_MAX_VALUE_ON_SPLIT = 0.2;

  /** Possible changes based on a point change. */
  enum PointChangeResult {
    /** No change necessary. */
    NO_CHANGE,
    /** Should split. */
    SPLIT,
    /** Is knocked out. */
    KNOCKOUT
  }

  /** The current points this has. */
  private int points;

  /** The list of delegates to use for this player. */
  private final List<Delegate> delegateList;

  /** The controller to use for this EvoPlayer. */
  private final DelegatingAIController aiController;

  /** Creates a new EvoPlayer with the given delegates list. */
  EvoPlayer(Iterable<Delegate> delegates) {
    points = STARTING_POINTS;
    delegateList = new ArrayList<>();
    delegates.forEach(delegateList::add);
    aiController =
        DelegatingAIControllerFactory.newBuilder()
            .setIdToTimestampPlusNextId()
            .addDelegates(delegateList)
            .build();
  }

  /** Reset's this' points after splitting. */
  void resetPoints() {
    points = STARTING_POINTS;
  }

  /**
   * Changes this player's points, adding points if win is true and subtracting points if win is
   * false. Returns a result that should be processed based on the number of points this has after
   * the point change.
   */
  PointChangeResult changePoints(boolean win) {
    points += DELTA_POINTS * (win ? 1 : -1);
    if (points <= KNOCKOUT_POINTS) {
      return PointChangeResult.KNOCKOUT;
    } else if (points >= SPLIT_POINTS) {
      return PointChangeResult.SPLIT;
    } else {
      return PointChangeResult.NO_CHANGE;
    }
  }

  /** Returns a list of headers for the weights used in this, in the order of delegates. */
  List<String> getWeightsHeader() {
    List<String> headers = new ArrayList<>();
    for (Delegate delegate : delegateList) {
      String simpleName = delegate.getClass().getSimpleName();
      List<String> subHeaders = delegate.getSubweightsHeaders();
      if (subHeaders.isEmpty()) {
        headers.add(simpleName);
      } else {
        headers.addAll(
            subHeaders
                .stream()
                .map(header -> simpleName + "-" + header)
                .collect(Collectors.toList()));
      }
    }
    return headers;
  }

  /** Returns a list of all weights used in this, in the order of delegates. */
  List<Double> getWeightsList() {
    List<Double> weights = new ArrayList<>();
    for (Delegate delegate : delegateList) {
      if (delegate.getSubweightsHeaders().isEmpty()) {
        weights.add(delegate.getWeight());
      } else {
        weights.addAll(
            Arrays.stream(delegate.getSubWeights())
                .mapToObj(d -> d * delegate.getWeight())
                .collect(Collectors.toList()));
      }
    }
    return weights;
  }

  /** Returns a controller to use for this player, when it plays a game. */
  DelegatingAIController getController() {
    return aiController;
  }

  /**
   * Creates a new EvoPlayer that's the result of this splitting. Copies each delegate to a list,
   * then performs a random evolutionary change.
   */
  EvoPlayer split() {
    Random random = new Random();
    List<Delegate> delegateCopies =
        delegateList.stream().map(Delegate::copy).collect(Collectors.toList());
    for (Delegate delegate : delegateCopies) {
      if (random.nextDouble() < WEIGHT_MUTATION_CHANCE_ON_SPLIT) {
        delegate.changeWeight(
            (random.nextDouble() * MUTATION_MAX_VALUE_ON_SPLIT * 2) - MUTATION_MAX_VALUE_ON_SPLIT);
      }
      for (int i = 0; i < delegate.getSubweightsLength(); i++) {
        if (random.nextDouble() < SUBWEIGHT_MUTATION_CHANCE_ON_SPLIT) {
          delegate.getChangeSubWeight(
              i,
              (random.nextDouble() * MUTATION_MAX_VALUE_ON_SPLIT * 2)
                  - MUTATION_MAX_VALUE_ON_SPLIT);
        }
      }
    }
    return new EvoPlayer(delegateCopies);
  }
}
