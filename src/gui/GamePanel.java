package gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import board.*;

/** Drawable wrapper for a board object */
public class BoardPanel extends JPanel implements Paintable{

	/***/
	private static final long serialVersionUID = 1L;

	/** Pixels (size) for each square tile. */
	protected static final int CELL_SIZE = 30; 

	/** The BoardState this BoardPanel is responsible for drawing */
	public final Board boardState;

	/** The BoardCursor for this BoardPanel */
	public final BoardCursor boardCursor;
	
	/** Maximum number of columns of tiles to show */
	public final int maxX;
	
	/** Maximum number of rows of tiles to show */
	public final int maxY;
	
	/** Scroll in the x direction, in terms of # of cols to skip. Used as a scroll delta */
	protected int scrollX;
	
	/** Scroll in the y direction, in terms of # of rows to skip. Used as a scroll delta */
	protected int scrollY;

	/** Constructor for BoardPanel
	 * @param bs - the board to display using this panel
	 * @param maxRows - the maximum number of rows of tiles to show at a time
	 * @param maxCols - the maximum number of cols of tiles to show at a time
	 */
	public BoardPanel(Board bs, int maxRows, int maxCols){
		boardState = bs;
		boardCursor = new BoardCursor(this);
		
		this.maxX = maxCols;
		this.maxY = maxRows;
		
		scrollX = 0;
		scrollY = 0;
		
		setPreferredSize(new Dimension(maxX * CELL_SIZE, maxY * CELL_SIZE));
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
					(t.col - scrollX) * CELL_SIZE, 
					(t.row - scrollY) * CELL_SIZE,
					CELL_SIZE, CELL_SIZE, 
					null);
			}
		}
		
		//Draw the cursor
		boardCursor.paintComponent(g);
	}

}
