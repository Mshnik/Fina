package ai.delegating;

import ai.delegates.Delegate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** A factory for DelegatingAIControllers that handles setting delegate weights. */
public final class DelegatingAIControllerFactory {
  private DelegatingAIControllerFactory() {}

  /**
   * Id suffix to assign. Will be regenerated between different class loadings, but that should be
   * enough to guarantee id uniqueness.
   */
  private static final AtomicInteger idCounter = new AtomicInteger();

  /** The id to set for the next call to build(). */
  private String id = "";

  /** The current set of delegates to add to the next call of build(). */
  private List<Delegate> delegates = new ArrayList<>();

  /** Returns a new builder. */
  public static DelegatingAIControllerFactory newBuilder() {
    return new DelegatingAIControllerFactory();
  }

  /** Sets the id and returns this. */
  public DelegatingAIControllerFactory setId(String id) {
    this.id = id;
    return this;
  }

  /** Sets the id to the timestamp plus the nextId returns this. */
  public DelegatingAIControllerFactory setIdToTimestampPlusNextId() {
    this.id = Long.toString(System.currentTimeMillis()) + "-" + idCounter.getAndIncrement();
    return this;
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
    DelegatingAIController delegatingAIController = new DelegatingAIController(id);
    for (Delegate d : delegates) {
      delegatingAIController.addDelegate(d);
    }
    return delegatingAIController;
  }
}
