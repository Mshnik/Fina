package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
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
	
	/** The texture for the background of the HeaderPanel */
	private int index = 0;

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
		g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, 30));
		if(game.getCurrentPlayer() == null){
			g2d.drawString("Setup Phase", 20, 25);
		} else{
			Player p = game.getCurrentPlayer();
			g2d.drawString(p.getCommander().name + " (p" + (game.getPlayerIndex() + 1) + ")", 15, 32);
		}
	}
	
}
