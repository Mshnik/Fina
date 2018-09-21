package ai.delegating;

import ai.delegates.Delegate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A factory for DelegatingAIControllers that handles setting delegate weights. */
public final class DelegatingAIControllerFactory {
  private DelegatingAIControllerFactory() {}

  /** The current set of delegates to add to the next call of build(). */
  private List<Delegate> delegates = new ArrayList<>();

  /** Returns a new builder. */
  public static DelegatingAIControllerFactory newBuilder() {
    return new DelegatingAIControllerFactory();
  }

  /** Adds the given delegate and returns this. */
  public DelegatingAIControllerFactory addDelegate(Delegate delegate) {
    delegates.add(delegate);
    return this;
  }

  /** Adds all of the given delegate and returns this. */
  public DelegatingAIControllerFactory addDelegates(Collection<Delegate> delegateCollection) {
    delegates.addAll(delegateCollection);
    return this;
  }

  /** Returns a new DelegatingAIController with the configured delegates. */
  public DelegatingAIController build() {
    DelegatingAIController delegatingAIController = new DelegatingAIController();
    for (Delegate d : delegates) {
      delegatingAIController.addDelegate(d);
    }
    return delegatingAIController;
  }
}
