package view.gui.image;

import java.awt.Color;

/**
 * Holder for color constants.
 */
public final class Colors {
  /**
   * Shading for fog of war - translucent black
   */
  public static final Color FOG_OF_WAR = new Color(0, 0, 0, 0.75f);
  /**
   * Color of summon/build radii
   */
  public static final Color SUMMON_FILL_COLOR = new Color(154, 222, 195);
  /**
   * Color of summon/build radii
   */
  public static final Color SUMMON_BORDER_COLOR = new Color(65, 222, 173);
  /**
   * Color of cast radii
   */
  public static final Color CAST_BORDER_COLOR = new Color(244, 231, 35);
  /**
   * Color of cast radii
   */
  public static final Color CAST_FILL_COLOR = new Color(0.96f, 0.89f, 0.11f, 0.5f);
  /**
   * The default color for shading possible selectable locations in a LocationSelector. A
   * translucent white.
   */
  public static final Color DEFAULT_CLOUD_FILL_COLOR = new Color(1, 1, 1, 0.5f);

  /**
   * The default color for shading possible selectable locations in a LocationSelector. A
   * less translucent white.
   */
  public static final Color DEFAULT_CLOUD_TRACE_COLOR = new Color(1, 1, 1, 0.75f);
  /**
   * The default color for shading cloud locations in a LocationSelector. A
   * translucent grey.
   */
  public static final Color DEFAULT_EFFECT_FILL_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f);
  /**
   * The default color for shading cloud locations in a LocationSelector. A
   * less translucent grey.
   */
  public static final Color DEFAULT_EFFECT_TRACE_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.75f);
  /**
   * Color to shade area an attack selector is on.
   */
  public static final Color ATTACK_FILL_COLOR = new Color(1.0f, 0f, 0f, 0.5f);
  /**
   * Color of attack radii
   */
  public static final Color ATTACK_BORDER_COLOR = Color.red;

  private Colors() {
  }
}
