package gui;

import game.Game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JPanel;
import board.*;
import unit.*;

/** Drawable wrapper for a board object */
public class GamePanel extends JPanel implements Paintable{

	/***/
	private static final long serialVersionUID = 1L;

	/** Pixels (size) for each square tile. */
	protected static final int CELL_SIZE = 64; 

	/** The Game this GamePanel is responsible for drawing */
	public final Game game;

	/** The BoardCursor for this GamePanel */
	public final BoardCursor boardCursor;
	
	/** The PathSelector that is currently active. Null if none */
	private PathSelector pathSelector;
	
	/** Maximum number of columns of tiles to visually show */
	public final int maxX;
	
	/** Maximum number of rows of tiles to visually show */
	public final int maxY;
	
	/** Scroll in the x direction, in terms of # of cols to skip. Used as a scroll delta */
	protected int scrollX;
	
	/** Scroll in the y direction, in terms of # of rows to skip. Used as a scroll delta */
	protected int scrollY;

	/** Constructor for GamePanel
	 * @param g - the game to display using this panel
	 * @param maxRows - the maximum number of rows of tiles to show at a time
	 * @param maxCols - the maximum number of cols of tiles to show at a time
	 */
	public GamePanel(Game g, int maxRows, int maxCols){
		game = g;
		boardCursor = new BoardCursor(this);
		
		this.maxX = maxCols;
		this.maxY = maxRows;
		
		scrollX = 0;
		scrollY = 0;
		
		setPreferredSize(new Dimension(maxX * CELL_SIZE, maxY * CELL_SIZE));
	}
	
	/** Creates a new pathSelector at the current boardCursor position.
	 * Does nothing if the current tile is unoccupied or the unit has already moved. */
	public void startPathSelection(){
		Tile t = boardCursor.getTile();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canMove()) return;
		
		pathSelector = new PathSelector(this, (MovingUnit) t.getOccupyingUnit());
	}
	
	/** Cancels the path selection - deletes it but does nothing */
	public void cancelPathSelection(){
		if(pathSelector != null) boardCursor.setTile(pathSelector.getPath().getFirst());
		pathSelector = null;
	}
	
	/** Processes the path selection - if ok, deletes it.
	 * Otherwise makes err noise or something. */
	public void processPathSelection(){
		try{
			pathSelector.unit.move(pathSelector.getPath());
			pathSelector = null;
		}catch(Exception e){
			//TODO - sound err
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	/** Returns the current pathSelector, if any */
	public PathSelector getPathSelector(){
		return pathSelector;
	}
	
	/** Returns the xPosition (graphically) of the top left corner of the given tile */
	public int getXPosition(Tile t){
		return (t.col - scrollX) * CELL_SIZE;
	}
	
	/** Returns the yPosition (graphically) of the top left corner of the given tile */
	public int getYPosition(Tile t){
		return (t.row - scrollY) * CELL_SIZE;
	}

	@Override
	/** Paints this boardpanel, for use in the frame it is in. */
	public void paintComponent(Graphics g){

		//Paint the board itself, painting the portion within
		//[scrollY ... scrollY + maxY - 1], 
		//[scrollX ... scrollX + maxX - 1]
		for(int row = scrollY; row < scrollY + maxY; row ++){
			for(int col = scrollX; col < scrollX + maxX; col ++){
				Tile t = game.board.getTileAt(row, col);
				int x = getXPosition(t);
				int y = getYPosition(t);
				//Draw terrain
				g.drawImage(ImageIndex.imageForTile(t), x, y,
					CELL_SIZE, CELL_SIZE, null);
				
				//Draw a unit if necessary
				if(t.isOccupied()){
					g.drawImage(ImageIndex.imageForUnit(t.getOccupyingUnit()), x, y, 
						CELL_SIZE, CELL_SIZE, null);
				}
			}
		}
		
		//Draw the movement and cloud path
		if(pathSelector != null){			
			pathSelector.paintComponent(g);
		}
		
		//Draw the cursor
		boardCursor.paintComponent(g);
	}

}
