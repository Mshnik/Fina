package view.gui.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import model.board.Direction;
import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.building.Building;
import model.unit.combatant.Combatant;
import model.unit.combatant.Combatant.CombatantClass;
import model.unit.commander.Commander;
import view.gui.panel.GamePanel;

/** Library for lookup of different image resources. Also some drawing functionality */
public final class ImageIndex {

  /** Prevent instantiation of ImageIndex */
  private ImageIndex() {}

  /** Root Location of image files */
  private static final String IMAGE_ROOT = "img/";

  /** Location of terrain files within image root */
  private static final String TERRAIN_IMAGE_ROOT = "terrain/";

  /** Location of class icons within image root */
  private static final String CLASS_ICONS_ROOT = "icons/weapons/";

  /** Image root for commanders within image root */
  private static final String COMMANDER_IMAGE_ROOT = "unit/";

  /** Image root for combatants within image root */
  private static final String COMBATANT_IMAGE_ROOT = "unit/fireemblem/";

  /** Image root for Building images within image root */
  private static final String BUILDING_IMAGE_ROOT = "building/";

  // TERRAIN

  /** The image for margin outside of the board */
  private static BufferedImage MARGIN;
  /** The image for grassy terrain */
  private static BufferedImage GRASS;
  /** The image for mountain terrain */
  private static BufferedImage MOUNTAINS;
  /** The image for woods terrain */
  private static BufferedImage WOODS;
  /** The image for ancient ground terrain */
  private static BufferedImage ANCIENT_GROUND;
  /** The image for sandstone texture */
  public static BufferedImage SANDSTONE;

  // ICONS

  /** The image for the Fighter icon. */
  private static BufferedImage FIGHTER_ICON;
  /** The image for the Assassin icon. */
  private static BufferedImage ASSASSIN_ICON;
  /** The image for the Mage icon. */
  private static BufferedImage MAGE_ICON;
  /** The image for the Ranger icon. */
  private static BufferedImage RANGER_ICON;
  /** The image for the Tank icon. */
  private static BufferedImage TANK_ICON;

  /** Read in units thus far */
  private static HashMap<String, BufferedImage> readUnits;

  /** Tinted units thus far */
  private static HashMap<BufferedImage, HashMap<Color, BufferedImage>> tintedUnits;

