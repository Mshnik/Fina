package gui;

import board.*;

import gui.panel.GamePanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;

/** An instance represents a selector for any location with a given criteria */
public abstract class LocationSelector implements Paintable {
	/** Color for Cloud Drawing - translucent white */
	protected static final Color CLOUD_COLOR = new Color(1, 1, 1, 0.5f);

	/** The gamePanel this is drawing for */
	public final GamePanel gamePanel;

	/** The possible tiles the path could go to from here - possibilities cloud */
	protected HashSet<Tile> cloud;

	/** Constructor for PathSelector.
	 * Implementing classes should refresh possibilities cloud at end of construction
	 * @param s - start of path.
	 */
	public LocationSelector(GamePanel gp){
		gamePanel = gp;
		cloud = new HashSet<Tile>();
	}

	/** Empties and recalculated the possibilities cloud using the current path as set */
	protected abstract void refreshPossibilitiesCloud();

	/** Returns the possible movements cloud.
	 * This is pass-by-value, so editing the returned set won't change the PathSelector.
	 **/
	public HashSet<Tile> getPossibleMovementsCloud(){
		return new HashSet<Tile>(cloud);
	}

	/** Draws this cloud */
	@Override
	public void paintComponent(Graphics g){
		//Draw the possible movement cloud
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(CLOUD_COLOR);

		for(Tile t : cloud){
			int x = gamePanel.getXPosition(t);
			int y = gamePanel.getYPosition(t);
			g2d.fillRect(x, y, GamePanel.CELL_SIZE, GamePanel.CELL_SIZE);
		}
	}
}
