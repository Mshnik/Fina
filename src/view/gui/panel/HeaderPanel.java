package view.gui.panel;

import model.game.Game;
import model.game.Player;
import view.gui.Frame;
import view.gui.image.ImageIndex;
import view.gui.image.ImageIndex.DrawingBarSegment;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.List;

/**
 * The panel at the top of the frame that shows basic information and the current player's turn
 *
 * @author MPatashnik
 */
public final class HeaderPanel extends JPanel {

  /** */
  private static final long serialVersionUID = 1L;

  /** The Height of the HeaderPanel */
  private static final int HEIGHT = 50;

  /** The color of the border surrounding the headerPanel */
  private static final Color BORDER_COLOR = new Color(74, 47, 12);

  /** Distance (in pixels) between the top of the HeaderPanel and the top of the bars */
  private static final int MARGIN = 15;

  /** Width of the stroke used to make the bars */
  private static final int STROKE = 4;

  /** Extra width on sides of components. */
  private static final int X_MARGIN = 20;

  /** Color of text used to show mana and health bars */
  private static final Color TEXT_COLOR = Color.white;

  /** Color behind the bars if the bars aren't totally full */
  private static final Color BACK_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.2f);

  /** Increments drawn with thin black lines along bar */
  private static final int INCREMENT_VAL = 100;

  /** Color used to draw Increment lines - translucent black */
  private static final Color INCREMENT_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.5f);

  /** The border color for the health bar */
  private static final Color HEALTH_BORDER = new Color(153, 15, 0);

  /** The filled in color for the health bar */
  private static final Color HEALTH_FILL = new Color(242, 33, 10);

  /** The border color for the mana bar */
  private static final Color MANA_BORDER = new Color(0, 53, 145);

  /** The filled in color for the mana bar */
  private static final Color MANA_FILL = new Color(9, 93, 237);

  /** The filled in color for the manaPerTurn cap in the mana bar */
  private static final Color MANA_PER_TURN_FILL = new Color(9, 169, 237);

  /** Color for level text */
  private static final Color LEVEL = Color.WHITE;

  /** The border color for the exp bar */
  private static final Color EXP_BORDER = new Color(135, 121, 17);

  /** The filled in color for the exp bar */
  private static final Color EXP_FILL = new Color(208, 189, 26);

  /** The filled in color for the exp per turn cap in the exp bar */
  private static final Color EXP_PER_TURN_FILL = new Color(255, 237, 43);

  /** The "max" mana (highest mana value seen thus far for the given players) */
  private HashMap<Player, Integer> maxMana;

  /** The frame this belongs to */
  public final Frame frame;

  public HeaderPanel(Frame f) {
    frame = f;
    maxMana = new HashMap<>();
    GamePanel gp = f.getGamePanel();
    setPreferredSize(new Dimension(gp.getShowedCols() * frame.getGamePanel().cellSize(), HEIGHT));
  }

  /** Draws the HeaderPanel */
  @Override
  public void paintComponent(Graphics g) {
    Game game = frame.getController().game;
    // Draw background and border
    Graphics2D g2d = (Graphics2D) g;
    if (getHeight() == 0) return;
    for (int i = 0; i <= getWidth(); i += getHeight()) {
      g2d.drawImage(ImageIndex.SANDSTONE, i, 0, getHeight(), getHeight(), null);
    }
    g2d.setColor(BORDER_COLOR);
    int width = 6;
    g2d.setStroke(new BasicStroke(width));
    g2d.drawRect(width / 2, width / 2, getWidth() - width, getHeight() - width);

    if (frame.DEBUG) {
      g2d.setColor(Color.black);
      g2d.drawString(frame.getController().getToggle().toString(), MARGIN, MARGIN);
    }
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, 20));
    if (game.getCurrentPlayer() == null) {
      g2d.drawString("Setup Phase", 20, 25);
    } else if (game.isGameOver()) {
      g2d.drawString("Player " + game.getRemainingPlayers().get(0).index + " wins", 20, 25);
    } else {
      Player p = game.getCurrentPlayer();

      String playerString = p.getCommander().name + " (p" + (game.getPlayerIndex() + 1) + ")";
      g2d.drawString(playerString, X_MARGIN, 32);
      int barStartX = X_MARGIN * 3 + g2d.getFontMetrics().stringWidth(playerString);

      int allBarsWidth = getWidth() - barStartX;
      int barWidth = allBarsWidth / 3 - (X_MARGIN * 2);

      Font f = new Font(Frame.FONTNAME, Font.BOLD, 10);

      // Health bar
      drawBar(
          g2d,
          barStartX,
          barWidth,
          HEALTH_BORDER,
          p.getMaxHealth(),
          DrawingBarSegment.listOf(HEALTH_FILL, (double) p.getHealth() / (double) p.getMaxHealth()),
          p.getHealth() + "/" + p.getMaxHealth(),
          f);
      barStartX += barWidth + X_MARGIN;

      // Update "max" mana for this player, if necessary
      if (!maxMana.containsKey(p) || p.getMana() + p.getManaPerTurn() > maxMana.get(p)) {
        maxMana.put(p, p.getMana() + p.getManaPerTurn());
      }

      // Mana bar.
      drawBar(
          g2d,
          barStartX,
          barWidth,
          MANA_BORDER,
          maxMana.get(p),
          DrawingBarSegment.listOf(
              MANA_FILL,
              (double) p.getMana() / (double) maxMana.get(p),
              MANA_PER_TURN_FILL,
              Math.min(
                  (double) (maxMana.get(p) - p.getMana()) / (double) maxMana.get(p),
                  (double) p.getManaPerTurn() / (double) maxMana.get(p))),
          p.getMana() + " (" + (p.getManaPerTurn() >= 0 ? "+" : "-") + p.getManaPerTurn() + ")",
          f);

      // research bar
      barStartX += barWidth + X_MARGIN;

      final int LEVEL_SPACE = 40;
      final int LEVEL_TOP = 33;
      final int LEVEL_START = barStartX + LEVEL_SPACE;
      drawBar(
          g2d,
          LEVEL_START,
          barWidth,
          EXP_BORDER,
          p.getResearchRequirement(),
          DrawingBarSegment.listOf(
              EXP_FILL,
              (double) p.getResearch() / p.getResearchRequirement(),
              EXP_PER_TURN_FILL,
              Math.min(p.getResearchPerTurn(), p.getResearchRemaining())
                  / (double) p.getResearchRequirement()),
          p.getResearch()
              + (p.getResearchPerTurn() > 0 ? " ( +" + p.getResearchPerTurn() + ") " : "")
              + "/"
              + p.getResearchRequirement(),
          f);

      // Draw level on top of bar
      g2d.setColor(EXP_BORDER);
      g2d.fillRect(
          LEVEL_START - LEVEL_SPACE + 5,
          MARGIN - STROKE / 2,
          LEVEL_SPACE - 5,
          HEIGHT - MARGIN * 2 + STROKE);
      g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, 25));
      g2d.setColor(LEVEL);
      g2d.drawString(
          "" + p.getCommander().getLevel(), LEVEL_START + MARGIN - LEVEL_SPACE, LEVEL_TOP);
    }
  }

  /**
   * Draws a bar with the border and fill colors, full the given amount
   *
   * @param g2d - a graphics object to draw with
   * @param X - the x coordinate of the top left corner
   * @param width - the width of the bar to draw.
   * @param border - the color with which to draw the border.
   * @param segments - the segments to fill.
   * @param maxVal - the value the full bar currently corresponds to
   * @param text - the text to draw.
   */
  private void drawBar(
      Graphics2D g2d,
      final int X,
      final int width,
      Color border,
      int maxVal,
      List<DrawingBarSegment> segments,
      String text,
      Font f) {
    ImageIndex.drawBar(
        g2d,
        X,
        MARGIN,
        width,
        HEIGHT - MARGIN * 2,
        BACK_COLOR,
        border,
        STROKE,
        maxVal,
        segments,
        text,
        TEXT_COLOR,
        f,
        INCREMENT_COLOR,
        INCREMENT_VAL);
  }
}
