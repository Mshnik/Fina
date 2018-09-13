package view.gui.modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.unit.Unit;
import model.unit.modifier.Modifier;
import model.unit.modifier.Modifiers;
import model.unit.modifier.Modifiers.ModifierDescription;
import view.gui.Animatable;
import view.gui.panel.GamePanel;

/**
 * Represents a icon on an unit image that in some way represents the modifiers on that unit. May be
 * animatable. If not, can return 0 / default value for all methods. May have zero modifiers, in
 * which case should paint nothing.
 */
public abstract class ModifierIcon implements Animatable {

  /**
   * The unit this ModifierIcon is painting modifiers for. Should be a unit on the board, not in a
   * menu.
   */
  final Unit unit;

  /**
   * The modifier descriptions this should paint. Will be recomputed whenever the modifiers on the
   * unit changes.
   */
  private List<ModifierDescription> modifierDescriptions;

  /** Filter types for what subset of modifiers to show for a ModifierIcon. */
  public enum FilterType {
    /** Show no modifiers. */
    NONE,
    /** Show all visible modifiers. Default */
    ALL_VISIBLE,
    /** Show only visible modifiers that have non-infinite duration. */
    ONLY_NON_INFINITE_VISIBLE
  }

  /** The current filterType type for this ModifierIcon. */
  private FilterType filterType;

  /** The gamePanel this is drawing for. Reference kept so repaint events can occur. */
  final GamePanel gamePanel;

  /** Constructs a new ModifierIcon. */
  ModifierIcon(GamePanel gamePanel, Unit unit) {
    this.gamePanel = gamePanel;
    this.unit = unit;
    this.modifierDescriptions = new ArrayList<>();
    this.filterType = FilterType.ALL_VISIBLE;

    refreshModifiers();
  }

  /**
   * Refreshes this ModifierIcon. Should be called whenever the modifiers on the unit may have
   * changed.
   */
  public void refreshModifiers() {
    List<Modifier> modifiers;
    switch (filterType) {
      case NONE:
        modifiers = Collections.emptyList();
        break;
      case ALL_VISIBLE:
        modifiers = unit.getVisibleModifiers();
        break;
      case ONLY_NON_INFINITE_VISIBLE:
        modifiers = unit.getVisibleTemporaryModifiers();
        break;
      default:
        throw new RuntimeException("Unexpected filterType " + filterType);
    }
    modifierDescriptions = Modifiers.getModifierDescriptions(modifiers);
    if (hasModifiers()) {
      setState(0);
    }
  }

  /** Sets the filterType for this ModifierIcon. Also causes a refresh if this was a change. */
  public void setFilterType(FilterType filterType) {
    if (this.filterType != filterType) {
      this.filterType = filterType;
      refreshModifiers();
    }
  }

  /** Returns true iff this has at least one modifier. */
  boolean hasModifiers() {
    return !modifierDescriptions.isEmpty();
  }

  /** Returns the most recently computed modifier descriptions. */
  List<ModifierDescription> getModifiers() {
    return modifierDescriptions;
  }

  @Override
  public int getStateLength() {
    return 1050;
  }

  @Override
  public boolean isActive() {
    return modifierDescriptions.size() > 1;
  }
}
