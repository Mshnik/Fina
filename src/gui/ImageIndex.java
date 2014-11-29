package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import unit.Unit;

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
	
	/** The image for sandstone texture */
	public static BufferedImage SANDSTONE;
	
	/** The image for dummy commander */
	public static BufferedImage DUMMY_COMMANDER;
	
	/** Static initializer for the Image Class - do all image reading here */
	static{
		try {
			GRASS = ImageIO.read(new File(IMAGE_ROOT + "grass.png"));
			MOUNTAINS = ImageIO.read(new File(IMAGE_ROOT + "mountain.png"));
			WOODS = ImageIO.read(new File(IMAGE_ROOT + "woods.png"));
			SANDSTONE = ImageIO.read(new File(IMAGE_ROOT + "sandstone.jpg"));
			DUMMY_COMMANDER = ImageIO.read(new File(IMAGE_ROOT + "chrono.gif"));
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
