package gui.decision;

import java.awt.Color;

import board.Tile;
import unit.Combatant;
import gui.LocationSelector;
import gui.panel.GamePanel;

/** A selector for choosing a unit to attack */
public class AttackSelector extends LocationSelector {

	/** The unit that is attacking from this selector */
	public final Combatant attacker;

	public AttackSelector(GamePanel gp, Combatant attacker) {
		super(gp);
		this.attacker = attacker;
		cloudColor = new Color(1.0f, 0f, 0f, 0.5f);
		refreshPossibilitiesCloud();
	}

	@Override
	protected void refreshPossibilitiesCloud() {
		cloud = gamePanel.game.board.getRadialCloud(attacker.getLocation(), attacker.getAttackRange() + 1);
		int i = 0;
		while(i < cloud.size()){
			Tile t= cloud.get(i);
			if(! attacker.owner.canSee(t) || ! t.isOccupied() || t.getOccupyingUnit().owner == attacker.owner)
				cloud.remove(i);
			else
				i++;
		}
		gamePanel.repaint();
	}

}
