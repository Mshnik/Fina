package ai.delegates;

import ai.AIAction;

/** A list of delegates for moving units. */
public final class MovementDelegates {
  private MovementDelegates() {}

  /** A movement delegate that wants to expand the danger radius as much as possible. */
  public static final class ExpandDangerRadiusMovementDelegate implements Delegate {
    @Override
    public double getScore(AIAction action) {
      return 0;
    }
  }

  /**
   * A movement delegate that wants to allow units to attack. Wants to get the unit into immediate
   * attack range, but is also happy whenever a unit gets closer to an enemy unit.
   */
  public static final class MoveToAttackMovementDelegate implements Delegate {
    @Override
    public double getScore(AIAction action) {
      return 0;
    }
  }

  /**
   * A movement delegate that wants to allow units to attack and not be counter attacked. Wants to
   * get the unit into attack range that doesn't allow the opponent to counter attack.
   */
  public static final class MoveToAttackAndNotBeCounterAttackedMovementDelegate
      implements Delegate {
    @Override
    public double getScore(AIAction action) {
      return 0;
    }
  }

  /**
   * A movement delegate that wants to keep units from being attacked. Wants to get the unit out of
   * immediate danger range.
   */
  public static final class MoveToNotBeAttackedMovementDelegate implements Delegate {
    @Override
    public double getScore(AIAction action) {
      return 0;
    }
  }
}
