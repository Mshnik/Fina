package gui;

import game.Game;

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
	
	/** The choices to display on this DecisionPanel */
	private Decision[] choices;
	
	/** The drawing height of a Decision */
	protected static final int DECISION_HEIGHT = 40;
	
	/** The drawing width of an Decision */
	protected static final int DECISION_WIDTH = 120;
	
	/** A Cursor for this DecisionPanel */
	public final DecisionCursor cursor;
	
	/** The x coordinate of the top left corner of this panel */
	private int x;
	
	/** The y coordinate of the top left corner of this panel */
	private int y;
	
	/** The types of decisions that can be made */
	public enum Type{
		ACTION,
		END_OF_TURN
	}
	
	/** The type of this DecisionPanel - what kind of decision it is making */
	public final Type type;
	
	public DecisionPanel(Game g, Type t, int maxY, Decision[] choices) {
		super(g, 1, maxY, 0, 0);
		this.type = t;
		this.choices = choices;
		cursor = new DecisionCursor(this);
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
		return DECISION_WIDTH;
	}
	
	/** The height of this Panel is the height of all the decisions */
	@Override
	public int getHeight(){
		return DECISION_HEIGHT * choices.length;
	}
	
	/** Overrides super version by adding the x coordinate of this panel to super's result */
	@Override
	public int getXPosition(Decision elm){
		return super.getXPosition(elm) + x;
	}
	
	/** Overrides super version by adding the y coordinate of this panel to super's result */
	@Override
	public int getYPosition(Decision elm){
		return super.getYPosition(elm) + y;
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
		g2d.fillRect(x, y, DECISION_WIDTH, DECISION_HEIGHT * getMatrixHeight());
		g2d.setStroke(new BasicStroke(BORDER_WIDTH));
		g2d.setColor(BORDER);
		g2d.drawRect(x + BORDER_WIDTH/2, y + BORDER_WIDTH/2, 
				DECISION_WIDTH - BORDER_WIDTH, DECISION_HEIGHT * getMatrixHeight() - BORDER_WIDTH);
		
		g2d.setColor(TEXT_COLOR);
		g2d.setFont(TEXT_FONT);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		for(int r = 0; r < getMatrixHeight(); r++){
			g2d.drawString(choices[r].getMessage(), TEXT_X + x, TEXT_Y + y + r * DECISION_HEIGHT);
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
		return DECISION_WIDTH;
	}
}
