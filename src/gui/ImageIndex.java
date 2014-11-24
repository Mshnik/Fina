package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import board.Tile;

/** Library for lookup of different image resources */
public class ImageIndex {

	/** Prevent instantiation of ImageIndex */
	private ImageIndex(){}

	/** Root Location of image files */
	public static final String IMAGE_ROOT = "img/";
	/** The image for grassy terrain */
	private static BufferedImage GRASS_IMG;
	/** The image for mountain terrain */
	private static BufferedImage MOUNTAIN_IMG;
	/** The image for woods terrain */
	private static BufferedImage WOODS_IMG;
	
	/** Static initializer for the Image Class - do all image reading here */
	static{
		try {
			GRASS_IMG = ImageIO.read(new File(IMAGE_ROOT + "grass.png"));
			MOUNTAIN_IMG = ImageIO.read(new File(IMAGE_ROOT + "mountain.png"));
			WOODS_IMG = ImageIO.read(new File(IMAGE_ROOT + "woods.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Returns the image file corresponding to the given terrain type */
	public static BufferedImage imageForTile(Tile t){
		switch(t.terrain){
			case GRASS: 	return GRASS_IMG;
			case MOUNTAIN: 	return MOUNTAIN_IMG;
			case WOODS:		return WOODS_IMG;
			default:		return null;
		}
	}
}
