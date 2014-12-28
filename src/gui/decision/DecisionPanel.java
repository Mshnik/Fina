package gui.decision;

import game.Game;
import game.Player;
import gui.Frame;
import gui.MatrixPanel;
import gui.Paintable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

 
/** Panel on the bottom of the frame that shows text as necessary for
 * decisions
 * @author MPatashnik
 *
 */
public class DecisionPanel extends MatrixPanel<Decision> implements Paintable {
	
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
	
	/** The choices to display on this DecisionPanel */
	private Decision[] choices;
	
	/** The drawing height of a Decision */
	public static final int DECISION_HEIGHT = 40;
	
	/** The drawing width of an Decision */
	public static final int DECISION_WIDTH = 120;
	
	/** A Cursor for this DecisionPanel */
	public final DecisionCursor cursor;
	
	/** The x coordinate of the top left corner of this panel */
	private int x;
	
	/** The y coordinate of the top left corner of this panel */
	private int y;
	
	/** True if this is manditory (can't be canceled), false otherwise */
	public final boolean manditory;
	
	/** The player this decision is for */
	public final Player player;
	
	/** The types of decisions that can be made */
	public enum Type{
		ACTION_DECISION,
		SUMMON_DECISION,
		CAST_DECISION,
		NEW_ABILITY_DECISION,
		END_OF_TURN_DECISION
	}
	
	/** The type of this DecisionPanel - what kind of decision it is making */
	public final Type type;
	
	public DecisionPanel(Game g, Player p, Type t, boolean manditory, 
			int maxY, String title, Decision[] choices) {
		super(g, 1, maxY, 0, 0);
		this.player = p;
		this.type = t;
		this.manditory = manditory;
		this.choices = choices;
		this.title = title;
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
		return getElementHeight() * (Math.min(super.getShowedRows(), choices.length) + 1);
	}
	
	/** Overrides super version by adding the x coordinate of this panel to super's result */
	@Override
	public int getXPosition(Decision elm){
		return super.getXPosition(elm) + x;
	}
	
	/** Overrides super version by adding the y coordinate of this panel to super's result */
	@Override
	public int getYPosition(Decision elm){
		return super.getYPosition(elm) + y + DECISION_HEIGHT;
	}
	
	/** Returns the decision the cursor is currently hovering *
	 * 
	 */
	public Decision getElm(){
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
			if(choices[r].isSelectable())
				g2d.setColor(TEXT_COLOR);
			else
				g2d.setColor(TEXT_COLOR.darker());
			g2d.drawString(choices[r].getMessage(), TEXT_X + x, TEXT_Y + y + (r - scrollY + 1) * DECISION_HEIGHT);
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
		return choices.length;
	}

	/** Returns the action at the given index. Col must be 0 */
	@Override
	public Decision getElmAt(int row, int col)
			throws IllegalArgumentException {
		if(col != 0)
			throw new IllegalArgumentException("Can't get decision at col != 0");
		try{
			return choices[row];
		} catch(ArrayIndexOutOfBoundsException e){
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
		return DECISION_WIDTH + (type == Type.SUMMON_DECISION ? 100 : 0);
	}
	
	@Override
	public String toString(){
		String s = type + " Decision Panel";
		for(Decision d : choices){
			s += d.toString() + ", ";
		}
		return s.substring(0, s.length() - 2);
	}
}
