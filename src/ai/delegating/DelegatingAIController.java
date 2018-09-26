package ai.delegating;

import ai.AIAction;
import ai.AIController;
import ai.delegates.ByNameDelegate;
import ai.delegates.Delegate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import model.board.Board;
import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;

/** An AI controller that maintains a set of delegates to determine its behavior. */
public final class DelegatingAIController implements AIController {

  /** The id of this AIController. May be empty to delegate setting to player. */
  private final String id;

  /** The delegates this is delegating to. */
  private final List<Delegate> delegates;

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

  /**
   * The current computed set of possible cast actions. All are for commander. Recomputed as needed.
   */
  private final Set<AIActionWithValue> possibleSpellsToCast;

  /**
   * Constructs a new DelegatingAIController, initially with an empty set of delegates. The id is
   * left empty, thus the player will set it on its own.
   */
  DelegatingAIController() {
    this("");
  }

  /**
   * Constructs a new DelegatingAIController, initially with an empty set of delegates and the given
   * id.
   */
  DelegatingAIController(String id) {
    this.id = id;
    this.delegates = new ArrayList<>();
    possibleMoveActionsByUnit = new HashMap<>();
    possibleAttackActionsByUnit = new HashMap<>();
    possibleSummonActionsByUnit = new HashMap<>();
    possibleSpellsToCast = new HashSet<>();
  }

  @Override
  public String id() {
    return id;
  }

  /** Adds the given delegate and returns this. */
  void addDelegate(Delegate delegate) {
    delegates.add(delegate);
  }

  /** Returns the delegates in this controller. */
  public List<Delegate> getDelegates() {
    return Collections.unmodifiableList(delegates);
  }

  /**
   * Returns the delegate by Class. Throws a runtime exception if the given delegate class isn't
   * present in delegates.
   */
  Delegate getDelegate(Class<? extends Delegate> delClass) {
    return delegates
        .stream()
        .filter(delClass::isInstance)
        .findAny()
        .orElseThrow(() -> new RuntimeException("No matching delegate to " + delClass));
  }

  /**
   * Returns the ByNameDelegate by Class. Throws a runtime exception if the given delegate class
   * isn't present in delegates.
   */
  ByNameDelegate getByNameDelegate(Class<? extends ByNameDelegate> delClass) {
    return delegates
        .stream()
        .filter(delClass::isInstance)
        .findAny()
        .map(d -> (ByNameDelegate) d)
        .orElseThrow(() -> new RuntimeException("No matching delegate to " + delClass));
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
    recomputeSpellsForCommander(player, player.getCommander());
  }

  /** Recomputes the moving actions for the given unit. */
  private void recomputeMoveActionsForUnit(Player p, MovingUnit movingUnit) {
    HashSet<AIActionWithValue> actionWithValues = new HashSet<>();
    if (movingUnit.canMove()) {
      Board board = p.game.board;
      List<Tile> cloud = board.getMovementCloud(movingUnit, false);
      cloud.remove(movingUnit.getLocation());
      int pathComputationId = board.getPathComputationId();
      for (Tile t : cloud) {
        List<Tile> movementPath = board.getMovementPath(pathComputationId, t);
        if (t == movingUnit.getLocation()
            // TODO - this allows the AI to cheat by not moving onto a tile that's occupied,
            // even if it can't see it. Needs to be fixed up.
            || t.isOccupied()
            || movementPath
                .stream()
                .anyMatch(tile -> tile.isOccupied() && tile.getOccupyingUnit().owner != p)) {
          continue;
        }
        actionWithValues.add(
            new AIActionWithValue(AIAction.moveUnit(p, movingUnit, t, movementPath)));
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
        if (p.getMana() >= summonUnit.getManaCostWithScalingAndDiscountsForPlayer(p)) {
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
      for (Unit summonUnit : summonerUnit.getBuildables().values()) {
        if (p.getMana() >= summonUnit.getManaCostWithScalingAndDiscountsForPlayer(p)) {
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
    }
    possibleSummonActionsByUnit.put(summonerUnit, actionWithValues);
  }

  /** Recomputes the casting actions for the given commander. */
  private void recomputeSpellsForCommander(Player p, Commander commander) {
    possibleSpellsToCast.clear();
    if (commander.canCast()) {
      for (Ability ability : commander.getCastables().values()) {
        if (p.getMana() >= ability.getManaCostWithDiscountsForPlayer(p)) {
          for (Tile t : p.game.board.getCastCloud(commander, ability)) {
            possibleSpellsToCast.add(
                new AIActionWithValue(AIAction.cast(p, commander, t, ability)));
          }
        }
      }
    }
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
            .add(possibleSpellsToCast.stream())
            .build()
            .flatMap(Function.identity())
            .max(Comparator.naturalOrder())
            .orElse(null);
    return action != null && action.getValue() > 0 ? action.getAction() : null;
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

  /** Creates a config string from the delegates used and their weights. */
  @Override
  public String getConfigString() {
    int weightSigFigs = 4;
    return delegates
        .stream()
        .map(
            d ->
                String.format(
                    "%s %." + weightSigFigs + "f [%s]",
                    d.getClass().getSimpleName(),
                    d.getWeight(),
                    Arrays.stream(d.getSubWeights())
                        .mapToObj(w -> String.format("%." + weightSigFigs + "f", w))
                        .collect(Collectors.joining(","))))
        .collect(Collectors.joining(", "));
  }
}
