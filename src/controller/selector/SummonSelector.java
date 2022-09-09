package controller.selector;

import controller.game.GameController;
import model.board.Tile;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.building.Building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A location selector for summoning a new model.unit
 */
public final class SummonSelector<S extends Unit & Summoner> extends LocationSelector {

  /**
   * The model.unit doing the summoning - must be able to summon
   */
  public final S summoner;

  /**
   * The model.unit this selector is trying to summon
   */
  public final Unit toSummon;

  /**
   * The cloud of effect range based on the current location of the boardCursor. Should be a 1x1 cloud except for fanout buildings.
   */
  private List<Tile> effectCloud;

  public SummonSelector(GameController gc, S summoner, Unit toSummon) {
    super(gc);
    effectCloud = new ArrayList<>();
    this.toSummon = toSummon;
    this.summoner = summoner;
    refreshPossibilitiesCloud();
  }

  /**
   * Refreshes the possible summon locations for this summonselector
   */
  @Override
  protected void refreshPossibilitiesCloud() {
    cloud = controller.game.board.getSummonCloud(this);
    refreshEffectCloud();
  }

  @Override
  public List<Tile> getEffectCloud() {
    return Collections.unmodifiableList(effectCloud);
  }

  @Override
  public void refreshEffectCloud() {
    effectCloud = new ArrayList<>();
    Tile location = controller.frame.getGamePanel().boardCursor.getElm();
    effectCloud.add(location);
    if (toSummon instanceof Building) {
      Set<Tile> fanout = ((Building<?>) toSummon).buildFanOut(summoner.owner, location);
      effectCloud.addAll(fanout);
    }

    controller.repaint();
  }
}