  /* Static initializer for the Image Class - do all image reading here */
  static {
    try {
      // Assorted other
      SANDSTONE = ImageIO.read(new File(IMAGE_ROOT + "sandstone.jpg"));

      // Terrain
      MARGIN = ImageIO.read(new File(IMAGE_ROOT + TERRAIN_IMAGE_ROOT + "margin.jpg"));
      GRASS = ImageIO.read(new File(IMAGE_ROOT + TERRAIN_IMAGE_ROOT + "grass.png"));
      MOUNTAINS = ImageIO.read(new File(IMAGE_ROOT + TERRAIN_IMAGE_ROOT + "mountain.png"));
      WOODS = ImageIO.read(new File(IMAGE_ROOT + TERRAIN_IMAGE_ROOT + "woods.png"));
      ANCIENT_GROUND = ImageIO.read(new File(IMAGE_ROOT + TERRAIN_IMAGE_ROOT + "gold.jpg"));

      // Class Icons
      FIGHTER_ICON = ImageIO.read(new File(IMAGE_ROOT + CLASS_ICONS_ROOT + "weapon_icon_1_0.png"));
      ASSASSIN_ICON = ImageIO.read(new File(IMAGE_ROOT + CLASS_ICONS_ROOT + "weapon_icon_0_0.png"));
      MAGE_ICON = ImageIO.read(new File(IMAGE_ROOT + CLASS_ICONS_ROOT + "weapon_icon_5_0.png"));
      RANGER_ICON = ImageIO.read(new File(IMAGE_ROOT + CLASS_ICONS_ROOT + "weapon_icon_6_0.png"));
      TANK_ICON = ImageIO.read(new File(IMAGE_ROOT + CLASS_ICONS_ROOT + "weapon_icon_9_0.png"));

      // Units
      readUnits = new HashMap<>();
      tintedUnits = new HashMap<>();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Returns the margin image file for painting outside of the board. */
  public static BufferedImage margin() {
    return MARGIN;
  }

  /** Returns the image file corresponding to the given terrain type */
  public static BufferedImage imageForTerrain(Terrain t) {
    switch (t) {
      case GRASS:
        return GRASS;
      case MOUNTAIN:
        return MOUNTAINS;
      case WOODS:
        return WOODS;
      case ANCIENT_GROUND:
        return ANCIENT_GROUND;
      default:
        throw new RuntimeException("Unsupported terrain: " + t);
    }
  }

  /** Returns the image file for the corresponding combatant class. */
  public static BufferedImage imageForCombatantClass(CombatantClass combatantClass) {
    switch (combatantClass) {
      case FIGHTER:
        return FIGHTER_ICON;
      case RANGER:
        return RANGER_ICON;
      case ASSASSIN:
        return ASSASSIN_ICON;
      case TANK:
        return TANK_ICON;
      case MAGE:
        return MAGE_ICON;
      default:
        throw new RuntimeException("Unsupported combat class: " + combatantClass);
    }
  }

  /** Returns the key for the given unit in the readUnits map. */
  private static String getImageKey(Unit unit, Player activePlayer) {
    return unit.getImgFilename()
        + "-"
        + (unit.owner == null ? activePlayer.index : unit.owner.index);
  }

  /** Returns the image file corresponding to the given model.unit */
  public static BufferedImage imageForUnit(Unit unit, Player activePlayer) {
    String imageKey = getImageKey(unit, activePlayer);
    if (readUnits.containsKey(imageKey)) {
      return readUnits.get(imageKey);
    }

    BufferedImage u;
    try {
      String root = IMAGE_ROOT;
      if (unit instanceof Commander) root += COMMANDER_IMAGE_ROOT;
      else if (unit instanceof Combatant) {
        root += COMBATANT_IMAGE_ROOT;
        if (unit.owner != null) {
          root += unit.owner.getColor().toString().toLowerCase() + "/";
        } else {
          root += activePlayer.getColor().toString().toLowerCase() + "/";
        }
      } else if (unit instanceof Building) {
        root += BUILDING_IMAGE_ROOT;
        if (unit.owner != null) {
          root += unit.owner.getColor().toString().toLowerCase() + "/";
        } else {
          root += activePlayer.getColor().toString().toLowerCase() + "/";
        }
      }
      u = ImageIO.read(new File(root + unit.getImgFilename()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    readUnits.put(imageKey, u);
    return u;
  }

  /**
   * Tints the given image with the given color.
   *
   * @param loadImg - the image to paint and tint
   * @param color - the color to tint. Alpha value of input color isn't used.
   * @return A tinted version of loadImg
   */
  public static BufferedImage tint(BufferedImage loadImg, Color color) {
    if (tintedUnits.containsKey(loadImg) && tintedUnits.get(loadImg).containsKey(color))
      return tintedUnits.get(loadImg).get(color);

    BufferedImage img =
        new BufferedImage(loadImg.getWidth(), loadImg.getHeight(), BufferedImage.TRANSLUCENT);
    final float tintOpacity = 0.45f;
    Graphics2D g2d = img.createGraphics();

    // Draw the base image
    g2d.drawImage(loadImg, null, 0, 0);
    // Set the color to a transparent version of the input color
    g2d.setColor(
        new Color(
            color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, tintOpacity));

    // Iterate over every pixel, if it isn't transparent paint over it
    Raster data = loadImg.getData();
    for (int x = data.getMinX(); x < data.getWidth(); x++) {
      for (int y = data.getMinY(); y < data.getHeight(); y++) {
        int[] pixel = data.getPixel(x, y, new int[4]);
        if (pixel[3] > 0) { // If pixel isn't full alpha. Could also be pixel[3]==255
          g2d.fillRect(x, y, 1, 1);
        }
      }
    }
    g2d.dispose();

    if (!tintedUnits.containsKey(loadImg))
      tintedUnits.put(loadImg, new HashMap<Color, BufferedImage>());
    tintedUnits.get(loadImg).put(color, img);
    return img;
  }

  /** Draws a border around the set of tiles. */
  public static void trace(Collection<Tile> tiles, GamePanel gp, Graphics2D g2d) {
    for (Tile t : tiles) {
      for (Direction d : Direction.values()) {
        boolean paint = true;
        try {
          Tile n = t.board.getTileAt(t.row + d.dRow(), t.col + d.dCol());
          paint = !tiles.contains(n);
        } catch (IllegalArgumentException e) {
        }
        if (paint) drawLine(g2d, gp, t, d);
      }
    }
  }

  /** Fills the given tiles. */
  public static void fill(Collection<Tile> tiles, GamePanel gp, Graphics2D g2d) {
    for (Tile t : tiles) {
      g2d.fillRect(gp.getXPosition(t), gp.getYPosition(t), gp.cellSize(), gp.cellSize());
    }
  }

  private static void drawLine(Graphics2D g2d, GamePanel gp, Tile t, Direction side) {
    switch (side) {
      case UP:
        g2d.drawLine(
            gp.getXPosition(t),
            gp.getYPosition(t),
            gp.getXPosition(t) + gp.cellSize(),
            gp.getYPosition(t));
        break;
      case RIGHT:
        g2d.drawLine(
            gp.getXPosition(t) + gp.cellSize(),
            gp.getYPosition(t),
            gp.getXPosition(t) + gp.cellSize(),
            gp.getYPosition(t) + gp.cellSize());
        break;
      case DOWN:
        g2d.drawLine(
            gp.getXPosition(t),
            gp.getYPosition(t) + gp.cellSize(),
            gp.getXPosition(t) + gp.cellSize(),
            gp.getYPosition(t) + gp.cellSize());
        break;
      case LEFT:
        g2d.drawLine(
            gp.getXPosition(t),
            gp.getYPosition(t),
            gp.getXPosition(t),
            gp.getYPosition(t) + gp.cellSize());
        break;
    }
  }

  /**
   * A segment for using in {@link #drawBar(Graphics2D, int, int, int, int, Color, Color, int, int,
   * List, String, Color, Font, Color, int)}.
   */
  public static final class DrawingBarSegment {
    private final Color color;
    private final double percentFull;

    private DrawingBarSegment(Color color, double percentFull) {
      this.color = color;
      this.percentFull = percentFull;
    }

    private double getPercentFull() {
      return percentFull;
    }

    /** Returns a list of one drawing bar segment. */
    public static List<DrawingBarSegment> listOf(Color color1, double percentFull1) {
      ArrayList<DrawingBarSegment> list = new ArrayList<>();
      list.add(new DrawingBarSegment(color1, percentFull1));
      return list;
    }

    /** Returns a list of two drawing bar segments. */
    public static List<DrawingBarSegment> listOf(
        Color color1, double percentFull1, Color color2, double percentFull2) {
      ArrayList<DrawingBarSegment> list = new ArrayList<>();
      list.add(new DrawingBarSegment(color1, percentFull1));
      list.add(new DrawingBarSegment(color2, percentFull2));
      return list;
    }
  }

  /**
   * Draws a bar with the border and fill colors, full the given amount, etc etc etc.
   *
   * @param g2d - the graphics object to use for drawing. Can't be null
   * @param X - the x coordinate of the top left corner of the bar
   * @param Y - the y coordinate of the top left corner of the bar
   * @param BAR_WIDTH - the width of the bar
   * @param BAR_HEIGHT - the height of the bar
   * @param backColor - the color to draw behind the bar for any unfilled portion. can be null
   * @param borderColor - the color to draw the border of the bar. If strokeWidth is 0, this is
   *     unused
   * @param strokeWidth - the width of the border portion of the bar. Can't be negative.
   * @param segments - the color and percentages of the fill portion of the bar. Total percent
   *     should be in [0,1].
   * @param maxVal - the value corresponding the the max fullness of this bar
   * @param text - text to draw. Set to empty to draw nothing.
   * @param textColor - color to draw the text. If text is null or empty, unused.
   * @param textFont - the font to use for text drawing. If text is null or empty, unused
   * @param incrementColor - the color to draw the increment lines in. set to null or transparent to
   *     not use
   * @param incrementVal - the numeric value corresponding to one increment value. If incrementColor
   *     is null, not used.
   */
  public static void drawBar(
      Graphics2D g2d,
      final int X,
      final int Y,
      final int BAR_WIDTH,
      final int BAR_HEIGHT,
      Color backColor,
      Color borderColor,
      int strokeWidth,
      int maxVal,
      List<DrawingBarSegment> segments,
      String text,
      Color textColor,
      Font textFont,
      Color incrementColor,
      int incrementVal) {
    if (strokeWidth < 0) throw new IllegalArgumentException("Bar Border Can't have negative width");

    double totalPercent = segments.stream().mapToDouble(DrawingBarSegment::getPercentFull).sum();
    if (totalPercent < 0 || totalPercent > 1)
      throw new IllegalArgumentException(
          "Can't fill a bar an illegal Percent full: " + totalPercent);

    if (backColor != null) {
      g2d.setColor(backColor);
      g2d.fillRect(
          X + strokeWidth / 2,
          Y + strokeWidth / 2,
          BAR_WIDTH - strokeWidth / 2 - 1,
          BAR_HEIGHT - strokeWidth);
    }
    int startBarX = X + strokeWidth / 2;
    for (DrawingBarSegment segment : segments) {
      int width = (int) Math.ceil((BAR_WIDTH - strokeWidth / 2 - 1) * segment.percentFull);
      g2d.setColor(segment.color);
      g2d.fillRect(startBarX, Y + strokeWidth / 2, width, BAR_HEIGHT - strokeWidth);
      startBarX += width;
    }
    if (strokeWidth > 0) {
      g2d.setStroke(new BasicStroke(strokeWidth));
      g2d.setColor(borderColor);
      g2d.drawRect(X, Y, BAR_WIDTH, BAR_HEIGHT);
    }
    if (incrementColor != null) {
      g2d.setColor(incrementColor);
      g2d.setStroke(new BasicStroke(2));
      for (int i = incrementVal; i < maxVal; i += incrementVal) {
        int x = X + strokeWidth / 2 + (int) (BAR_WIDTH * (double) i / (double) maxVal);
        g2d.drawLine(x, strokeWidth / 2 + Y + 1, x, Y + BAR_HEIGHT - strokeWidth / 2);
      }
    }
    if (text != null) {
      int textWidth = g2d.getFontMetrics(textFont).stringWidth(text);
      g2d.setColor(textColor);
      g2d.setFont(textFont);
      g2d.drawString(text, X + (BAR_WIDTH - textWidth) / 2, Y + BAR_HEIGHT / 2 + strokeWidth);
    }
  }
}
