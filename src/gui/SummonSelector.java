package gui;

import unit.Unit;

/** A location selector for summoning a new unit */
public class SummonSelector extends LocationSelector {

	/** The unit doing the summoning - must be able to summon */
	public final Unit summoner;
	
	/** The unit this selector is trying to summon */
	public final Unit toSummon;
	
	public SummonSelector(GamePanel gp, Unit summoner, Unit toSummon) {
		super(gp);
		this.toSummon = toSummon;
		this.summoner = summoner;
		refreshPossibilitiesCloud();
	}

	/** Refreshes the possible summon locations for this summonselector */
	@Override
	protected void refreshPossibilitiesCloud() {
		cloud = gamePanel.game.board.getSummonCloud(this);
		gamePanel.repaint();
	}

}
