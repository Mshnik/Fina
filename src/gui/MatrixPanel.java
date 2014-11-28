package gui;

import game.MatrixElement;
import javax.swing.JPanel;

import board.Direction;
import board.Tile;

/** Instantiating classes are able to act as a matrix with scroll, max sizes. */
public abstract class MatrixPanel<T extends MatrixElement> extends JPanel {

	/***/
	private static final long serialVersionUID = 1L;
	
	/** Maximum number of columns of elements to visually show */
	public final int maxX;
	
	/** Maximum number of rows of tiles to visually show */
	public final int maxY;
	
	/** Scroll in the x direction, in terms of # of cols to skip. Used as a scroll delta */
	protected int scrollX;
	
	/** Scroll in the y direction, in terms of # of rows to skip. Used as a scroll delta */
	protected int scrollY;
	
	/** Constructor for MatrixPanel
	 * @param maxX		- the number of cols to paint at a time
	 * @param maxY		- the number of rows to paint at a time
	 * @param scrollX	- the starting scrolling of cols
	 * @param scrollY	- the starting scrolling of rows
	 */
	public MatrixPanel(int maxX, int maxY, int scrollX, int scrollY){
		this.maxX = maxX;
		this.maxY = maxY;
		
		this.scrollX = scrollX;
		this.scrollY = scrollY;
	}
	
	/** Returns the width of the underlying matrix */
	public abstract int getMatrixWidth();
	
	/** Returns the height of the underlying matrix */
	public abstract int getMatrixHeight();
	
	/** Returns the element at the given indices. Throws IllegalArgumentException if this is OOB */
	public abstract T getElmAt(int row, int col) throws IllegalArgumentException;
	
	/** Returns the xPosition (graphically) of the top left corner of the given element */
	public int getXPosition(T t){
		return (t.getCol() - scrollX) * getElementWidth();
	}
	
	/** Returns the yPosition (graphically) of the top left corner of the given tile */
	public int getYPosition(T t){
		return (t.getRow() - scrollY) * getElementHeight();
	}
	
	/** Returns the height of a element in the drawing */
	public abstract int getElementHeight();
	
	/** Returns the width of a element in the drawing */
	public abstract int getElementWidth();
	
	/** Return the tile in the given direction from this tile.
	 * If oob, returns null or if direction invalid.
	 */
	public T getElmInDirection(T t, Direction d){
		try{
			switch(d){
			case DOWN: return getElmAt(t.getRow() + 1, t.getCol());
			case LEFT: return getElmAt(t.getRow(), t.getCol() - 1);
			case RIGHT:return getElmAt(t.getRow(), t.getCol() + 1);
			case UP:   return getElmAt(t.getRow() - 1, t.getCol());
			default:   return null;
			}
		}catch(IllegalArgumentException e){
			return null;
		}
	}
	
}
