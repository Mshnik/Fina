package view.gui.decision;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import controller.decision.Choice;
import controller.decision.Decision;
import controller.game.GameController;

import view.gui.Frame;
import view.gui.MatrixPanel;
import view.gui.Paintable;

import model.game.Player;

 
/** Panel on the bottom of the frame that shows text as necessary for
 * decisions
 * @author MPatashnik
 *
 */
public class DecisionPanel extends MatrixPanel<Choice> implements Paintable {
	
	/***/
	private static final long serialVersionUID = 1L;
	
	/** Background color for DecisionPanel */
	protected static final Color BACKGROUND = new Color(117, 74, 0);
	
	/** Border color for DecisionPanel */
	protected static final Color BORDER = new Color(145, 116, 0); 
	
	/** Width of borders for DecisionPanel */
	protected static final int BORDER_WIDTH = 4;
	
	/** Text color */
	protected static final Color TEXT_COLOR = Color.white;
	
	/** Font for DecisionPanel */
	protected static final Font TEXT_FONT = new Font(Frame.FONTNAME, Font.BOLD, 17);
	
	/** Text x margin */
	protected static final int TEXT_X = 17;
	
	/** Text y margin */
	protected static final int TEXT_Y = 25;
	
	/** The title of this decisionPanel, to paint at the top */
	private String title;
	
	/** The decision to display on this DecisionPanel */
	private Decision decision;
	
	/** The drawing height of a Decision */
	public static final int DECISION_HEIGHT = 40;
	
	/** The drawing width of an Decision */
	public final int DECISION_WIDTH;
	
	/** A Cursor for this DecisionPanel */
	public final DecisionCursor cursor;
	
	/** The x coordinate of the top left corner of this panel */
	private int x;
	
	/** The y coordinate of the top left corner of this panel */
	private int y;
	
	/** The player this decision is for */
	public final Player player;
	
	public DecisionPanel(GameController g, Player p,
			int maxY, String title, Decision decision) {
		super(g, 1, maxY, 0, 0);
		this.player = p;
		this.decision = decision;
		this.title = title;
		
		//Determine width of panel based on all text 
		int maxWidth = controller.frame.getTextWidth(TEXT_FONT, title);
		for(Choice c : decision){
			maxWidth = Math.max(maxWidth, controller.frame.getTextWidth(TEXT_FONT, c.getMessage()));
		}
		DECISION_WIDTH = maxWidth  + TEXT_X * 2; //Add margins for either side
		
		cursor = new DecisionCursor(this);
		cursor.moved();
	}
	
	/** Sets the x position of this DecisionPanel */
	public void setXPosition(int xPos){
		x = xPos;
	}
	
	/** Sets the y position of this DecisionPanel */
	public void setYPosition(int yPos){
		y = yPos;
	}
	
	/** The width of this Panel is the width of a single decision */
	@Override
	public int getWidth(){
		return getElementWidth();
	}
	
	/** The height of this Panel is the height of all the decisions and the title */
	@Override
	public int getHeight(){
		return getElementHeight() * (Math.min(super.getShowedRows(), decision.size()) + 1);
	}
	
	/** Overrides super version by adding the x coordinate of this panel to super's result */
	@Override
	public int getXPosition(Choice elm){
		return super.getXPosition(elm) + x;
	}
	
	/** Overrides super version by adding the y coordinate of this panel to super's result */
	@Override
	public int getYPosition(Choice elm){
		return super.getYPosition(elm) + y + DECISION_HEIGHT;
	}
	
	/** Returns the decision the cursor is currently hovering *
	 * 
	 */
	public Choice getElm(){
		return cursor.getElm();
	}
	
	/** Returns the decision message of the decision that is currently selected */
	public String getSelectedDecisionMessage(){
		return cursor.getElm().getMessage();
	}
	
	/** Draws this DecisionPanel */
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(BACKGROUND);
		g2d.fillRect(x, y, getWidth(), getHeight());
		g2d.setStroke(new BasicStroke(BORDER_WIDTH));
		g2d.setColor(BORDER);
		g2d.drawRect(x + BORDER_WIDTH/2, y + BORDER_WIDTH/2, 
				getWidth() - BORDER_WIDTH, getHeight() - BORDER_WIDTH);
		
		//Title section
		g2d.setColor(BACKGROUND.brighter());
		g2d.fillRect(x, y, getWidth(), DECISION_HEIGHT);
		
		g2d.setFont(TEXT_FONT);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		//Title
		g2d.setColor(TEXT_COLOR);
		g2d.drawString(title, TEXT_X + x, TEXT_Y + y);
		
		for(int r = scrollY; r < scrollY + Math.min(super.getShowedRows(), getMatrixHeight()); r++){
			if(decision.get(r).isSelectable())
				g2d.setColor(TEXT_COLOR);
			else
				g2d.setColor(TEXT_COLOR.darker());
			g2d.drawString(decision.get(r).getMessage(), TEXT_X + x, TEXT_Y + y + (r - scrollY + 1) * DECISION_HEIGHT);
		}
		
		//Draw cursor
		cursor.paintComponent(g);
	}

	/** DecisionPanels are of a 1 dimensional array, so the height is 1 */
	@Override
	public int getMatrixWidth() {
		return 1;
	}


	/** Returns choices.length, the number of choices this decision panel is showing */
	@Override
	public int getMatrixHeight() {
		return decision.size();
	}

	/** Returns the action at the given index. Col must be 0 */
	@Override
	public Choice getElmAt(int row, int col)
			throws IllegalArgumentException {
		if(col != 0)
			throw new IllegalArgumentException("Can't get decision at col != 0");
		try{
			return decision.get(row);
		} catch(IndexOutOfBoundsException e){
			throw new IllegalArgumentException("Can't get decision at row " + row + ": OOB");
		}
	}

	/** Returns DecisionPanel.DECISION_HEIGHT */
	@Override
	public int getElementHeight() {
		return DECISION_HEIGHT;
	}

	/** Returns DecisionPanel.DECISION_WIDTH */
	@Override
	public int getElementWidth() {
		return DECISION_WIDTH;
	}
	
	@Override
	public String toString(){
		String s = "";
		for(Choice d : decision){
			s += d.toString() + ", ";
		}
		return s.substring(0, s.length() - 2);
	}
}
