package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import unit.Combatant;

import board.Tile;

/** Library for lookup of different image resources */
public class ImageIndex {

	/** Prevent instantiation of ImageIndex */
	private ImageIndex(){}

	/** Root Location of image files */
	public static final String IMAGE_ROOT = "img/";
	
	/** The image for grassy terrain */
	private static BufferedImage GRASS;
	/** The image for mountain terrain */
	private static BufferedImage MOUNTAINS;
	/** The image for woods terrain */
	private static BufferedImage WOODS;
	
	/** The image for dummy unit */
	private static BufferedImage DUMMY_UNIT;
	
	/** Static initializer for the Image Class - do all image reading here */
	static{
		try {
			GRASS = ImageIO.read(new File(IMAGE_ROOT + "grass.png"));
			MOUNTAINS = ImageIO.read(new File(IMAGE_ROOT + "mountain.png"));
			WOODS = ImageIO.read(new File(IMAGE_ROOT + "woods.png"));
			DUMMY_UNIT = ImageIO.read(new File(IMAGE_ROOT + "chrono.gif"));
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
	public static BufferedImage imageForUnit(Combatant u){
		switch(u.getImgFilename()){
			case "chrono.gif": return DUMMY_UNIT;
			
			
			//Image not found
			default: 		   return null;
		}
	}
}
