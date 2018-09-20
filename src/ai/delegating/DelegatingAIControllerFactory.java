package ai.delegating;

import ai.delegates.Delegate;

import java.util.ArrayList;
import java.util.List;

/** A factory for DelegatingAIControllers that handles setting delegate weights. */
final class DelegatingAIControllerFactory {
  private DelegatingAIControllerFactory() {}

  /** The current set of delegates to add to the next call of build(). */
  private List<Delegate> delegates = new ArrayList<>();

  /** Returns a new builder. */
  static DelegatingAIControllerFactory newBuilder() {
    return new DelegatingAIControllerFactory();
  }

  /** Adds the given delegate and returns this. */
  DelegatingAIControllerFactory addDelegate(Delegate delegate) {
    delegates.add(delegate);
    return this;
  }

  /** Returns a new DelegatingAIController with the configured delegates. */
  DelegatingAIController build() {
    DelegatingAIController delegatingAIController = new DelegatingAIController();
    for (Delegate d : delegates) {
      delegatingAIController.addDelegate(d);
    }
    return delegatingAIController;
  }
}
