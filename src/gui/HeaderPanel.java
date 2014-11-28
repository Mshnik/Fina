package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import game.Game;
import game.Player;

import javax.swing.JPanel;

/** The panel at the top of the frame that shows basic information and
 * the current player's turn
 * @author MPatashnik
 *
 */
public class HeaderPanel extends JPanel {

	/***/
	private static final long serialVersionUID = 1L;

	/** The Height of the HeaderPanel */
	protected static final int HEIGHT = 50;

	/** Distance (in pixels) between the top of the HeaderPanel and the top of the bars */
	private static final int MARGIN = 15;

	/** Width of the stroke used to make the bars */
	private static final int STROKE = 4;

	/** X coordinate of the start of the bars.
	 */
	protected static final int X_BAR_START = 275;

	/** Color of text used to show mana and health bars */
	protected static final Color TEXT_COLOR = Color.white;

	/** Color behind the bars if the bars aren't totally full */
	protected static final Color BACK_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.2f);

	/** Width of the health bar */
	protected static final int BAR_WIDTH = 200;

	/** The border color for the health bar */
	protected static final Color HEALTH_BORDER = new Color(153, 15, 0);

	/** The filled in color for the health bar */
	protected static final Color HEALTH_FILL = new Color(242, 33, 10);

	/** The border color for the mana bar */
	protected static final Color MANA_BORDER = new Color(0, 53, 145);

	/** The filled in color for the mana bar */
	protected static final Color MANA_FILL = new Color(9, 93, 237);

	/** The filled in color for the manaPerTurn cap in the mana bar */
	protected static final Color MANA_PER_TURN_FILL = new Color(9, 169, 237);

	/** The border color for the exp bar */
	protected static final Color EXP_BORDER = new Color(201, 186, 18);
	
	/** The filled in color for the exp bar */
	protected static final Color EXP_FILL = new Color(255, 237, 43);
	
	/** The game this HeaderPanel is drawing info for */
	public final Game game;

	public HeaderPanel(Game g, GamePanel gp){
		game = g;
		setPreferredSize(new Dimension(gp.maxX * GamePanel.CELL_SIZE, HEIGHT));
	}

	/** Draws the HeaderPanel */
	@Override
	public void paintComponent(Graphics g){

		//Draw background and border
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(new Color(219, 167, 99));
		g2d.fillRect(0, 0, getWidth(), getHeight());

		g2d.setColor(new Color(74, 47, 12));
		int width = 6;
		g2d.setStroke(new BasicStroke(width));
		g2d.drawRect(width/2, width/2, getWidth() - width, getHeight() - width);

		g2d.setColor(Color.black);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, 20));
		if(game.getCurrentPlayer() == null){
			g2d.drawString("Setup Phase", 20, 25);
		} else{
			Player p = game.getCurrentPlayer();
			g2d.drawString(p.getCommander().name + " (p" + (game.getPlayerIndex() + 1) + ")", 15, 32);

			g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, 10));
			g2d.setStroke(new BasicStroke(STROKE));

			//Health bar
			drawBar(g2d, X_BAR_START, HEALTH_BORDER, HEALTH_FILL,  
					(double)p.getHealth() / (double)p.getMaxHealth(), 
					p.getHealth() + "/" + p.getMaxHealth());

			//Mana bar - bit of custom work because of manaPerTurn
			final int MANA_START = (int)(X_BAR_START + BAR_WIDTH * 1.125);
			drawBar(g2d, MANA_START, MANA_BORDER, MANA_FILL,  
					(double)p.getMana() / (double)p.getMaxMana(), 
					"");

			double pManaPT = Math.min((double)
					(p.getMaxMana() - p.getMana())/(double)p.getMaxMana(),
					(double)p.getManaPerTurn() / (double)p.getMaxMana());
			g2d.setColor(MANA_PER_TURN_FILL);
			g2d.fillRect(MANA_START + STROKE/2 + (int)((BAR_WIDTH - STROKE/2 - 1) * (double)p.getMana() / (double)p.getMaxMana()), 
					MARGIN + STROKE/2, 
					(int)((BAR_WIDTH - STROKE/2 - 1) * pManaPT), HEIGHT - MARGIN *2 - STROKE/2 - 1);
			g2d.setColor(TEXT_COLOR);
			String sym = "+";
			if(p.getManaPerTurn() < 0) sym = "-";
			g2d.drawString(p.getMana() + "(" + sym + Math.abs(p.getManaPerTurn()) + ") /" + p.getMaxMana(), 
					MANA_START + BAR_WIDTH/2 - 10, HEIGHT/2 + STROKE);

			//Level and exp bar
			final int LEVEL_START = (int)(MANA_START + BAR_WIDTH * 1.125);
			drawBar(g2d, LEVEL_START, EXP_BORDER, EXP_FILL,  
					(double)p.getExp() / 1.0, 
					(int)(p.getExp() * 100) + "/" + 100);
		}
	}

	/** Draws a bar with the border and fill colors, full the given amount
	 * @param g2d	- a graphics object to draw with
	 * @param X		- the x coordinate of the top left corner
	 * @param border - the color with which to draw the border.
	 * @param fill	- the color with which to fill.
	 * @param percentFull - the percentage to fill
	 * @param text	- the text to draw.
	 */
	private void drawBar(Graphics2D g2d, final int X, Color border, Color fill, double percentFull, String text){
		g2d.setColor(border);
		g2d.drawRect(X, MARGIN, BAR_WIDTH, HEIGHT - MARGIN * 2);
		g2d.setColor(BACK_COLOR);
		g2d.fillRect(X + STROKE/2, MARGIN + STROKE/2, 
				BAR_WIDTH - STROKE/2 - 1, HEIGHT - MARGIN *2 - STROKE/2 - 1);
		g2d.setColor(fill);
		g2d.fillRect(X + STROKE/2, MARGIN + STROKE/2, 
				(int)((BAR_WIDTH - STROKE/2 - 1) * percentFull), HEIGHT - MARGIN *2 - STROKE/2 - 1);
		g2d.setColor(TEXT_COLOR);
		g2d.drawString(text, X + BAR_WIDTH/2 - 10, HEIGHT/2 + STROKE);
	}

}
