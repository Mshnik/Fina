package view.gui.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.StringJoiner;
import java.util.stream.Collectors;


import javax.swing.JPanel;

import model.board.Terrain;
import view.gui.Frame;
import view.gui.ImageIndex;

import model.unit.*;
import model.unit.ability.*;
import model.unit.modifier.Modifier;
import model.unit.modifier.ModifierBundle;
import model.unit.stat.Stat;
import model.unit.stat.Stats;


public final class InfoPanel extends JPanel{
	/***/
	private static final long serialVersionUID = 1L;

	/** The Height of the InfoPanel */
	protected static final int HEIGHT = 125;

	/** The color of the border surrounding the headerPanel */
	protected static final Color BORDER_COLOR = new Color(74, 47, 12);
	
	/** Distance (in pixels) between the top of the InfoPanel and the top of the bars */
	private static final int YMARGIN = 32;
	
	/** Font for drawing title text */
	private static final Font BIG_FONT = new Font(Frame.FONTNAME, Font.BOLD, 20);
	
	/** Font for drawing standard text */
	private static final Font SMALL_FONT = new Font(Frame.FONTNAME, Font.BOLD, 16);
	
	/** Character for the infinity character */
	private static final char INF_CHAR = '\u221E';
	
	/** The frame this belongs to*/
	public final Frame frame;
	
	/** The Unit (if any) this InfoPanel is currently drawing info for */
	private Unit unit;
	
	/** The Ability (if any) this InfoPanel is currently drawing info for */
	private Ability ability;

	/** The Terrain (if any) this InfoPanel is currently drawing info for */
	private Terrain terrain;
	
	public InfoPanel(Frame f){
		frame = f;
		GamePanel gp = f.getGamePanel();
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
	
	/** Sets the model.unit this InfoPanel is to draw info for, and causes a repaint */
	public void setUnit(Unit u){
		unit = u;
		ability = null;
		terrain = null;
		repaint();
	}
	
	/** Sets the model.ability this InfoPanel is to draw info for, and causes a repaint */
	public void setAbility(Ability a){
		unit = null;
		ability = a;
		terrain = null;
		repaint();
	}

	/** Sets the model.terrain this InfoPanel is to draw for and causes a repaint */
	public void setTerrain(Terrain t) {
		unit = null;
		ability = null;
		terrain = t;
		repaint();
	}
	
	/** Paints this InfoPanel, the info for the currently selected model.unit */
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

		int xStart = 25;
		int x = 25;
		int y = YMARGIN;
		final int xInc = 225;

		//Unit painting
		if(unit != null){
			String mainLine = unit.name;
			if (unit.owner != null) {
				mainLine += " (" + unit.owner + ")";
			}
			g2d.drawString(mainLine, x, YMARGIN);
			g2d.setFont(SMALL_FONT);
			final int infoFont = SMALL_FONT.getSize();

			String subString = " " + unit.getIdentifierString() + " ";
			if (unit instanceof Combatant) {
				subString += " - " + ((Combatant) unit).combatantClasses.stream()
						.map(Combatant.CombatantClass::toMidString)
						.collect(Collectors.joining(", "));
			}

			g2d.drawString(subString, x, YMARGIN + infoFont);
			
			g2d.setFont(new Font(Frame.FONTNAME, Font.BOLD, infoFont - 3));
			x += xInc;
			Stats stats = unit.getStats();
			
			//Insert health at top here
			g2d.drawString("Health", x, y);
			g2d.drawString(unit.getHealth() + "", x + 145, y);
			y += infoFont;
			
			for(Stat s : stats.getStandardStatsList()){
				drawStat(g2d, s, x, y);
				y += infoFont;
			}
			
			x = xStart;
			String modifierTitle = "Modifiers: ";
			g2d.drawString(modifierTitle, x, y);
			String modString = "";
			for(Modifier m : unit.getModifiers()){
				if(! (m.name.equals(Commander.LEVELUP_HEALTH_NAME) 
						|| m.name.equals(Commander.LEVELUP_MANA_NAME)))
				modString += m.name + " ";
			}
			g2d.drawString(modString, x + frame.getTextWidth(SMALL_FONT, modifierTitle) + 15, y);
			
			x = xStart + 2 * xInc;
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
			y = YMARGIN;
			if(ability instanceof ModifierAbility){
				ModifierBundle mod = ((ModifierAbility)ability).getModifiers();
				String turns = (mod.getTurnsRemaining() == Integer.MAX_VALUE ? INF_CHAR + "": mod.getTurnsRemaining())
						+ " Turns Remaining (" 
						+ (mod.isStackable() ? "" : "Not ") + "Stackable)";
				g2d.drawString(turns, x, y);
				for(Modifier m : mod.getModifiers()){
					y += fontSize;
					g2d.drawString(m.toStatString(), x, y);
				}
			} else if(ability instanceof EffectAbility){
				g2d.drawString("Effects:", x, y);
				y += fontSize;
				String s = ((EffectAbility) ability).toStringLong();
				g2d.drawString(s, x, y);
			}
		}
		// Terrain painting.
		else if (terrain != null) {
			g2d.drawString("Terrain: " + terrain.toString(), x, y);
		}
	}
	
	private void drawStat(Graphics2D g2d, Stat s, int x, int y){
		g2d.drawString(s.name.toString(), x, y);
		String str = s.val.toString();
		g2d.drawString(str, x + 145, y);
	}
}
