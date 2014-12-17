package gui.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import game.Game;
import gui.Frame;
import gui.ImageIndex;

import javax.swing.JPanel;

import unit.*;

public class InfoPanel extends JPanel{
	/***/
	private static final long serialVersionUID = 1L;

	/** The Height of the InfoPanel */
	protected static final int HEIGHT = 125;

	/** The color of the border surrounding the headerPanel */
	protected static final Color BORDER_COLOR = new Color(74, 47, 12);
	
	/** Distance (in pixels) between the top of the InfoPanel and the top of the bars */
	private static final int YMARGIN = 35;
	
	/** The game this InfoPanel is drawing info for */
	public final Game game;
	
	/** The Tile (if any) this InfoPanel is currently drawing info for */
	private Unit unit;
	
	public InfoPanel(Game g, GamePanel gp){
		game = g;
		setPreferredSize(new Dimension(gp.getShowedCols() * GamePanel.CELL_SIZE, HEIGHT));
	}
	
	/** Returns the tile this InfoPanel is currently drawing info for */
	public Unit getUnit(){
		return unit;
	}
	
	/** Sets the unit this InfoPanel is to draw info for, and causes a repaint */
	public void setUnit(Unit u){
		unit = u;
		repaint();
	}
	
	/** Paints this InfoPanel, the info for the currently selected unit */
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		//Background painting
		if(getHeight() == 0) return;
		for(int i = 0; i <= getWidth(); i+= getHeight()){
			g2d.drawImage(ImageIndex.SANDSTONE, i, 0, getHeight(), getHeight(), null);
		}
		g2d.setColor(BORDER_COLOR);
		int width = 7;
		g2d.setStroke(new BasicStroke(width));
		g2d.drawRect(width/2, width/2, getWidth() - width, getHeight() - width);
		
		g2d.setColor(Color.black);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, 20));
		
		//Tile painting
		//Stuff?
		
		
		//Unit painting
		if(unit != null){
			
			int x = 25;
			final int xInc = 225;
			
			
			g2d.drawString(unit.name, x, YMARGIN);
			final int infoFont = 16;
			g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, infoFont));
			g2d.drawString("(" + unit.getIdentifierString() + ")", x, YMARGIN + infoFont);
			if(unit.owner != null)
				g2d.drawString("Owned by " + unit.owner, x, YMARGIN + infoFont * 2);
			
			g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, infoFont - 3));
			x += xInc;
			int y = YMARGIN;
			UnitStats stats = unit.getStats();
			
			//Insert health at top here
			g2d.drawString("Health", x, y);
			g2d.drawString(unit.getHealth() + "", x + 145, y);
			y += infoFont;
			
			for(Stat s : stats.getStandardStatsList()){
				drawStat(g2d, s, x, y);
				y += infoFont;
			}
			x += xInc;
			y = YMARGIN;
			for(Stat s : stats.getAttackStatsList()){
				drawStat(g2d, s, x, y);
				y += infoFont;
			}
			x += xInc;
			y = YMARGIN;
			for(Stat s : stats.getMovementStatsList()){
				drawStat(g2d, s, x, y);				drawStat(g2d, s, x, y);
				drawStat(g2d, s, x, y);
				drawStat(g2d, s, x, y);
				drawStat(g2d, s, x, y);

				y += infoFont;
			}
		}
	}
	
	private void drawStat(Graphics2D g2d, Stat s, int x, int y){
		g2d.drawString(s.name.toString(), x, y);
		String str = s.val.toString();
		if(s.val instanceof AttackType){
			str = AttackType.getAbbrevString((AttackType) s.val);
		}
		g2d.drawString(str, x + 145, y);
	}
}
