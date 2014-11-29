package gui;

import game.Game;
import gui.Frame.Toggle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.HashSet;

import board.*;
import unit.*;

/** Drawable wrapper for a board object */
public class GamePanel extends MatrixPanel<Tile> implements Paintable{

	/***/
	private static final long serialVersionUID = 1L;

	/** Pixels (size) for each square tile. */
	protected static final int CELL_SIZE = 64; 
	
	/** Shading for fog of war - translucent black */
	protected static final Color FOG_OF_WAR = new Color(0,0,0,0.75f);

	/** The BoardCursor for this GamePanel */
	public final BoardCursor boardCursor;
	
	/** The DecisionPanel that is currently active. Null if none */
	private DecisionPanel decisionPanel;
	
	/** The PathSelector that is currently active. Null if none */
	private PathSelector pathSelector;

	/** Constructor for GamePanel
	 * @param g - the game to display using this panel
	 * @param maxRows - the maximum number of rows of tiles to show at a time
	 * @param maxCols - the maximum number of cols of tiles to show at a time
	 */
	public GamePanel(Game g, int maxRows, int maxCols){
		super(g, maxCols, maxRows, 0, 0);
		boardCursor = new BoardCursor(this);
		
		setPreferredSize(new Dimension(getShowedCols() * CELL_SIZE, getShowedRows() * CELL_SIZE));
	}
	
	/** Creates a new Decision Panel, shown to the side of the current tile (side depending
	 * on location of current tile)
	 * Starts decision toggling between moving.... //TODO
	 */
	public void startActionDecision(){
		Tile t = boardCursor.getElm();
		int x = getXPosition(t) - DecisionPanel.DECISION_WIDTH - 25;
		if(x < 0){
			x = getXPosition(t) + GamePanel.CELL_SIZE + 25;
		}
		int y = getYPosition(t);
		if(y + DecisionPanel.DECISION_HEIGHT > getHeight()){
			y = getYPosition(t) - DecisionPanel.DECISION_HEIGHT;
		}
		Decision[] decisions = {new Decision(0, "Move"), new Decision(1, "Attack"), new Decision(2, "Summon")};
		decisionPanel = new DecisionPanel(game, x, y, Math.min(3, decisions.length), 0, decisions);
		getFrame().addToggle(Toggle.DECISION);
		getFrame().setActiveCursor(decisionPanel.cursor);
		repaint();
	}
	
	/** Cancels the currently selected decision
	 * Throws a runtimeException if this was a bad time to cancel because decision wasn't happening. */
	public void cancelDecision() throws RuntimeException{
		Toggle t = getFrame().removeTopToggle();
		if(! t.equals(Toggle.DECISION))
			throw new RuntimeException("Can't cancel decision, currently toggling " + getFrame().getToggle());
		getFrame().getAnimator().removeAnimatable(decisionPanel.cursor);
		decisionPanel = null;
		getFrame().setActiveCursor(boardCursor);
		repaint();
	}
	
	/** Processes the currently selected decision.
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processDecision() throws RuntimeException{
//		Toggle t = getFrame().removeTopToggle();
//		if(! t.equals(Toggle.DECISION))
//			throw new RuntimeException("Can't cancel path selection, currently toggling " + getFrame().getToggle());
		//TODO
		cancelDecision();
	}
	
	/** Creates a new pathSelector at the current boardCursor position.
	 * Does nothing if the current tile is unoccupied or the unit has already moved. */
	public void startPathSelection(){
		Tile t = boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canMove()) return;
		
		pathSelector = new PathSelector(this, (MovingUnit) t.getOccupyingUnit());
		getFrame().addToggle(Frame.Toggle.PATH_SELECTION);
	}
	
	/** Cancels the path selection - deletes it but does nothing.
	 * Throws a runtimeException if this was a bad time to cancel because pathSelection wasn't happening. */
	public void cancelPathSelection() throws RuntimeException{
		Toggle t = getFrame().removeTopToggle();
		if(! t.equals(Toggle.PATH_SELECTION))
			throw new RuntimeException("Can't cancel path selection, currently toggling " + getFrame().getToggle());
		if(pathSelector != null) boardCursor.setElm(pathSelector.getPath().getFirst());
		pathSelector = null;
	}
	
	/** Processes the path selection - if ok, deletes it.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processPathSelection() throws RuntimeException{
		Toggle t = getFrame().removeTopToggle();
		if(! t.equals(Toggle.PATH_SELECTION))
			throw new RuntimeException("Can't cancel path selection, currently toggling " + getFrame().getToggle());
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
	
	/** Returns the frame this is drawn within */
	public Frame getFrame(){
		return game.getFrame();
	}

	@Override
	/** Paints this boardpanel, for use in the frame it is in. */
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		
		HashSet<Tile> vision = null;
		if(game.getCurrentPlayer() != null){
			g2d.setColor(FOG_OF_WAR);
		}
		//Paint the board itself, painting the portion within
		//[scrollY ... scrollY + maxY - 1], 
		//[scrollX ... scrollX + maxX - 1]
		for(int row = scrollY; row < scrollY + getShowedRows(); row ++){
			for(int col = scrollX; col < scrollX + getShowedCols(); col ++){
				Tile t = game.board.getTileAt(row, col);
				int x = getXPosition(t);
				int y = getYPosition(t);
				//Draw terrain
				g.drawImage(ImageIndex.imageForTile(t), x, y,
					CELL_SIZE, CELL_SIZE, null);
				
				//If the player can't see this tile, shade darkly.
				if(game.isFogOfWar() && game.getCurrentPlayer() != null 
						&& ! game.getCurrentPlayer().canSee(t)){
					g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
				}
				
				//Draw a unit if necessary - only if player can see it.
				if(t.isOccupied() && (! game.isFogOfWar() || vision == null || vision.contains(t))){
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
		
		//Draw the decisionPanel
		if(decisionPanel != null){
			decisionPanel.paintComponent(g);
		}
	}

	/** Returns the tile at the given row and col. Ignores scrolling for this. */
	@Override
	public Tile getElmAt(int row, int col) throws IllegalArgumentException {
		return game.board.getTileAt(row, col);
	}

	/** Returns the width of the board's matrix */
	@Override
	public int getMatrixWidth() {
		return game.board.getWidth();
	}

	/** Returns the height of the board's matrix */
	@Override
	public int getMatrixHeight() {
		return game.board.getHeight();
	}

	/** Returns GamePanel.CELL_SIZE */
	@Override
	public int getElementHeight() {
		return GamePanel.CELL_SIZE;
	}

	/** Returns GamePanel.CELL_SIZE */
	@Override
	public int getElementWidth() {
		return GamePanel.CELL_SIZE;
	}

}
