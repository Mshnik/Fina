package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.board.Tile;
import model.unit.commander.Commander;

/** A list of delegates for casting spells. */
public final class CastDelegates {
  private CastDelegates() {}

  /** Parent class for CastByNameDelegates. */
  private abstract static class CastByNameDelegate extends ByNameDelegate {
    private CastByNameDelegate() {
      super(AIActionType.CAST_SPELL);
    }
  }

  /** Cast delegate that wants to cast a certain name of spell. */
  public static final class CastSpellByNameDelegate extends CastByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      return getSubWeight(action.spellToCast.name);
    }
  }

  /**
   * Cast delegate that wants to maximize the number of units effected by a spell (maximizes
   * friendly units for boost spells, maximizes enemy units and minimizes friendly units for damage
   * spells) by name.
   */
  public static final class MaximizeUnitsEffectedCastByNameDelegate extends CastByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      Commander caster = (Commander) action.actingUnit;
      return action
          .spellToCast
          .getTranslatedEffectCloud(caster, action.targetedTile, action.player.getCastCloudBoost())
          .stream()
          .filter(Tile::isOccupied)
          .map(Tile::getOccupyingUnit)
          .filter(u -> action.spellToCast.wouldAffect(u, caster))
          .mapToInt(
              u -> {
                switch (action.spellToCast.abilityType) {
                  case ATTACK:
                    return u.owner != action.player ? 1 : -1;
                  case HEAL:
                  case BUFF:
                  case UTILITY:
                    return u.owner == action.player ? 1 : -1;
                }
                throw new RuntimeException(
                    "Unknown abilityType: " + action.spellToCast.abilityType);
              })
          .sum();
    }
  }

  /** Cast delegate that wants to minimize the "wasted" spell effect. */
  public static final class MinimizeRedundantEffectByNameCastDelegate extends CastByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      Commander caster = (Commander) action.actingUnit;
      return action
          .spellToCast
          .getTranslatedEffectCloud(caster, action.targetedTile, action.player.getCastCloudBoost())
          .stream()
          .filter(Tile::isOccupied)
          .map(Tile::getOccupyingUnit)
          .filter(u -> action.spellToCast.wouldAffect(u, caster))
          .mapToDouble(u -> action.spellToCast.getEffectivenessOn(u) - 1)
          .sum();
    }
  }
}
