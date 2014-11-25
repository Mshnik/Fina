package gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import board.*;

/** Drawable wrapper for a board object */
public class GamePanel extends JPanel implements Paintable{

	/***/
	private static final long serialVersionUID = 1L;

	/** Pixels (size) for each square tile. */
	protected static final int CELL_SIZE = 32; 

	/** The BoardState this GamePanel is responsible for drawing */
	public final Board boardState;

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
	 * @param bs - the board to display using this panel
	 * @param maxRows - the maximum number of rows of tiles to show at a time
	 * @param maxCols - the maximum number of cols of tiles to show at a time
	 */
	public GamePanel(Board bs, int maxRows, int maxCols){
		boardState = bs;
		boardCursor = new BoardCursor(this);
		
		this.maxX = maxCols;
		this.maxY = maxRows;
		
		scrollX = 0;
		scrollY = 0;
		
		setPreferredSize(new Dimension(maxX * CELL_SIZE, maxY * CELL_SIZE));
	}
	
	/** Creates a new pathSelector at the current boardCursor position */
	public void startPathSelection(){
		pathSelector = new PathSelector(this, boardCursor.getTile());
	}
	
	/** Cancels the path selection - deletes it but does nothing */
	public void cancelPathSelection(){
		pathSelector = null;
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
				Tile t = boardState.getTileAt(row, col);
				g.drawImage(ImageIndex.imageForTile(t), 
					getXPosition(t), 
					getYPosition(t),
					CELL_SIZE, CELL_SIZE, 
					null);
			}
		}
		
		//Draw the path
		if(pathSelector != null){
			pathSelector.paintComponent(g);
		}
		
		//Draw the cursor
		boardCursor.paintComponent(g);
	}

}
