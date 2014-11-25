package gui;

import board.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import unit.AbstractUnit;

/** An instance represents and draws the path on the board when
 * the player is moving a unit
 * @author MPatashnik
 *
 */
public class PathSelector implements Paintable, Iterable<Tile>{

	/** Color for Path Drawing - red */
	protected static final Color PATH_COLOR = Color.red;
	
	/** Color for Cloud Drawing - translucent white */
	protected static final Color CLOUD_COLOR = new Color(1, 1, 1, 0.5f);
	
	/** Thickness of lines in Path Drawing */
	protected static final int THICKNESS = 8;
	
	/** The unit this path is moving */
	public final AbstractUnit unit;
	
	/** The gamePanel this is drawing for */
	public final GamePanel gamePanel;

	/** The path this pathSelector currently represents and is drawing.
	 * First element is always the start tile */
	private LinkedList<Tile> path;
	
	/** The possible tiles the path could go to from here - possibilities cloud */
	private HashSet<Tile> cloud;

	/** Constructor for PathSelector
	 * @param s - start of path.
	 */
	public PathSelector(GamePanel gp, AbstractUnit unit){
		gamePanel = gp;
		this.unit = unit;
		path = new LinkedList<Tile>();
		path.add(unit.getOccupiedTile());
		cloud = new HashSet<Tile>();
		refreshPossibilitiesCloud();
	}
	
	/** Empties and recalculated the possibilities cloud using the current path as set */
	private void refreshPossibilitiesCloud(){
		cloud = gamePanel.boardState.getMovementCloud(unit, this);
		gamePanel.repaint();
	}

	/** Return the path this PathSelector currently represents and is drawing.
	 * This is pass-by-value, so editing the returned list won't change the PathSelector. */
	public LinkedList<Tile> getPath(){
		return new LinkedList<Tile>(path);
	}
	
	/** Returns the possible movements cloud.
	 * This is pass-by-value, so editing the returned set won't change the PathSelector.
	 **/
	public HashSet<Tile> getPossibleMovementsCloud(){
		return new HashSet<Tile>(cloud);
	}
	
	/** Returns a toString for this PathSelector as the toString of its list of tiles */
	@Override
	public String toString(){
		return path.toString();
	}

	/** Return the length of the path in tiles */
	public int getLength(){
		return path.size();
	}
	
	/** Return the total cost of traveling the given path for unit */
	public int getTotalCost(){
		int c = 0;
		for(Tile t : path){
			c += unit.getMovementCost(t.terrain);
		}
		return c;
	}

	/** Adds the given Tile to the path, then removes cycle as necessary */
	public void addToPath(Tile t){
		path.add(t);
		//Cycle iff first and last index of t aren't equal
		int i = path.indexOf(t);
		int l = path.lastIndexOf(t);
		int diff = l - i;
		for(int r = 0; r < diff; r++){
			path.remove(i);		//Remove ith position r times to delete cycle.
		}
		
		refreshPossibilitiesCloud();
	}

	/** Returns an iterator over the tiles in this path */
	@Override
	public Iterator<Tile> iterator() {
		return path.iterator();
	}

	/** Draws this path */
	@Override
	public void paintComponent(Graphics g){
		//Draw the possible movement cloud
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(CLOUD_COLOR);
		
		for(Tile t : cloud){
			if(t != unit.getOccupiedTile()){
				int x = gamePanel.getXPosition(t);
				int y = gamePanel.getYPosition(t);
				g2d.fillRect(x, y, GamePanel.CELL_SIZE, GamePanel.CELL_SIZE);
			}
		}
		
		//Draw the path itself
		if(getLength() < 2) return;	//Do nothing for drawing path of length 1 (or 0, but that's impossible).
		
		g2d.setColor(PATH_COLOR);
		g2d.setStroke(new BasicStroke(THICKNESS));
		
		//Create two iterators, with i2 always one ahead of i1
		Iterator<Tile> i1 = iterator();
		Iterator<Tile> i2 = iterator();
		i2.next(); //Advance i2 to be in front of i1
		
		final int s = GamePanel.CELL_SIZE/2;

		while(i2.hasNext()){		
			Tile current = i1.next();
			Tile next = i2.next();
			
			//Draw this part of the line from this center to the next center
			g2d.drawLine(gamePanel.getXPosition(current) + s, 
						 gamePanel.getYPosition(current) + s, 
						 gamePanel.getXPosition(next)    + s, 
						 gamePanel.getYPosition(next)    + s);
		}
		Tile current = i1.next();
		int x = gamePanel.getXPosition(current);
		int y = gamePanel.getYPosition(current);
		
		final int scaledS = (int)(0.5 * s);
		
		g2d.fillPolygon(new int[]{x + scaledS, x + s, x + 3 * scaledS, x + s},
						new int[]{y + s, y + scaledS, y + s, y + 3 * scaledS},
						4);
	}
}
