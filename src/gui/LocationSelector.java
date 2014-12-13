package gui;

import board.*;

import gui.panel.GamePanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

/** An instance represents a selector for any location with a given criteria */
public abstract class LocationSelector implements Paintable {
	/** The gamePanel this is drawing for */
	public final GamePanel gamePanel;

	/** The possible tiles the path could go to from here - possibilities cloud */
	protected ArrayList<Tile> cloud;
	
	/** Color for Cloud Drawing - translucent white - can be changed by subclasses */
	protected Color cloudColor = new Color(1, 1, 1, 0.5f);

	/** Constructor for PathSelector.
	 * Implementing classes should refresh possibilities cloud at end of construction
	 * @param s - start of path.
	 */
	public LocationSelector(GamePanel gp){
		gamePanel = gp;
		cloud = new ArrayList<Tile>();
	}

	/** Empties and recalculated the possibilities cloud using the current path as set */
	protected abstract void refreshPossibilitiesCloud();
	
	/** Returns the possible movements cloud.
	 * This is pass-by-value, so editing the returned set won't change the PathSelector.
	 **/
	public ArrayList<Tile> getPossibleMovementsCloud(){
		return new ArrayList<Tile>(cloud);
	}

	/** Draws this cloud */
	@Override
	public void paintComponent(Graphics g){
		//Draw the possible movement cloud
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(cloudColor);

		for(Tile t : cloud){
			int x = gamePanel.getXPosition(t);
			int y = gamePanel.getYPosition(t);
			g2d.fillRect(x, y, GamePanel.CELL_SIZE, GamePanel.CELL_SIZE);
		}
	}
}
