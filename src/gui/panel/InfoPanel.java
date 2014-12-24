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
import unit.ability.*;
import unit.modifier.Modifier;
import unit.modifier.ModifierBundle;
import unit.stat.Stat;
import unit.stat.Stats;

public class InfoPanel extends JPanel{
	/***/
	private static final long serialVersionUID = 1L;

	/** The Height of the InfoPanel */
	protected static final int HEIGHT = 125;

	/** The color of the border surrounding the headerPanel */
	protected static final Color BORDER_COLOR = new Color(74, 47, 12);
	
	/** Distance (in pixels) between the top of the InfoPanel and the top of the bars */
	private static final int YMARGIN = 35;
	
	/** Font for drawing title text */
	private static final Font BIG_FONT = new Font(Frame.FONTNAME, Font.BOLD, 20);
	
	/** Font for drawing standard text */
	private static final Font SMALL_FONT = new Font(Frame.FONTNAME, Font.BOLD, 16);
	
	/** The game this InfoPanel is drawing info for */
	public final Game game;
	
	/** The Unit (if any) this InfoPanel is currently drawing info for */
	private Unit unit;
	
	/** The Ability (if any) this InfoPanel is currently drawing info for */
	private Ability ability;
	
	public InfoPanel(Game g, GamePanel gp){
		game = g;
		setPreferredSize(new Dimension(gp.getShowedCols() * GamePanel.CELL_SIZE, HEIGHT));
	}
	
	/** Returns the tile this InfoPanel is currently drawing info for */
	public Unit getUnit(){
		return unit;
	}
	
	/** Returns the Ability this InfoPanel is currently drawing info for */
	public Ability getAbility(){
		return ability;
	}
	
	/** Sets the unit this InfoPanel is to draw info for, and causes a repaint */
	public void setUnit(Unit u){
		unit = u;
		ability = null;
		repaint();
	}
	
	/** Sets the unit this InfoPanel is to draw info for, and causes a repaint */
	public void setAbility(Ability a){
		unit = null;
		ability = a;
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
		g2d.setFont(BIG_FONT);
		
		//Unit painting
		if(unit != null){
			int x = 25;
			final int xInc = 225;
			
			g2d.drawString(unit.name, x, YMARGIN);
			g2d.setFont(SMALL_FONT);
			final int infoFont = SMALL_FONT.getSize();
			g2d.drawString("(" + unit.getIdentifierString() + ")", x, YMARGIN + infoFont);
			if(unit.owner != null)
				g2d.drawString("Owned by " + unit.owner, x, YMARGIN + infoFont * 2);
			
			g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, infoFont - 3));
			x += xInc;
			int y = YMARGIN;
			Stats stats = unit.getStats();
			
			//Insert health at top here
			g2d.drawString("Health", x, y);
			g2d.drawString(unit.getHealth() + "", x + 145, y);
			y += infoFont;
			
			for(Stat s : stats.getStandardStatsList()){
				drawStat(g2d, s, x, y);
				y += infoFont;
			}
			g2d.drawString("Modifiers: ", x, y);
			String modString = "";
			for(Modifier m : unit.getModifiers()){
				modString += m.name + " ";
			}
			g2d.drawString(modString, x + 145, y);
			
			x += xInc;
			//Draw modifier list along bottom
			y = YMARGIN;
			for(Stat s : stats.getAttackStatsList()){
				drawStat(g2d, s, x, y);
				y += infoFont;
			}
			x += xInc;
			y = YMARGIN;
			//Insert movement at top here
			g2d.drawString("Movement", x, y);
			
			String movement = "0";
			if(unit instanceof MovingUnit){
				movement = ((MovingUnit) unit).getMovement() + "";
			}
			g2d.drawString(movement, x + 145, y);
			y += infoFont;
			for(Stat s : stats.getMovementStatsList()){
				drawStat(g2d, s, x, y);
				y += infoFont;
			}
		}
		//Ability painting
		else if(ability != null){
			int x = 25;
			int y = YMARGIN;
			final int xInc = 225;
			g2d.drawString(ability.name, x, y);
			final int fontSize = SMALL_FONT.getSize();
			g2d.setFont(SMALL_FONT);
			
			y += fontSize;
			if(ability.manaCost > 0){
				g2d.drawString("Costs " + ability.manaCost + " Mana Per Use", x, y);
				if(! ability.canBeCastMultipleTimes){
					y += fontSize;
					g2d.drawString("Can Only Be Cast Once Per Turn", x, y);
				}
			}
			else
				g2d.drawString("Passive Ability", x, y);
			
			y += fontSize;
			int size = ability.getEffectCloudSize();
			if(size == Integer.MAX_VALUE)
				g2d.drawString("Affects Whole Board", x, y);
			else
				g2d.drawString("Affects Up To " + size + " Units", x, y);
			
			String affects = "";
			if(ability.appliesToAllied)
				affects += "Allied";
			if(ability.appliesToAllied && ability.appliesToFoe)
				affects += " and ";
			if(ability.appliesToFoe)
				affects += "Enemy";
			
			y+= fontSize;
			g2d.drawString("Affects " + affects + " Units", x, y);
			
			x += xInc;
			if(ability instanceof ModifierAbility){
				ModifierBundle mod = ((ModifierAbility)ability).getModifiers();
				String turns = mod.getTurnsRemaining() + " Turns Remaining (" 
						+ (mod.isStackable() ? "" : "Not ") + "Stackable)";
				g2d.drawString(turns, x, y);
				y += fontSize;
				for(Modifier m : mod.getModifiers()){
					g2d.drawString(m.toStatString(), x, y);
				}
			} else if(ability instanceof EffectAbility){
				g2d.drawString("Effects:", x, y);
				y += fontSize;
				String s = ((EffectAbility) ability).toStringLong();
				g2d.drawString(s, x, y);
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
