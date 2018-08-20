package controller.selector;

import controller.game.GameController;
import model.unit.Unit;

/** A location selector for summoning a new model.unit */
public final class SummonSelector extends LocationSelector {

	/** The model.unit doing the summoning - must be able to summon */
	public final Unit summoner;
	
	/** The model.unit this selector is trying to summon */
	public final Unit toSummon;
	
	public SummonSelector(GameController gc, Unit summoner, Unit toSummon) {
		super(gc);
		this.toSummon = toSummon;
		this.summoner = summoner;
		refreshPossibilitiesCloud();
	}

	/** Refreshes the possible summon locations for this summonselector */
	@Override
	protected void refreshPossibilitiesCloud() {
		cloud = controller.game.board.getSummonCloud(this);
		controller.repaint();
	}

}
