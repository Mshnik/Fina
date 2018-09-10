package view.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.combatant.Combatant;

/**
 * View options for a given player. Used to customize the UI while persisting through turns. A
 * player can only alter / see their own options.
 */
public final class ViewOptions {
  /** The Frame this is drawing view options for. * */
  private final Frame frame;

  /** The index of the player these ViewOptions correspond to. */
  private final Player player;

  /**
   * Enemy units to paint the danger radius for. Units no longer on the board will be automatically
   * cleared from this set.
   */
  private final Set<Combatant> paintDangerRadiusUnits;

  /** The most recent computed danger radius. Set to null when this needs a recompute. */
  private Set<Tile> dangerRadius;

  /** Constructs a new ViewOptions for the given player. */
  ViewOptions(Frame frame, Player player) {
    this.frame = frame;
    this.player = player;
    paintDangerRadiusUnits = new HashSet<>();
    dangerRadius = null;
  }

  /** Clears all units from the paintDangerRadiusUnits. */
  void clearDangerRadiusUnits() {
    paintDangerRadiusUnits.clear();
    dangerRadius = null;
  }

  /** Adds the given unit to the dangerRadius if it isn't present, removes it if it is present. */
  public void toggleDangerRadiusUnit(Combatant c) {
    if (!paintDangerRadiusUnits.contains(c)) {
      paintDangerRadiusUnits.add(c);
    } else {
      paintDangerRadiusUnits.remove(c);
    }
    dangerRadius = null;
  }

  /** Remove all dead units from the danger radius. */
  private void cleanupDangerRadiusUnits() {
    boolean elementsRemoved = paintDangerRadiusUnits.removeIf(c -> !c.isAlive());
    if (elementsRemoved) {
      dangerRadius = null;
    }
  }

  /** Called when a player's danger radius changes. Always recompute danger Radius here. */
  void unitDangerRadiusChanged() {
    dangerRadius = null;
  }

  /**
   * Called when a unit's danger radius changes. If this contains the given unit, set to recalculate
   * danger radius.
   */
  void unitDangerRadiusChanged(Unit u) {
    if (paintDangerRadiusUnits.contains(u)) {
      dangerRadius = null;
    }
  }

  /** A pair of a unit and its max move cloud. */
  private static final class UnitToTilePair {
    private final Combatant unit;
    private final Tile tile;

    private UnitToTilePair(Combatant unit, Tile tile) {
      this.unit = unit;
      this.tile = tile;
    }
  }

  /** Returns true if the danger radius has at least one tile in it. */
  public boolean hasNonEmptyDangerRadius() {
    return !getDangerRadius().isEmpty();
  }

  /**
   * Returns the tile(s) threatened by the units in paintDangerRadiusUnits. May be empty if that set
   * is empty. Causes a cleanup that removes dead units.
   */
  public Set<Tile> getDangerRadius() {
    cleanupDangerRadiusUnits();
    if (dangerRadius != null) {
      return dangerRadius;
    }
    dangerRadius =
        paintDangerRadiusUnits
            .stream()
            .filter(player::canSee)
            .filter(unit -> unit.owner != player || unit.canFight())
            .flatMap(
                unit ->
                    frame
                        .getController()
                        .game
                        .board
                        .getMovementCloud(unit, unit.owner != player)
                        .stream()
                        .map(tile -> new UnitToTilePair(unit, tile)))
            .flatMap(
                unitToTilePair ->
                    unitToTilePair.unit.getAttackableTilesFrom(unitToTilePair.tile).stream())
            .collect(Collectors.toSet());
    return dangerRadius;
  }
}
