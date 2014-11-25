package gui;

import board.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;

/** An instance represents and draws the path on the board when
 * the player is moving a unit
 * @author MPatashnik
 *
 */
public class PathSelector implements Paintable, Iterable<Tile>{

	/** Color for Path Drawing */
	protected static final Color COLOR = Color.red;
	
	/** Thickness of lines in Path Drawing */
	protected static final int THICKNESS = 8;
	
	/** The tile this path starts on */
	public final Tile start;
	
	/** The gamePanel this is drawing for */
	public final GamePanel gamePanel;

	/** The path this pathSelector currently represents and is drawing.
	 * First element is always the start tile */
	private LinkedList<Tile> path;

	/** Constructor for PathSelector
	 * @param s - start of path.
	 */
	public PathSelector(GamePanel gp, Tile s){
		gamePanel = gp;
		start = s;
		path = new LinkedList<Tile>();
		path.add(s);
	}

	/** Return the path this PathSelector currently represents and is drawing.
	 * This is pass-by-value, so editing the returned list won't change the PathSelector. */
	public LinkedList<Tile> getPath(){
		return new LinkedList<Tile>(path);
	}
	
	/** Returns a toString for this PathSelector as the toString of its list of tiles */
	@Override
	public String toString(){
		return path.toString();
	}

	/** Return the length of the path */
	public int getLength(){
		return path.size();
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
	}

	/** Returns an iterator over the tiles in this path */
	@Override
	public Iterator<Tile> iterator() {
		return path.iterator();
	}

	/** Draws this path */
	@Override
	public void paintComponent(Graphics g){
		if(getLength() < 2) return;	//Do nothing for drawing path of length 1.

		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(COLOR);
		g2d.setStroke(new BasicStroke(THICKNESS));
		
		//Create two iterators, with i2 always one ahead of i1
		Iterator<Tile> i1 = iterator();
		Iterator<Tile> i2 = iterator();
		
		i2.next();

		while(i2.hasNext()){		
			Tile current = i1.next();
			Tile next = i2.next();
			
			//Draw this part of the line from this center to the next center
			g2d.drawLine(gamePanel.getXPosition(current) + GamePanel.CELL_SIZE/2, 
						 gamePanel.getYPosition(current) + GamePanel.CELL_SIZE/2, 
						 gamePanel.getXPosition(next)    + GamePanel.CELL_SIZE/2, 
						 gamePanel.getYPosition(next)    + GamePanel.CELL_SIZE/2);
			
		}
	}
}
