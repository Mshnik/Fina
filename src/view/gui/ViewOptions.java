package view.gui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import model.board.Tile;
import model.unit.combatant.Combatant;

/**
 * View options for a given player. Used to customize the UI while persisting through turns. A
 * player can only alter / see their own options.
 */
public final class ViewOptions {
  /** The index of the player these ViewOptions correspond to. */
  private final int playerIndex;

  /**
   * Enemy units to paint the danger radius for. Units no longer on the board will be automatically
   * cleared from this set.
   */
  private final Set<Combatant> paintDangerRadiusUnits;

  /** Constructs a new ViewOptions for the given player. */
  ViewOptions(int playerIndex) {
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

  /**
   * Returns the tile(s) threatened by the units in paintDangerRadiusUnits. May be empty if that set
   * is empty. Causes a cleanup that removes dead units.
   */
  Set<Tile> getDangerRadius() {
    return Collections.emptySet();
  }
}
