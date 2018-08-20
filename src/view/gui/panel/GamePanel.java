package view.gui.panel;


import java.awt.*;
import java.awt.image.BufferedImage;

import controller.decision.Decision;
import controller.game.GameController;
import controller.selector.CastSelector;

import view.gui.*;
import view.gui.Frame;
import view.gui.decision.*;

import model.board.*;
import model.game.*;
import model.unit.*;
import model.unit.ability.Ability;

/** Drawable wrapper for a model.board object */
public final class GamePanel extends MatrixPanel<Tile> implements Paintable{

	/***/
	private static final long serialVersionUID = 1L;

	/** Pixels (size) for each square tile. */
	public static final int CELL_SIZE = 64; 

	/** Shading for fog of war - translucent black */
	private static final Color FOG_OF_WAR = new Color(0,0,0,0.75f);
	
	/** Stroke for drawing effect radii */
	private static final Stroke RADIUS_STROKE = new BasicStroke(3);
	
	/** Color of attack radii */
	private static final Color ATTACK_COLOR = Color.red;
	
	/** Color of summon/build radii */
	private static final Color SUMMON_COLOR = Color.cyan;
	
	/** Color of cast radii */
	private static final Color CAST_COLOR = Color.magenta;

	/** The BoardCursor for this GamePanel */
	public final BoardCursor boardCursor;

	/** The DecisionPanel that is currently active. Null if none */
	private DecisionPanel decisionPanel;

	/** Constructor for GamePanel
	 * @param f
	 * @param maxRows - the maximum number of rows of tiles to show at a time
	 * @param maxCols - the maximum number of cols of tiles to show at a time
	 */
	public GamePanel(Frame f, int maxRows, int maxCols){
		super(f.getController(), maxCols, maxRows, 0, 0,
				Math.max(0, maxCols - f.getController().game.board.getWidth()),
				Math.max(0, maxRows - f.getController().game.board.getHeight()));
		boardCursor = new BoardCursor(this);
		setPreferredSize(new Dimension(getShowedCols() * CELL_SIZE, getShowedRows() * CELL_SIZE));
	}

	/**
	 * Sets the decisionPanel
	 */
	public void setDecisionPanel(DecisionPanel d){
		decisionPanel = d;
	}
	
	/** Returns the current active decision panel, if any */
	public DecisionPanel getDecisionPanel(){
		return decisionPanel;
	}
	
	/**
	 * Creates a decisionPanel with the given decisionArray.
	 * Fixes the location of the open decisionPanel for the location of the boardCursor,
	 * sets active toggle and active cursor, and repaints.
	 */
	public void fixDecisionPanel(String title, 
			Player p, Decision decision){
		decisionPanel = new DecisionPanel(controller, p, 
				Math.min(4, decision.size()), title, decision);
		getFrame().setActiveCursor(decisionPanel.cursor);
	}

	/** Moves the decision panel to accomidate the current location of the boardCursor */
	public void moveDecisionPanel(){
		Tile t = boardCursor.getElm();
		int x = getXPosition(t);
		if(x < getWidth() / 2){
			x += GamePanel.CELL_SIZE + 5;
		} else{
			x -= (decisionPanel.DECISION_WIDTH + 5);
		}
		int y = getYPosition(t);
		if(y > getHeight() / 2){
			y = getYPosition(t) - decisionPanel.getHeight() + GamePanel.CELL_SIZE;
		}
		decisionPanel.setXPosition(x);
		decisionPanel.setYPosition(y);
		repaint();
	}

	/** Moves the decision panel to the center of the screen */
	public void centerDecisionPanel(){
		int w = decisionPanel.getWidth();
		int h = decisionPanel.getHeight();

		decisionPanel.setXPosition( (getWidth() - w) / 2);
		decisionPanel.setYPosition( (getHeight() - h) / 2);
		repaint();
	}

	/**
	 * Returns true if the given model.unit is visible to the current player, false otherwise.
	 * Helper for painting fog of war and hiding units
	 */
	private boolean isVisible(Tile t){
		Game game = controller.game;
		return ! game.isFogOfWar() || 
				(game.getCurrentPlayer() != null && game.getCurrentPlayer().canSee(t));
	}

