package view.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import model.board.Tile;
import model.unit.combatant.Combatant;

/**
 * View options for a given player. Used to customize the UI while persisting through turns. A
 * player can only alter / see their own options.
 */
public final class ViewOptions {
  /** The Frame this is drawing view options for. * */
  private final Frame frame;

  /** The index of the player these ViewOptions correspond to. */
  private final int playerIndex;

  /**
   * Enemy units to paint the danger radius for. Units no longer on the board will be automatically
   * cleared from this set.
   */
  private final Set<Combatant> paintDangerRadiusUnits;

  /** Constructs a new ViewOptions for the given player. */
  ViewOptions(Frame frame, int playerIndex) {
    this.frame = frame;
    this.playerIndex = playerIndex;
    paintDangerRadiusUnits = new HashSet<>();
  }

  /** Clears all units from the paintDangerRadiusUnits. */
  void clearDangerRadiusUnits() {
    paintDangerRadiusUnits.clear();
  }

  /** Adds the given unit to the dangerRadius. */
  void addDangerRadiusUnit(Combatant c) {
    paintDangerRadiusUnits.add(c);
  }

  /** Remove all dead units from the danger radius. */
  private void cleanupDangerRadiusUnits() {
    paintDangerRadiusUnits.removeIf(c -> !c.isAlive());
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

  /**
   * Returns the tile(s) threatened by the units in paintDangerRadiusUnits. May be empty if that set
   * is empty. Causes a cleanup that removes dead units.
   */
  public Set<Tile> getDangerRadius() {
    return paintDangerRadiusUnits
        .stream()
        .flatMap(
            unit ->
                frame
                    .getController()
                    .game
                    .board
                    .getMovementCloud(unit, true)
                    .stream()
                    .map(tile -> new UnitToTilePair(unit, tile)))
        .flatMap(
            unitToTilePair ->
                unitToTilePair.unit.getAttackableTilesFrom(unitToTilePair.tile).stream())
        .collect(Collectors.toSet());
  }
}
