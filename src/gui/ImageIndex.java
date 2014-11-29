package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import unit.MovingUnit;
import unit.Unit;

import board.Terrain;
import board.Tile;

/** Library for lookup of different image resources */
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
	
	/** The image for dummy commander */
	public static BufferedImage DUMMY_COMMANDER;
	
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
			DUMMY_COMMANDER = ImageIO.read(new File(IMAGE_ROOT + MovingUnit.IMAGE_ROOT + "link.png"));
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
		switch(unit.getImgFilename()){
			case "chrono.gif": return DUMMY_COMMANDER;
			
			//Image not found
			default: 		   return null;
		}
	}
}