	/** Paints this boardpanel, for use in the frame it is in. */
	@Override
	public void paintComponent(Graphics g){
		Game game = controller.game;
		Graphics2D g2d = (Graphics2D)g;
		//Paint the model.board itself, painting the portion within
		//[scrollY ... scrollY + maxY - 1], 
		//[scrollX ... scrollX + maxX - 1]

		int marginRowTop = marginY/2;
		int marginRowBottom = marginY - marginRowTop;
		int marginColLeft = marginX/2;
		int marginColRight = marginX - marginColLeft;

		for(int row = 0; row < getShowedRows(); row ++){
			for(int col = 0; col < getShowedCols(); col ++){
				if (row < marginRowTop || row >= (getShowedRows() - marginRowBottom)
						|| col < marginColLeft || col >= (getShowedCols() - marginColRight)) {
					g2d.drawImage(ImageIndex.margin(),
							col * getElementWidth(),
							row * getElementHeight(),
							CELL_SIZE, CELL_SIZE, null);
				} else {
					Tile t = game.board.getTileAt(row + scrollY - marginRowTop, col + scrollX - marginColLeft);
					drawTile(g2d, t);
					if (t.isOccupied() && isVisible(t)) {
						drawUnit(g2d, t.getOccupyingUnit());
					}
				}
			}
		}

		//Draw the locationSelector, if there is one
		if(controller.getLocationSelector() != null){			
			controller.getLocationSelector().paintComponent(g);
		}

		//Depending on the decision panel, draw ranges as applicable.
		//This happens when the action menu is up
		if(decisionPanel != null 
				&& controller.getDecisionType() == Decision.DecisionType.ACTION_DECISION){
			g2d.setStroke(RADIUS_STROKE);
			switch(decisionPanel.cursor.getElm().getMessage()){
			case GameController.FIGHT:
				g2d.setColor(ATTACK_COLOR);
				ImageIndex.trace(controller.game.board.getRadialCloud(boardCursor.getElm(), 
						boardCursor.getElm().getOccupyingUnit().getAttackRange() + 1), 
						this, g2d);
				break;
			case GameController.BUILD:
			case GameController.SUMMON:
				g2d.setColor(SUMMON_COLOR);
				ImageIndex.trace(controller.game.board.getRadialCloud(boardCursor.getElm(), 
						boardCursor.getElm().getOccupyingUnit().getSummonRange()), 
						this, g2d);
				break;
			}
		}
		else if(
			decisionPanel != null && controller.getDecisionType() == Decision.DecisionType.CAST_DECISION
			|| controller.getLocationSelector() != null && controller.getToggle() == GameController.Toggle.CAST_SELECTION
			){
			Ability a = null;
			if(decisionPanel != null) a = (Ability) decisionPanel.getElm().getVal();
			else a = ((CastSelector) controller.getLocationSelector()).toCast;
			
			g2d.setStroke(RADIUS_STROKE);
			g2d.setColor(CAST_COLOR);
			ImageIndex.trace(a.getEffectCloud(boardCursor.getElm()), 
					this, g2d);
		}

		//Draw the cursor
		boardCursor.paintComponent(g);

		//Draw the decisionPanel
		if(decisionPanel != null){
			decisionPanel.paintComponent(g);
		}
	}

	/** Draws the given tile. Doesn't do any model.unit drawing. */
	private void drawTile(Graphics2D g2d, Tile t){
		int x = getXPosition(t);
		int y = getYPosition(t);
		//Draw terrain
		g2d.drawImage(ImageIndex.imageForTile(t), x, y,
				CELL_SIZE, CELL_SIZE, null);

		//If the player can't see this tile, shade darkly.
		if(! isVisible(t)){
			g2d.setColor(FOG_OF_WAR);
			g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
		}
	}

	/** Draws the given model.unit. Doesn't do any tile drawing. */
	private void drawUnit(Graphics2D g2d, Unit u){
		int x = getXPosition(u.getLocation());
		int y = getYPosition(u.getLocation());

		//Draw model.unit
		BufferedImage unitImg =ImageIndex.imageForUnit(u);
		g2d.drawImage(ImageIndex.tint(unitImg, controller.getColorFor(u.owner)), x, y, 
				CELL_SIZE, CELL_SIZE, null);

		//Draw health bar
		final int marginX = 4; //Room from left side of tile
		final int marginY = 4; //Room from BOTTOM side of tile
		final int barX = x + marginX;
		final int barY = y + CELL_SIZE - marginY * 2;
		ImageIndex.drawBar(g2d, barX, barY, CELL_SIZE - marginX * 2, marginY, 
				null, null, 0, Color.red, u.getMaxHealth(), u.getHealthPercent(), 
				null, null, null, null, 0);

	}

	/** Returns the currently selected element */
	public Tile getElm(){
		return boardCursor.getElm();
	}

	/** Returns the tile at the given row and col. Ignores scrolling for this. */
	@Override
	public Tile getElmAt(int row, int col) throws IllegalArgumentException {
		return controller.game.board.getTileAt(row, col);
	}

	/** Returns the width of the model.board's matrix */
	@Override
	public int getMatrixWidth() {
		return controller.game.board.getWidth();
	}

	/** Returns the height of the model.board's matrix */
	@Override
	public int getMatrixHeight() {
		return controller.game.board.getHeight();
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
