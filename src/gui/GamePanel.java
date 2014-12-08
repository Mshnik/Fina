package gui;

import game.Game;
import game.Player;
import gui.Frame.Toggle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.LinkedList;

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
	
	/** Cancels the currently selected decision for any decision
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
	
	/** Processes the currently selected Actiondecision.
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processActionDecision() throws RuntimeException{
		if(! decisionPanel.cursor.canSelect()) return;
		String choice = decisionPanel.getSelectedDecisionMessage();
		cancelDecision();
		switch(choice){
		case Unit.MOVE:
			startPathSelection();
			break;
		case Unit.SUMMON:
			startSummonDecision();
			break;
		default:
			break;
		}
	}
	
	/** Returns the current active decision panel, if any */
	public DecisionPanel getDecisionPanel(){
		return decisionPanel;
	}
	
	/** Starts a decisionPanel for ending the current player's turn */
	public void startEndTurnDecision(){
		Decision[] decisionsArr = {new Decision(0, Game.CANCEL), new Decision(1, Game.END_TURN)};
		fixDecisionPanel(DecisionPanel.Type.END_OF_TURN, decisionsArr);
	}
	
	/** Processes an endOfTurn Decision */
	public void processEndTurnDecision(){
		if(! decisionPanel.cursor.canSelect()) return;
		String m = decisionPanel.getSelectedDecisionMessage();
		cancelDecision();
		switch(m){
		case Game.END_TURN:
			game.getCurrentPlayer().turnEnd();
			break;
		}
	}

	/** Creates a new Decision Panel for doing things with a unit, 
	 * shown to the side of the current tile (side depending
	 * on location of current tile)
	 * If there is no unit on the given tile or the unit doesn't belong to the current player, does nothing.
	 * If the unit on the tile, populates decisionPanel with all the possible decisions
	 */
	public void startActionDecision(){
		Tile t = boardCursor.getElm();
		if(! boardCursor.canSelect()) return;
		if(t.getOccupyingUnit() == null || t.getOccupyingUnit().owner != game.getCurrentPlayer()){
			return;
		} 
		Unit u = t.getOccupyingUnit();
		
		//Add choices based on the unit on this tile
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		int i = 0;
		if(u.canMove()){
			decisions.add(new Decision(i++, Unit.MOVE));
		}
		if(u.canFight()){
			decisions.add(new Decision(i++, Unit.FIGHT));
		}
		if(u.canSummon()){
			decisions.add(new Decision(i++, Unit.SUMMON));
		}
		
		//If there are no applicable choices, do nothing
		if(decisions.isEmpty()) return;
		
		//Otherwise, convert to array, create panel, set correct location on screen.
		Decision[] decisionsArr = decisions.toArray(new Decision[decisions.size()]);
		fixDecisionPanel(DecisionPanel.Type.ACTION, decisionsArr);
	}
	
	/** Creates a decisionPanel for summoning new units */
	public void startSummonDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		int i = 0;
		for(Unit u : c.getSummonables()){
			boolean ok = u.manaCost <= c.getMana();
			decisions.add(new Decision(i++, ok, u.name + " (" + u.manaCost + ")"));
		}
		Decision[] decisionsArr = decisions.toArray(new Decision[decisions.size()]);
		fixDecisionPanel(DecisionPanel.Type.SUMMON, decisionsArr);
	}
	
	/** Creates a decisionPanel with the given decisionArray. 
	 * Fixes the location of the open decisionPanel for the location of the boardCursor,
	 * sets active toggle and active cursor, and repaints.
	 */
	private void fixDecisionPanel(DecisionPanel.Type type, Decision[] decisionsArr){
		decisionPanel = new DecisionPanel(game, type, Math.min(3, decisionsArr.length), decisionsArr);
		moveDecisionPanel();
		getFrame().addToggle(Toggle.DECISION);
		getFrame().setActiveCursor(decisionPanel.cursor);
		repaint();
	}
	
	/** Moves the decision panel to accomidate the current location of the boardCursor *?
	 *
	 */
	private void moveDecisionPanel(){
		Tile t = boardCursor.getElm();
		int x = getXPosition(t);
		if(x < getWidth() / 2){
			x += GamePanel.CELL_SIZE + 5;
		} else{
			x -= (DecisionPanel.DECISION_WIDTH + 5);
		}
		int y = getYPosition(t);
		if(y > getHeight() / 2){
			y = getYPosition(t) - decisionPanel.getHeight() + GamePanel.CELL_SIZE;
		}
		decisionPanel.setXPosition(x);
		decisionPanel.setYPosition(y);
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
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in path selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processPathSelection() throws RuntimeException{
		if(! boardCursor.canSelect()) return;
		if(pathSelector.getPath().size() < 2) return;
		Toggle t = getFrame().removeTopToggle();
		if(! t.equals(Toggle.PATH_SELECTION))
			throw new RuntimeException("Can't cancel path selection, currently toggling " + getFrame().getToggle());
		try{
			pathSelector.unit.move(pathSelector.getPath());
			pathSelector = null;
			startActionDecision();
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

	/** Returns true if the given unit is visible to the current player, false otherwise.
	 * Helper for painting fog of war and hiding units */
	private boolean isVisible(Tile t){
		 return ! game.isFogOfWar() || 
				 (game.getCurrentPlayer() != null && game.getCurrentPlayer().canSee(t));
	}
	
	@Override
	/** Paints this boardpanel, for use in the frame it is in. */
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;

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
				if(! isVisible(t)){
					g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
				}

				//Draw a unit if necessary - only if player can see it.
				if(t.isOccupied() && isVisible(t)){
					g2d.drawImage(ImageIndex.imageForUnit(t.getOccupyingUnit()), x, y, 
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
