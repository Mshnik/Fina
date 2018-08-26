package controller.selector;

import controller.game.GameController;
import model.board.Tile;
import model.unit.ability.Ability;
import model.unit.commander.Commander;

import java.util.ArrayList;
import java.util.List;

public final class CastSelector extends LocationSelector {
  /** The commander doing the casting */
  public final Commander caster;

  /** The cloud of effect range based on the current location of the boardCursor */
  public List<Tile> effectCloud;

  /** The ability this selector is trying to summon */
  public final Ability toCast;

  public CastSelector(GameController gc, Commander caster, Ability toCast) {
    super(gc);
    effectCloud = new ArrayList<>();
    this.caster = caster;
    this.toCast = toCast;
    refreshPossibilitiesCloud();
  }

  /** Refreshes the possible cast locations for this castSelector */
  @Override
  protected void refreshPossibilitiesCloud() {
    int castDist =
        toCast.castDist + (toCast.canBeCloudBoosted ? 0 : caster.owner.getCastSelectBoost());

    cloud = controller.game.board.getRadialCloud(caster.getLocation(), castDist);
    // If cast dist is greater than 0, can't cast on commander location.
    if (castDist > 0) {
      cloud.remove(caster.getLocation());
    }
    List<Tile> toRemove = new ArrayList<>();
    for (Tile t : cloud) {
      if (toCast
          .getTranslatedEffectCloud(caster, t, caster.owner.getCastCloudBoost())
          .stream()
          .noneMatch(
              tile ->
                  tile.isOccupied()
                      && caster.owner.canSee(tile)
                      && toCast.wouldAffect(tile.getOccupyingUnit(), caster))) {
        toRemove.add(t);
      }
    }

    cloud.removeAll(toRemove);
    if (!cloud.isEmpty()) {
      refreshEffectCloud();
    }
  }

  /** Refreshes the effectCloud for the current location of the boardCursor */
  public void refreshEffectCloud() {
    effectCloud =
        toCast.getTranslatedEffectCloud(
            caster,
            controller.frame.getGamePanel().boardCursor.getElm(),
            caster.owner.getCastCloudBoost());
    controller.repaint();
  }
}
