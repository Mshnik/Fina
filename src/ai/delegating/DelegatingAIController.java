package ai.delegating;

import ai.AIAction;
import ai.AIController;
import ai.delegates.Delegate;
import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.combatant.Combatant;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/** An AI controller that maintains a set of delegates to determine its behavior. */
public final class DelegatingAIController implements AIController {

  /** The delegates this is delegating to. */
  private final Set<Delegate> delegates;

  /**
   * A pair of an AIAction and a value of how much this controller likes it. The value can be
   * recomputed at any time. Null until it is computed for the first time. Hashes and computes
   * equality by AIAction alone, so value computation won't alter location in set or change
   * equality.
   */
  private final class AIActionWithValue implements Comparable<AIActionWithValue> {
    private final AIAction action;
    private Double value;

    /** Constructs an AIActionWithValue for the given action, and computes its initial value. */
    private AIActionWithValue(AIAction action) {
      this.action = action;
      value = null;
    }

    /** Returns the AIAction in this. */
    private AIAction getAction() {
      return action;
    }

    /** Returns the value of this AIAction. */
    private double getValue() {
      if (value == null) {
        recomputeValue();
      }
      return value;
    }

    /** Recomputes the value of this Action. */
    private void recomputeValue() {
      value = delegates.stream().mapToDouble(d -> d.getScore(action)).sum();
    }

    @Override
    public int compareTo(AIActionWithValue o) {
      return Double.compare(getValue(), o.getValue());
    }

    /** Determines equality by action alone. */
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof AIActionWithValue)) {
        return false;
      } else {
        return action.equals(((AIActionWithValue) o).action);
      }
    }

    /** Hashes by action alone. */
    @Override
    public int hashCode() {
      return action.hashCode();
    }
  }

  /** The current computed set of possible move actions, by moving unit. Recomputed as needed. */
  private final Map<MovingUnit, Set<AIActionWithValue>> possibleMoveActionsByUnit;

  /** The current computed set of possible attack actions, by combatant. Recomputed as needed. */
  private final Map<Combatant, Set<AIActionWithValue>> possibleAttackActionsByUnit;

  /** The current computed set of possible summon actions, by summoner. Recomputed as needed. */
  private final Map<Summoner, Set<AIActionWithValue>> possibleSummonActionsByUnit;

  /** Constructs a new DelegatingAIController, initially with an empty set of delegates. */
  public DelegatingAIController() {
    this.delegates = new HashSet<>();
    possibleMoveActionsByUnit = new HashMap<>();
    possibleAttackActionsByUnit = new HashMap<>();
    possibleSummonActionsByUnit = new HashMap<>();
  }

  /** Adds the given delegate and returns this. */
  public DelegatingAIController addDelegate(Delegate delegate) {
    delegates.add(delegate);
    return this;
  }

  /**
   * Starts the turn by recomputing all possible actions, since opponent change in state may have
   * disqualified any possible actions left from last turn.
   */
  @Override
  public void turnStart(Player player) {
    recomputeAllActions(player);
  }

  /** Recomputes all possible actions the player can take. */
  private void recomputeAllActions(Player player) {
    possibleAttackActionsByUnit.clear();
    possibleMoveActionsByUnit.clear();
    possibleSummonActionsByUnit.clear();
    for (MovingUnit m : player.getMovingUnits()) {
      recomputeMoveActionsForUnit(player, m);
    }
    for (Combatant c : player.getCombatants()) {
      recomputeAttackActionsForUnit(player, c);
    }
    for (Summoner s : player.getSummoners()) {
      recomputeSummonActionsForUnit(player, (Unit & Summoner) s);
    }
  }

  /** Recomputes the moving actions for the given unit. */
  private void recomputeMoveActionsForUnit(Player p, MovingUnit movingUnit) {
    HashSet<AIActionWithValue> actionWithValues = new HashSet<>();
    if (movingUnit.canMove()) {
      for (Tile t : p.game.board.getMovementCloud(movingUnit, false)) {
        if (t == movingUnit.getLocation() || (t.isOccupied() && t.getOccupyingUnit().owner == p)) {
          continue;
        }
        actionWithValues.add(
            new AIActionWithValue(
                AIAction.moveUnit(p, movingUnit, t, p.game.board.getMovementPath(t))));
      }
    }
    possibleMoveActionsByUnit.put(movingUnit, actionWithValues);
  }

  /** Recomputes the attacking actions for the given unit. */
  private void recomputeAttackActionsForUnit(Player p, Combatant combatant) {
    HashSet<AIActionWithValue> actionWithValues = new HashSet<>();
    if (combatant.canFight()) {
      for (Tile t : combatant.getAttackableTiles(true)) {
        actionWithValues.add(new AIActionWithValue(AIAction.attack(p, combatant, t)));
      }
    }
    possibleAttackActionsByUnit.put(combatant, actionWithValues);
  }

  /** Recomputes the summoning actions for the given unit. */
  private <U extends Unit & Summoner> void recomputeSummonActionsForUnit(Player p, U summonerUnit) {
    HashSet<AIActionWithValue> actionWithValues = new HashSet<>();
    if (summonerUnit.canSummon()) {
      for (Unit summonUnit : summonerUnit.getSummonables().values()) {
        for (Tile t : p.game.board.getSummonCloud(summonerUnit, summonUnit)) {
          if (t.isOccupied()) {
            continue;
          }
          actionWithValues.add(
              new AIActionWithValue(
                  AIAction.summonCombatantOrBuildBuilding(p, summonerUnit, t, summonUnit)));
        }
      }
      for (Unit summonUnit : summonerUnit.getBuildables().values()) {
        for (Tile t : p.game.board.getSummonCloud(summonerUnit, summonUnit)) {
          if (t.isOccupied()) {
            continue;
          }
          actionWithValues.add(
              new AIActionWithValue(
                  AIAction.summonCombatantOrBuildBuilding(p, summonerUnit, t, summonUnit)));
        }
      }
    }
    possibleSummonActionsByUnit.put(summonerUnit, actionWithValues);
  }

  /**
   * Returns the best action remaining with value > 0 as the next action. If no actions remaining or
   * all actions < 0, does nothing (returns null).
   */
  @Override
  public AIAction getNextAction(Player player) {
    // If game is over, don't try to execute another action.
    if (player.game.isGameOver()) {
      return null;
    }

    // Otherwise, get the best action. Execute if value is positive.
    AIActionWithValue action =
        Stream.<Stream<AIActionWithValue>>builder()
            .add(possibleAttackActionsByUnit.values().stream().flatMap(Set::stream))
            .add(possibleMoveActionsByUnit.values().stream().flatMap(Set::stream))
            .add(possibleSummonActionsByUnit.values().stream().flatMap(Set::stream))
            .build()
            .flatMap(Function.identity())
            .max(Comparator.naturalOrder())
            .orElse(null);
    return action != null && action.value > 0 ? action.getAction() : null;
  }

  /** Remove the action from the possible set and try again. */
  @Override
  public void actionFailed(Exception e, AIAction action) {
    e.printStackTrace();
    recomputeAllActions(action.player);
  }

  /**
   * Recompute all possible actions for the unit in question, since it likely can't do the things it
   * could before anymore.
   */
  @Override
  public void actionExecuted(AIAction action) {
    recomputeAllActions(action.player);
  }
}
