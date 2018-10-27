package model.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a cloud created via formula for a given side length. This allows it to be expanded or
 * contracted. Translating, reflecting, or rotating a cloud removes this functionality.
 */
public final class ExpandableCloud extends Cloud {

  /** Types of expandable clouds. */
  public enum ExpandableCloudType {
    PLUS {
      @Override
      Set<MPoint> createCloud(int size) {
        Set<MPoint> set = createInitialSet();
        for (int i = 1; i <= size; i++) {
          set.add(MPoint.get(i, 0));
          set.add(MPoint.get(-i, 0));
          set.add(MPoint.get(0, i));
          set.add(MPoint.get(0, -i));
        }
        return set;
      }
    },
    CROSS {
      @Override
      Set<MPoint> createCloud(int size) {
        Set<MPoint> set = createInitialSet();
        for (int i = 1; i <= size; i++) {
          set.add(MPoint.get(i, i));
          set.add(MPoint.get(-i, i));
          set.add(MPoint.get(-i, -i));
          set.add(MPoint.get(i, -i));
        }
        return set;
      }
    },
    CIRCLE {
      @Override
      Set<MPoint> createCloud(int size) {
        Set<MPoint> set = createInitialSet();
        for (int r = -size; r <= size; r++) {
          for (int c = -size; c <= size; c++) {
            if (Math.abs(r) + Math.abs(c) <= size) {
              set.add(MPoint.get(r, c));
            }
          }
        }
        return set;
      }
    },
    WALL {
      @Override
      Set<MPoint> createCloud(int size) {
        Set<MPoint> set = createInitialSet();
        for (int i = 1; i <= size; i++) {
          set.add(MPoint.get(i, 0));
          set.add(MPoint.get(-i, 0));
        }
        return set;
      }
    },
    CONE {
      @Override
      Set<MPoint> createCloud(int size) {
        Set<MPoint> set = createInitialSet();
        for (int r = -size; r <= size; r++) {
          for (int c = 1; c <= size; c++) {
            if (Math.abs(r) <= c) {
              set.add(MPoint.get(r, c));
            }
          }
        }
        return set;
      }
    },
    SQUARE {
      @Override
      Set<MPoint> createCloud(int size) {
        Set<MPoint> set = createInitialSet();
        for (int r = -size; r <= size; r++) {
          for (int c = -size; c <= size; c++) {
            set.add(MPoint.get(r, c));
          }
        }
        return set;
      }
    };

    /** Returns the set of points represented by this cloud type for the given size. */
    abstract Set<MPoint> createCloud(int size);

    /** Helper that creates a mutable set initially containing (0,0). */
    private static Set<MPoint> createInitialSet() {
      Set<MPoint> set = new HashSet<>();
      set.add(MPoint.ORIGIN);
      return set;
    }
  }

  /** Map of clouds created so far. */
  private static final Map<ExpandableCloudType, Map<Integer, ExpandableCloud>> CLOUDS =
      Collections.synchronizedMap(new HashMap<>());

  /* Setup default clouds map. */
  static {
    synchronized (CLOUDS) {
      for (ExpandableCloudType type : ExpandableCloudType.values()) {
        CLOUDS.put(type, new HashMap<>());
      }
    }
  }

  /** Creates an Expandable cloud of the given size and type. Size >= 0. */
  public static ExpandableCloud create(ExpandableCloudType type, int size) {
    if (size < 0) {
      throw new RuntimeException("Expected size >= 0, got " + size);
    }
    synchronized (CLOUDS) {
      if (CLOUDS.get(type).containsKey(size)) {
        return CLOUDS.get(type).get(size);
      }
      ExpandableCloud cloud = new ExpandableCloud(type.createCloud(size), size, type);
      CLOUDS.get(type).put(size, cloud);
      return cloud;
    }
  }

  /** Level of this cloud. Cloud will fit within square of size (level * 2) + 1. */
  private final int size;

  /** The type of this cloud. */
  public final ExpandableCloudType type;

  /** Constructs a new cloud. Up to the constructor to enforce that the points set is correct. */
  private ExpandableCloud(Set<MPoint> points, int size, ExpandableCloudType type) {
    super(points);
    this.size = size;
    this.type = type;
  }

  /** Returns a cloud of the same type by expanding by the given deltaSize. */
  public ExpandableCloud expand(int deltaSize) {
    return create(type, size + deltaSize);
  }
}
