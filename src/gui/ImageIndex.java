package gui;

import gui.panel.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import unit.Building;
import unit.MovingUnit;
import unit.Unit;

import board.Board;
import board.Direction;
import board.Terrain;
import board.Tile;

/** Library for lookup of different image resources.
 * Also some drawing functionality */
public class ImageIndex {

	/** Prevent instantiation of ImageIndex */
	private ImageIndex(){}

	/** Root Location of image files */
	public static final String IMAGE_ROOT = "img/";

	/** The image for grassy terrain */
	public static BufferedImage GRASS;
	/** The image for mountain terrain */
	public static BufferedImage MOUNTAINS;
	/** The image for woods terrain */
	public static BufferedImage WOODS;
	/** The image for ancient ground terrain */
	public static BufferedImage ANCIENT_GROUND;

	/** The image for sandstone texture */
	public static BufferedImage SANDSTONE;

	/** Read in units thus far */
	private static HashMap<String, BufferedImage> readUnits;

	/** Static initializer for the Image Class - do all image reading here */
	static{
		try {
			//Assorted other 
			SANDSTONE = ImageIO.read(new File(IMAGE_ROOT + "sandstone.jpg"));

			//Terrain
			GRASS = ImageIO.read(new File(IMAGE_ROOT + Terrain.IMAGE_ROOT + "grass.png"));
			MOUNTAINS = ImageIO.read(new File(IMAGE_ROOT + Terrain.IMAGE_ROOT + "mountain.png"));
			WOODS = ImageIO.read(new File(IMAGE_ROOT + Terrain.IMAGE_ROOT + "woods.png"));
			ANCIENT_GROUND = ImageIO.read(new File(IMAGE_ROOT + Terrain.IMAGE_ROOT + "gold.jpg"));	

			//Units
			readUnits = new HashMap<String, BufferedImage>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Returns the image file corresponding to the given terrain type */
	public static BufferedImage imageForTile(Tile t){
		switch(t.terrain){
		case GRASS: 	return GRASS;
		case MOUNTAIN: 	return MOUNTAINS;
		case WOODS:		return WOODS;
		case ANCIENT_GROUND: return ANCIENT_GROUND;

		//Image not found
		default:		return null;
		}
	}

	/** Returns the image file corresponding to the given unit */
	public static BufferedImage imageForUnit(Unit unit){
		if(readUnits.containsKey(unit.getImgFilename()))
			return readUnits.get(unit.getImgFilename());

		BufferedImage u = null;
		try {
			String root = IMAGE_ROOT;
			if(unit instanceof MovingUnit)
				root += MovingUnit.IMAGE_ROOT;
			else if(unit instanceof Building)
				root += Building.IMAGE_ROOT;
			u = ImageIO.read(new File(root + unit.getImgFilename()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		readUnits.put(unit.getImgFilename(), u);
		return u;
	}

	/** Tints the given image with the given color.
	 * @param loadImg - the image to paint and tint
	 * @param color - the color to tint. Alpha value of input color isn't used.
	 * @return A tinted version of loadImg */
	public static BufferedImage tint(BufferedImage loadImg, Color color) {
		BufferedImage img = new BufferedImage(loadImg.getWidth(), loadImg.getHeight(),
				BufferedImage.TRANSLUCENT);
		final float tintOpacity = 0.45f;
		Graphics2D g2d = img.createGraphics(); 
		
		//Draw the base image
		g2d.drawImage(loadImg, null, 0, 0);
		//Set the color to a transparent version of the input color
		g2d.setColor(new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, tintOpacity));
		
		//Iterate over every pixel, if it isn't transparent paint over it
		Raster data = loadImg.getData();
		for(int x = data.getMinX(); x < data.getWidth(); x++){
			for(int y = data.getMinY(); y < data.getHeight(); y++){
				int[] pixel = data.getPixel(x, y, new int[4]);
				if(pixel[3] > 0){ //If pixel isn't full alpha. Could also be pixel[3]==255
					g2d.fillRect(x, y, 1, 1);
				}
			}
		}
		g2d.dispose();
		return img;
	}

	/** Draws a border around the set of tiles using center and radius.
	 * Assumes graphic settings such as color, stroke have been set
	 * Radius is extra tiles around center - radius of 0 is just center */
	public static void drawRadial(Tile center, int radius, GamePanel gp, Graphics2D g2d){
		Board b = center.board;
		ArrayList<Tile> tiles = b.getRadialCloud(center, radius);
		for(Tile t : tiles){
			for(Direction d : Direction.values()){
				boolean paint = true;
				try{
					Tile n = b.getTileAt(t.row + d.dRow() , t.col + d.dCol());
					paint = ! tiles.contains(n);
				} catch(IllegalArgumentException e){
				}
				if(paint) drawLine(g2d, gp, t, d);
			}
		}

	}

	private static void drawLine(Graphics2D g2d, GamePanel gp, Tile t, Direction side){
		switch(side){
		case UP:	g2d.drawLine(gp.getXPosition(t), gp.getYPosition(t), 
				gp.getXPosition(t) + GamePanel.CELL_SIZE, gp.getYPosition(t));
		break;
		case RIGHT: g2d.drawLine(gp.getXPosition(t) + GamePanel.CELL_SIZE, gp.getYPosition(t), 
				gp.getXPosition(t) + GamePanel.CELL_SIZE, gp.getYPosition(t) + GamePanel.CELL_SIZE);
		break;
		case DOWN: g2d.drawLine(gp.getXPosition(t), gp.getYPosition(t) + GamePanel.CELL_SIZE, 
				gp.getXPosition(t) + GamePanel.CELL_SIZE, gp.getYPosition(t) + GamePanel.CELL_SIZE);
		break;
		case LEFT: g2d.drawLine(gp.getXPosition(t), gp.getYPosition(t), 
				gp.getXPosition(t), gp.getYPosition(t) + GamePanel.CELL_SIZE);
		break;
		}
	}

	/** Draws a bar with the border and fill colors, full the given amount, etc etc etc.
	 * @param g2d - the graphics object to use for drawing. Can't be null
	 * @param X - the x coordinate of the top left corner of the bar
	 * @param Y - the y coordinate of the top left corner of the bar
	 * @param BAR_WIDTH - the width of the bar
	 * @param BAR_HEIGHT - the height of the bar
	 * @param backColor - the color to draw behind the bar for any unfilled portion. can be null
	 * @param border - the color to draw the border of the bar. If strokeWidth is 0, this is unused
	 * @param strokeWidth - the width of the border portion of the bar. Can't be negative.
	 * @param fillColor - the color of the fill portion of the bar. Can't be null.
	 * @param maxVal - the value corresponding the the max fullness of this bar
	 * @param percentFull - the percent of this bar to fill - in the range [0, 1].
	 * @param text - text to draw. Set to empty to draw nothing.
	 * @param textColor - color to draw the text. If text is null or empty, unused.
	 * @param textFont - the font to use for text drawing. If text is null or empty, unused
	 * @param incrementColor - the color to draw the increment lines in. set to null or transparent to not use
	 * @param incrementVal - the numeric value corresponding to one increment value. 
	 * 						If incrementColor is null, not used.
	 */
	public static void drawBar(Graphics2D g2d, 
			final int X, final int Y, final int BAR_WIDTH, final int BAR_HEIGHT,
			Color backColor, Color borderColor, int strokeWidth, Color fillColor, 
			int maxVal, double percentFull, String text, Color textColor, Font textFont,
			Color incrementColor, int incrementVal){
		if(strokeWidth < 0)
			throw new IllegalArgumentException("Bar Border Can't have negative width");
		if(percentFull < 0 || percentFull > 1)
			throw new IllegalArgumentException("Can't fill a bar an illegal Percent full: " + percentFull);

		if(strokeWidth > 0){
			g2d.setStroke(new BasicStroke(strokeWidth));
			g2d.setColor(borderColor);
			g2d.drawRect(X, Y, BAR_WIDTH, BAR_HEIGHT);
		}
		if(backColor != null){
			g2d.setColor(backColor);
			g2d.fillRect(X + strokeWidth/2, Y + strokeWidth/2, 
					BAR_WIDTH - strokeWidth/2 - 1, BAR_HEIGHT - strokeWidth);
		}
		g2d.setColor(fillColor);
		g2d.fillRect(X + strokeWidth/2, Y + strokeWidth/2, 
				(int)((BAR_WIDTH - strokeWidth/2 - 1) * percentFull), BAR_HEIGHT - strokeWidth);

		if(incrementColor != null){
			g2d.setColor(incrementColor);
			g2d.setStroke(new BasicStroke(2));
			for(int i = incrementVal; i < maxVal; i+= incrementVal){
				int x = X + strokeWidth/2 + (int)(BAR_WIDTH * (double)i/(double)maxVal);
				g2d.drawLine(x, strokeWidth/2 + Y + 1, x, Y + BAR_HEIGHT - strokeWidth/2);
			}
		}
		if(text != null){
			g2d.setColor(textColor);
			g2d.setFont(textFont);
			g2d.drawString(text, X + BAR_WIDTH/2 - 10, Y + BAR_HEIGHT/2 + strokeWidth);
		}
	}
}
