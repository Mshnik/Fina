package ai.delegating;

import ai.delegates.Delegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A factory for DelegatingAIControllers that handles setting delegate weights.
 */
public final class DelegatingAIControllerFactory {
  private DelegatingAIControllerFactory() {
  }

  /**
   * Prints a DelegatingAIController with weights, for data interpretation.
   */
  public static void main(String[] args) {
    String weights =
        "2.100,0.798,3.609,3.301,0.643,1.108,3.905,2.682,3.281,2.168,2.107,3.098,"
            + "4.073,0.346,0.841,2.928,0.101,2.029,0.440,3.906,1.794,0.438,2.072,1.907,4.233,1.478,"
            + "3.027,4.676,3.889,2.078,0.388,2.360,1.662,2.921,1.983,2.270,1.209,3.177,1.102,2.506,"
            + "0.993,4.673,0.762,0.646,0.454,2.065,3.139,3.461,2.342,2.998,2.763,1.603,2.096,1.814,"
            + "5.116,2.500,2.127,2.648,3.428,3.501,1.885,4.875,2.883,1.006,1.240,3.092,1.265,1.324,"
            + "2.989,1.056,-0.104,1.930,1.655,3.108,2.995,1.497,2.704,2.488,2.860,3.808,2.559,2.260,"
            + "2.294,1.945,2.669,2.755,1.878,1.620,1.037,2.394,2.693,1.261,0.722,4.042,2.800,1.123";

    DelegatingAIController controller =
        DelegatingAIControllerFactory.copyOf(
            DelegatingAIControllers.randomWeightsDelegatingAIController())
            .setWeights(
                Arrays.stream(weights.split(","))
                    .map(Double::parseDouble)
                    .collect(Collectors.toList()))
            .build();
    System.out.println(controller.getConfigString());
  }

  /**
   * Id suffix to assign. Will be regenerated between different class loadings, but that should be
   * enough to guarantee id uniqueness.
   */
  private static final AtomicInteger idCounter = new AtomicInteger();

  /**
   * The id to set for the next call to build().
   */
  private String id = "";

  /**
   * The current set of delegates to add to the next call of build().
   */
  private final List<Delegate> delegates = new ArrayList<>();

  /**
   * Returns a new builder.
   */
  public static DelegatingAIControllerFactory newBuilder() {
    return new DelegatingAIControllerFactory();
  }

  /**
   * Returns a new builder that copies the given DelegatingAIController.
   */
  public static DelegatingAIControllerFactory copyOf(
      DelegatingAIController delegatingAIController) {
    DelegatingAIControllerFactory factory = new DelegatingAIControllerFactory();
    factory.delegates.addAll(delegatingAIController.getDelegates());
    factory.id = delegatingAIController.id();
    return factory;
  }

  /**
   * Sets the id and returns this.
   */
  public DelegatingAIControllerFactory setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Sets the id to the timestamp plus the nextId returns this.
   */
  public DelegatingAIControllerFactory setIdToTimestampPlusNextId() {
    this.id = System.currentTimeMillis() + "-" + idCounter.getAndIncrement();
    return this;
  }

  /**
   * Adds the given delegate and returns this.
   */
  public DelegatingAIControllerFactory addDelegate(Delegate delegate) {
    delegates.add(delegate);
    return this;
  }

  /**
   * Adds all of the given delegate and returns this.
   */
  public DelegatingAIControllerFactory addDelegates(Collection<Delegate> delegateCollection) {
    delegates.addAll(delegateCollection);
    return this;
  }

  /**
   * Sets the weights of the delegates added, consuming in the order provided. Throws if the list of
   * weights is not the correct size.
   */
  public DelegatingAIControllerFactory setWeights(List<Double> weights) {
    LinkedList<Double> weightsCopy = new LinkedList<>(weights);
    for (Delegate d : delegates) {
      if (d.getSubweightsHeaders().isEmpty()) {
        d.withWeight(weightsCopy.poll());
      } else {
        d.withSubweights(weightsCopy.subList(0, d.getSubweightsLength()));
        for (int i = 0; i < d.getSubweightsLength(); i++) {
          weightsCopy.poll();
        }
      }
    }
    if (!weightsCopy.isEmpty()) {
      throw new RuntimeException("Got too many weights by " + weightsCopy.size());
    }
    return this;
  }

  /**
   * Returns a new DelegatingAIController with the configured delegates.
   */
  public DelegatingAIController build() {
    DelegatingAIController delegatingAIController = new DelegatingAIController(id);
    for (Delegate d : delegates) {
      delegatingAIController.addDelegate(d);
    }
    return delegatingAIController;
  }
}
