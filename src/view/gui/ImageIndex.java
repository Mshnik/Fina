package view.gui;

import model.board.Direction;
import model.board.Terrain;
import model.board.Tile;
import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.building.Building;
import view.gui.panel.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/** Library for lookup of different image resources. Also some drawing functionality */
public final class ImageIndex {

  /** Prevent instantiation of ImageIndex */
  private ImageIndex() {}

  /** Root Location of image files */
  private static final String IMAGE_ROOT = "img/";

  /** Location of terrain files within image root */
  private static final String TERRAIN_IMAGE_ROOT = "terrain/";

  /** Image root for moving units (people) within image root */
  private static final String UNIT_IMAGE_ROOT = "unit/";

  /** Image root for Building images within image root */
  private static final String BUILDING_IMAGE_ROOT = "building/";

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

      // Units
      readUnits = new HashMap<String, BufferedImage>();
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

        // Image not found
      default:
        return null;
    }
  }

  /** Returns the image file corresponding to the given model.unit */
  public static BufferedImage imageForUnit(Unit unit) {
    if (readUnits.containsKey(unit.getImgFilename())) return readUnits.get(unit.getImgFilename());

    BufferedImage u = null;
    try {
      String root = IMAGE_ROOT;
      if (unit instanceof MovingUnit) root += UNIT_IMAGE_ROOT;
      else if (unit instanceof Building) root += BUILDING_IMAGE_ROOT;
      u = ImageIO.read(new File(root + unit.getImgFilename()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    readUnits.put(unit.getImgFilename(), u);
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
      g2d.fillRect(
          gp.getXPosition(t),
          gp.getYPosition(t),
          gp.cellSize(),
          gp.cellSize());
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
