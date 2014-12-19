package gui;

import gui.panel.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
	
	/** Tints the given image //TODO
	 * borders with given color */
	public static BufferedImage tint(BufferedImage img, Color color) {
		Graphics2D g2d = (Graphics2D)img.getGraphics();
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(10));
		g2d.drawRect(0, 0, img.getWidth(), img.getHeight());
		
//		BufferedImage img = new BufferedImage(loadImg.getWidth(), loadImg.getHeight(),
//	            BufferedImage.TRANSLUCENT);
//	        Graphics2D graphics = img.createGraphics(); 
//	        Color newColor = new Color(color.getRed(), color.getGreen(), 
//	        							color.getBlue(), 0 /* alpha needs to be zero */);
//	        graphics.setColor(color);
//	        graphics.drawRect(0, 0, loadImg.getWidth(), loadImg.getHeight());
//	        graphics.drawImage(loadImg, null, 0, 0);
//	        graphics.dispose();
		
	    return img;
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
	
	/** An image utility main method. */
	public static void main(String[] args){
//		try {
//			BufferedImage sheet = ImageIO.read(new File(IMAGE_ROOT + "spriteSheet.png"));
//			int sideLength = 67;
//			int i = 1;
//			for(int y = 0; y <= sheet.getWidth(); y+= sideLength){
//				for(int x = 0; x <= sheet.getHeight(); x+= sideLength){
//					try{
//					BufferedImage cut = sheet.getSubimage(x, y, sideLength, sideLength);
//					ImageIO.write(cut, "png", new File(IMAGE_ROOT + MovingUnit.IMAGE_ROOT + "_" + i + ".png"));
//					i++;
//					} catch(Exception e){}
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
