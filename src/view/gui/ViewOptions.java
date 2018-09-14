package view.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import view.gui.modifier.AnimatedModifierIcon;
import view.gui.modifier.ModifierIcon;
import view.gui.modifier.ModifierIcon.FilterType;
import view.gui.modifier.RowModifierIcon;
import view.gui.panel.GamePanel;

/**
 * View options for a given player. Used to customize the UI while persisting through turns. A
 * player can only alter / see their own options.
 */
public final class ViewOptions {
  /** The Frame this is drawing view options for. * */
  private final Frame frame;

  /** The index of the player these ViewOptions correspond to. */
  private final Player player;

  /** ModifierIcon types for modifier icons on units. */
  public enum ModifierIconType {
    ANIMATED(AnimatedModifierIcon::new),
    ROW(RowModifierIcon::new);

    private final BiFunction<GamePanel, Unit, ModifierIcon> constructor;

    ModifierIconType(BiFunction<GamePanel, Unit, ModifierIcon> constructor) {
      this.constructor = constructor;
    }
  }

  /** The modifierIcon type for this player. */
  private ModifierIconType modifierIconType;

  /** The FilterType on ModifierIcons to paint for this player. */
  private FilterType modifierIconsFilterType;

  /** View types for modifiers icons on units. */
  public enum ModifierViewType {
    /** Show modifiers only on the unit the cursor is on. */
    CURSOR_ONLY,
    /** Show modifiers on all units. */
    VIEW_ALL;
  }

  /** The ModifierViewType to paint for this player. */
  private ModifierViewType modifierIconViewType;

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
    modifierIconType = ModifierIconType.ANIMATED;
    modifierIconsFilterType = FilterType.ONLY_NON_INFINITE_VISIBLE;
    modifierIconViewType = ModifierViewType.CURSOR_ONLY;
    dangerRadius = null;
  }

  /** Creates a ModifierIcon for the given unit and returns it. */
  public ModifierIcon createModifierIconFor(GamePanel gamePanel, Unit u) {
    ModifierIcon modifierIcon = modifierIconType.constructor.apply(gamePanel, u);
    modifierIcon.setFilterType(modifierIconsFilterType);
    return modifierIcon;
  }

  /** Returns the current ModifierIcon ViewType. */
  public ModifierIconType getModifierIconType() {
    return modifierIconType;
  }

  /** Cycles to the next type. */
  public void cycleModifierIconType() {
    modifierIconType =
        ModifierIconType.values()[
            (modifierIconType.ordinal() + 1) % ModifierIconType.values().length];
  }

  /** Returns the current ModifierIcon FilterType. */
  public FilterType getModifierIconsFilterType() {
    return modifierIconsFilterType;
  }

  /** Cycles to the next display type. */
  public void cycleModifierIconsDisplayType() {
    modifierIconsFilterType =
        FilterType.values()[(modifierIconsFilterType.ordinal() + 1) % FilterType.values().length];
  }

  /** Returns the current ModifierIcon ViewType. */
  public ModifierViewType getModifierIconsViewType() {
    return modifierIconViewType;
  }

  /** Cycles to the next view type. */
  public void cycleModifierIconsViewType() {
    modifierIconViewType =
        ModifierViewType.values()[
            (modifierIconViewType.ordinal() + 1) % ModifierViewType.values().length];
  }

  /** Clears all units from the paintDangerRadiusUnits. */
  public void clearDangerRadiusUnits() {
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
