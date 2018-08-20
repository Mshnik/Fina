package view.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import controller.game.MatrixElement;

import model.board.Direction;
import controller.game.GameController;


/** Instantiating classes are able to act as a matrix with scroll, max sizes. */
public abstract class MatrixPanel<T extends MatrixElement> extends JPanel {

	/***/
	private static final long serialVersionUID = 1L;
	
	/** The GameController this MatrixPanel is drawing something or other for ... Fuck idk. */
	protected final GameController controller;
	
	/** Maximum number of columns of elements to visually show */
	private int maxX;
	
	/** Maximum number of rows of tiles to visually show */
	private int maxY;

	/** Margin in the x direction, in terms of extra cols that have no tiles on them. */
	protected int marginX;

	/** Margin in the y direction, in terms of extra rows that have no tiles on them. */
	protected int marginY;

	/** Scroll in the x direction, in terms of # of cols to skip. Used as a scroll delta */
	protected int scrollX;
	
	/** Scroll in the y direction, in terms of # of rows to skip. Used as a scroll delta */
	protected int scrollY;
	
	/** Constructor for MatrixPanel
	 * @param controller - the controller this MatrixPanel is doing something for.
	 * @param maxX    - the number of cols to paint at a time
	 * @param maxY    - the number of rows to paint at a time
	 * @param scrollX  - the starting scrolling of cols
	 * @param scrollY  - the starting scrolling of rows
	 * @param marginX - the number of cols of margin divided between left and right.
	 * @param marginY - the number of rows of margin divided between top and bottom.
	 */
	protected MatrixPanel(GameController controller, int maxX, int maxY, int scrollX, int scrollY, int marginX, int marginY){
		this.controller = controller;
		this.maxX = maxX;
		this.maxY = maxY;
		
		this.scrollX = scrollX;
		this.scrollY = scrollY;

		this.marginX = marginX;
		this.marginY = marginY;
	}
	
	/** Returns the number of rows this panel is showing currently */
	public int getShowedRows(){
		return maxY;
	}
	
	/** Returns the number of cols this panel is showing currently */
	public int getShowedCols(){
		return maxX;
	}
	
	/** Sets the number of rows this panel should show. Causes a repaint */
	protected void setShowedRows(int r){
		maxY = r;
		repaint();
	}
	
	/** Sets the number of rows this panel should show. Causes a repaint */
	protected void setShowedCols(int c){
		maxX = c;
		repaint();
	}
	
	/** Returns the width of the underlying matrix */
	protected abstract int getMatrixWidth();
	
	/** Returns the height of the underlying matrix */
	protected abstract int getMatrixHeight();
	
	/** Returns the currently selected elm, the one the cursor is hovering */
	protected abstract T getElm();
	
	/** Returns the element at the given indices. Throws IllegalArgumentException if this is OOB */
	protected abstract T getElmAt(int row, int col) throws IllegalArgumentException;
	
	/**
	 * Fixes the scroll to show the given row and column. Scrolls as little as possible to make this
	 * change
	 * row, col must be in range of matrix this is painting.
	 */
	public void fixScrollToShow(int row, int col) throws IllegalArgumentException{
		if(row < 0 || col < 0 || row > getMatrixHeight() || col > getMatrixWidth())
			throw new IllegalArgumentException("Can't show " + row + ", " + col + " : OOB");
		
		while(row < scrollY){
			scrollY--;
		}
		while(row > scrollY + maxY - 1){
			scrollY++;
		}
		while(col < scrollX){
			scrollX--;
		}
		while(col > scrollX + maxX - 1){
			scrollX++;
		}
		getFrame().repaint();
	}
	
	/** Returns the xPosition (graphically) of the top left corner of the given element */
	public int getXPosition(T t){
		return (t.getCol() - scrollX + marginX / 2) * getElementWidth();
	}
	
	/** Returns the yPosition (graphically) of the top left corner of the given tile */
	public int getYPosition(T t){
		return (t.getRow() - scrollY + marginY / 2) * getElementHeight();
	}
	
	/** Returns the height of a element in the drawing */
	protected abstract int getElementHeight();
	
	/** Returns the width of a element in the drawing */
	protected abstract int getElementWidth();
	
	/** Return the tile in the given direction from this tile.
	 * If oob, returns null or if direction invalid.
	 */
	T getElmInDirection(T t, Direction d){
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
	
	/** Returns the frame this is drawn in, using the frame maintained by the controller */
	public Frame getFrame(){
		return controller.frame;
	}
	
	/** Subclasses must have custom paint methods */
	@Override
	public abstract void paintComponent(Graphics g);
}
