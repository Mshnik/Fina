package helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/** Assertion utilities for testing. */
public final class Asserts {
  private Asserts() {}

  /** Creates a subject on the given integer element. */
  public static Subject<Integer> assertThat(Integer elem) {
    return new Subject<>(elem);
  }

  /** Creates a subject on the given double element. */
  public static Subject<Double> assertThat(Double elem) {
    return new Subject<>(elem);
  }

  /** Creates a subject on the given float element. */
  public static Subject<Float> assertThat(Float elem) {
    return new Subject<>(elem);
  }

  /** Creates a subject on the given string element. */
  public static StringSubject assertThat(String elem) {
    return new StringSubject(elem);
  }

  /** Creates a subject on the given boolean element. */
  public static BooleanSubject assertThat(Boolean elem) {
    return new BooleanSubject(elem);
  }

  /** Creates a subject on the given collection element. */
  public static <T, C extends Collection<T>> CollectionSubject<T, C> assertThat(C elem) {
    return new CollectionSubject<>(elem);
  }

  /** Creates a subject on the given object element. */
  public static <T> Subject<T> assertThat(T elem) {
    return new Subject<>(elem);
  }

  /** Creates a lazy subject on the given runnable. */
  public static LazySubject assertThat(Runnable runnable) {
    return new LazySubject(runnable);
  }

  /** An abstract parent of subjects. Stores the value and exposes standard assertion methods. */
  private abstract static class AbsSubject<T> {
    final T val;

    private AbsSubject(T val) {
      this.val = val;
    }

    public void isNull() {
      assertNull(val);
    }

    public void isNonNull() {
      assertNotNull(val);
    }

    public void isEqualTo(T other) {
      assertEquals(other, val);
    }

    public void isNotEqualTo(T other) {
      assertNotEquals(other, val);
    }

    public void isSameInstanceAs(T other) {
      assertSame(other, val);
    }

    public void isNotSameInstanceAs(T other) {
      assertNotSame(other, val);
    }

    public void isIn(Collection<T> collection) {
      assertTrue(
          String.format("Expected %s to contain %s", collection, val), collection.contains(val));
    }
  }

  /** A default subject, only exposing the methods in {@link AbsSubject}. */
  public static final class Subject<T> extends AbsSubject<T> {
    private Subject(T val) {
      super(val);
    }
  }

  /** A subject specifically for booleans, exposing conditional assertions. */
  public static final class BooleanSubject extends AbsSubject<Boolean> {
    private BooleanSubject(Boolean val) {
      super(val);
    }

    public void isTrue() {
      assertTrue(val);
    }

    public void isFalse() {
      assertFalse(val);
    }
  }

  /** A subject for strings, exposing containment logic. */
  public static final class StringSubject extends AbsSubject<String> {
    private StringSubject(String val) {
      super(val);
    }

    public void contains(String substring) {
      assertTrue(
          String.format("Expected %s to contain %s", val, substring), val.contains(substring));
    }

    public void startsWith(String prefix) {
      assertTrue(
          String.format("Expected %s to start with %s", val, prefix), val.startsWith(prefix));
    }

    public void endsWith(String suffix) {
      assertTrue(String.format("Expected %s to end with %s", val, suffix), val.endsWith(suffix));
    }
  }

  /** A subject for collections, exposing containment assertions. */
  public static final class CollectionSubject<T, C extends Collection<T>> extends AbsSubject<C> {
    private CollectionSubject(C val) {
      super(val);
    }

    public void hasSize(int size) {
      assertEquals(size, val.size());
    }

    public void contains(T elem) {
      assertTrue(String.format("Expected %s to contain %s", val, elem), val.contains(elem));
    }

    @SafeVarargs
    public final void containsExactly(T... elems) {
      containsExactlyElementsIn(Arrays.asList(elems));
    }

    public final void containsExactlyElementsIn(Collection<T> elemsIterable) {
      assertTrue(
          String.format("Expected %s to contain exactly values in %s", val, elemsIterable),
          val.containsAll(elemsIterable) && elemsIterable.containsAll(val));
    }
  }

  /** A subject on a supplier. get() is only called when an assertion is performed. */
  public static final class LazySubject {
    private final Runnable runnable;

    private LazySubject(Runnable runnable) {
      this.runnable = runnable;
    }

    public void throwsException(Class<? extends Exception> exception) {
      throwsExceptionThat(exception);
    }

    public <E extends Exception> ExceptionSubject<E> throwsExceptionThat(Class<E> exception) {
      try {
        runnable.run();
        fail(
            String.format(
                "Expected an exception of type %s, but no exception was thrown.",
                exception.getName()));
      } catch (Exception e) {
        if (exception.isInstance(e)) {
          return new ExceptionSubject<>(exception.cast(e));
        } else {
          fail(
              String.format(
                  "Expected an exception of type %s, but got: %s",
                  exception.getName(), e.toString()));
        }
      }
      throw new AssertionError("Unreachable");
    }
  }

  /** A subject on an exception. Exposes additional methods for assertion on message / cause. */
  public static final class ExceptionSubject<T extends Throwable> extends AbsSubject<T> {
    private ExceptionSubject(T val) {
      super(val);
    }

    public ExceptionSubject<Throwable> hasCauseThat() {
      return new ExceptionSubject<>(val.getCause());
    }

    public StringSubject hasMessageThat() {
      return new StringSubject(val.getMessage());
    }
  }
}
