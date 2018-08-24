package model.game;

import model.board.Terrain;
import model.board.Tile;
import model.unit.Unit;
import model.unit.building.AllUnitModifierBuilding;
import model.unit.building.PlayerModifierBuilding;
import model.unit.building.PlayerModifierBuilding.PlayerModifierEffectType;
import model.unit.building.StartOfTurnEffectBuilding;
import model.unit.building.Temple;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.util.Cloud;
import model.util.MPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An instance is a player (not the commander piece). Extended to be either human controlled or AI.
 *
 * @author MPatashnik
 */
public abstract class Player implements Stringable {

  /** The Game this player is playing in */
  public final Game game;

  /** The index of this player in the model.game, where player 1 is the first player */
  public final int index;

  /** The Units this player controls */
  private HashSet<Unit> units;

  /** The commander belonging to this player */
  private Commander commander;

  /** The AllUnitModifierBuildings this player controls, if any. */
  private Set<AllUnitModifierBuilding> allUnitModifierBuildings;

  /** The temples this player controls, if any. Max length 5 */
  private ArrayList<Temple> temples;

  /** The Tiles in the model.board this player has vision of */
  private HashSet<Tile> visionCloud;

  /** The sum of all actions per turn generations/costs this player owns */
  private int commanderActionsPerTurn;

  /** The number of actions remaining this turn. Reset at start of turn. */
  private int commanderActionsRemaining;

  /** The sum of all the mana per turn generation/costs this player owns */
  private int manaPerTurn;

  /**
   * Constructor for Player class with just model.game.
   *
   * @param g
   * @param c
   */
  public Player(Game g, Color c) {
    game = g;
    this.index = g.getController().addPlayer(this, c);
    units = new HashSet<Unit>();
    allUnitModifierBuildings = new HashSet<>();
    temples = new ArrayList<Temple>();
    visionCloud = new HashSet<Tile>();
  }

  /** Returns true if it is this player's turn, false if some other player */
  public boolean isMyTurn() {
    return game.getCurrentPlayer() == this;
  }

  /** Returns true if this is a human player, false otherwise */
  public boolean isHumanPlayer() {
    return this instanceof HumanPlayer;
  }

  // HEALTH AND MANA
  /** Returns the current health for this player (the health of the commander) */
  public int getHealth() {
    return commander.getHealth();
  }

  /** Returns the max health for this player (the max health of the commander */
  public int getMaxHealth() {
    return commander.getMaxHealth();
  }

  /** Returns the current mana for this player */
  public int getMana() {
    return commander.getMana();
  }

  /** Returns the manaPerTurn this player generates */
  public int getManaPerTurn() {
    return manaPerTurn;
  }

  /**
   * Updates the manaPerTurn this player generates. Should be called at least at the start of every
   * turn
   */
  public void updateManaPerTurn() {
    manaPerTurn = 0;
    for (Unit u : units) {
      manaPerTurn += u.getManaPerTurn();
      if (u instanceof PlayerModifierBuilding
          && ((PlayerModifierBuilding) u).getEffect().effectType
              == PlayerModifierEffectType.MANA_GENERATION) {
        manaPerTurn += ((PlayerModifierBuilding) u).getEffect().value;
      }
    }
  }

  /** Return the number of actions this player has remaining on this turn. */
  public int getCommanderActionsRemaining() {
    return commanderActionsRemaining;
  }

  /** Spends one commander action remaining. Throws if there were already zero. */
  public void spendCommanderAction() {
    if (commanderActionsRemaining <= 0) {
      throw new RuntimeException("Can't spend commander action, already have none left");
    }
    commanderActionsRemaining--;
  }

  /**
   * Updates the number of actions per turn this player has. Should be called at least at the start
   * of every turn. If this results in a change, also updates the number of current actions this
   * player has.
   */
  private void updateCommanderActionsPerTurn() {
    int oldActionsPerTurn = commanderActionsPerTurn;
    commanderActionsPerTurn = 0;
    for (Unit u : units) {
      commanderActionsPerTurn += u.getCommanderActionsPerTurn();
      if (u instanceof PlayerModifierBuilding
          && ((PlayerModifierBuilding) u).getEffect().effectType
              == PlayerModifierEffectType.BONUS_ACTIONS) {
        commanderActionsPerTurn += ((PlayerModifierBuilding) u).getEffect().value;
      }
    }
    commanderActionsRemaining += commanderActionsPerTurn - oldActionsPerTurn;
  }

  /** Returns the current level (not exp) of this player */
  public int getLevel() {
    return commander.getLevel();
  }

  /** Returns the current amount of research this commander has accrewed */
  public int getResearch() {
    return commander.getResearch();
  }

  /** Returns the amount of research necessary to get to the next level */
  public int getResearchRequirement() {
    return commander.getResearchRequirement();
  }

  /** Returns the amount of research still necessary to get to the next level */
  public int getResearchRemaining() {
    return commander.getResearchRequirement();
  }

  /**
   * Adds the given amount of research to resarch, capping at the requirement. Input must be
   * positive (you can only gain research)
   */
  public void addResearch(int deltaResearch) throws IllegalArgumentException {
    commander.addResearch(deltaResearch);
  }

  // UNITS
  /**
   * Returns the units belonging to this player. passed-by-value, so editing this hashSet won't do
   * anything
   */
  public Set<Unit> getUnits() {
    return new HashSet<>(units);
  }

  /** Returns the units belonging to this player in the given location cloud. */
  public Set<Unit> getUnitsInCloud(Cloud cloud) {
    return units
        .stream()
        .filter(u -> cloud.contains(u.getLocation().getPoint()))
        .collect(Collectors.toSet());
  }

  /** Returns the index of the given temple, or -1 if this is not a temple owned by this */
  public int getTempleIndex(Temple t) {
    return temples.indexOf(t);
  }

  /** The commander belonging to this player */
  public Commander getCommander() {
    return commander;
  }

  /**
   * Adds the given model.unit to this player's units. Call whenever a model.unit is constructed. If
   * commander is null and u is a commander, sets commander to u. If temple, adds to temples,
   * refreshes buffs.
   *
   * @throws IllegalArgumentException - If u is a commander and commander isn't null.
   */
  public void addUnit(Unit u) throws IllegalArgumentException {
    units.add(u);
    // Check that we don't have two commanders.
    if (u instanceof Commander) {
      if (commander == null) commander = (Commander) u;
      else throw new IllegalArgumentException("Can't set " + u + " to commander for " + this);
    }
    // If temple, re-index stuff.
    if (u instanceof Temple) {
      if (temples.size() >= Temple.MAX_TEMPLES)
        throw new IllegalArgumentException(
            this + " can't construct another temple, already has max");
      Temple t = (Temple) u;
      temples.add(t);
    }
    // If all unit modifier building, apply new modifiers to all units.
    // Otherwise, apply modifiers from existing all unit modifier buildings to new unit.
    if (u instanceof AllUnitModifierBuilding) {
      for (Unit u2 : units) {
        ((AllUnitModifierBuilding) u).applyModifiersTo(u2);
      }
    } else {
      for (AllUnitModifierBuilding allUnitModifierBuilding : allUnitModifierBuildings) {
        allUnitModifierBuilding.applyModifiersTo(u);
      }
    }
    refreshTempleBuffs();
    refreshVisionCloud();
    updateManaPerTurn();
    updateCommanderActionsPerTurn();
  }

  /**
   * Removes the given model.unit from this player's units. If the given model.unit is this player's
   * commander, sets commander to null.
   */
  public void removeUnit(Unit u) {
    units.remove(u);
    if (u instanceof Commander && (Commander) u == commander) commander = null;
    if (u instanceof Temple) {
      temples.remove(u);
    }
    refreshTempleBuffs();
    refreshVisionCloud();
    updateManaPerTurn();
  }

  /** Refreshes all temples buffs on all units */
  private void refreshTempleBuffs() {
    for (Temple t : temples) {
      t.refreshForIndex();
    }
  }

  // VISION
  /** Return true iff this player's vision contains tile T */
  public boolean canSee(Tile t) {
    return !game.getFogOfWar().active || visionCloud.contains(t);
  }

  /** Return true iff the tile u occupies is in this Player's vision */
  public boolean canSee(Unit u) {
    return canSee(u.getLocation());
  }

  /**
   * Returns the tiles this player can see. Pass-by-value, so editing the returned hashset will do
   * nothing
   */
  public HashSet<Tile> getVisionCloud() {
    return new HashSet<Tile>(visionCloud);
  }

  /** Refreshes this player's vision cloud based on its units */
  public void refreshVisionCloud() {
    visionCloud.clear();
    for (Unit u : units) {
      // Always have vision of a unit's location.
      visionCloud.add(u.getLocation());

      // Calculate ray-based vision.
      MPoint center = u.getLocation().getPoint();
      for (int radius = 1; radius <= u.getVisionRange(); radius++) {
        boolean radiusIsOne = radius == 1;
        addVisionPoint(center, center.add(radius, 0), radiusIsOne);
        addVisionPoint(center, center.add(-radius, 0), radiusIsOne);
        addVisionPoint(center, center.add(0, radius), radiusIsOne);
        addVisionPoint(center, center.add(0, -radius), radiusIsOne);
        for (int i = 1; i < radius; i++) {
          addVisionPoint(center, center.add(radius - i, i), false);
          addVisionPoint(center, center.add(-radius + i, -i), false);
          addVisionPoint(center, center.add(-i, radius - i), false);
          addVisionPoint(center, center.add(i, -radius + i), false);
        }
      }
    }
  }

  /**
   * Helper for refreshVisionCloud - adds the tile at the given point if:
   * <li>it is in bounds.
   * <li>It is a woods and a unit is directly adjacent to it
   */
  private void addVisionPoint(MPoint origin, MPoint point, boolean unitIsAdjacent) {
    Tile tile;
    try {
      tile = game.board.getTileAt(point);
    } catch (IllegalArgumentException e) {
      // OOB - can't see.
      return;
    }

    Cloud lineCloud = origin.getLineCloudTo(point);
    if (tile.terrain != Terrain.MOUNTAIN) {
      Tile originTile = game.board.getTileAt(origin);

      Set<Tile> occupiedMountainRange =
          originTile.terrain == Terrain.MOUNTAIN
              ? game.board.getContiguousMountainRange(originTile)
              : new HashSet<>();

      if (lineCloud
          .getPoints()
          .stream()
          .filter(p -> !occupiedMountainRange.contains(game.board.getTileAt(p)))
          .map(p -> game.board.getTileAt(p).terrain)
          .anyMatch(t -> t == Terrain.MOUNTAIN)) {
        // Can't see past mountains.
        return;
      }
    }

    switch (tile.terrain) {
      case GRASS:
      case ANCIENT_GROUND:
      case MOUNTAIN:
        visionCloud.add(tile);
        break;
      case WOODS:
        if (unitIsAdjacent) {
          visionCloud.add(tile);
        }
        break;
    }
  }

  // TURN
  /**
   * Called when it becomes this player's turn. Does start of turn processing. - calls refresh on
   * each model.unit (no particular order). Return true if this player can start their turn -
   * commander is alive, false otherwise
   */
  final boolean turnStart() {
    try {
      // Refresh for turn
      for (Unit u : units) {
        u.refreshForTurn();
      }
      // Add base mana per turn
      updateManaPerTurn();
      commander.addMana(manaPerTurn);

      // Add actions
      updateCommanderActionsPerTurn();
      commanderActionsRemaining = commanderActionsPerTurn;

      // Process start of turn buildings.
      for (Unit u : units) {
        if (u instanceof StartOfTurnEffectBuilding) {
          StartOfTurnEffectBuilding.StartOfTurnEffect effect =
              ((StartOfTurnEffectBuilding) u).getEffect();

          switch (effect.type) {
            case HEAL_COMBATANT:
              for (Unit u2 : getUnitsInCloud(effect.cloud.translate(u.getLocation().getPoint()))) {
                if (u2 instanceof Combatant) {
                  u2.changeHealth(effect.value, u);
                }
              }
              break;
            default:
              throw new RuntimeException("Unknown effect type: " + effect);
          }
        }
      }

      // If mana < 0, force player to choose units to sacrifice instead.
      if (commander.getMana() < 0) {
        // TODO
      }
      return true;
    } catch (NullPointerException e) {
      return false;
    }
  }

  /**
   * Called when it becomes this player's turn to do things. Passes control to player. Shouldn't be
   * recursive, and should terminate when it finishes doing things.
   */
  protected abstract void turn();

  /** Called by the someone (the player / the model.game) when this player's turn should end. */
  public abstract void turnEnd();

  @Override
  public String toString() {
    return "Player " + index;
  }

  @Override
  public String toStringShort() {
    return "Player " + index;
  }

  @Override
  public String toStringLong() {
    return "Player " + index + " - " + commander.toStringShort();
  }

  @Override
  public String toStringFull() {
    String s =
        "Player "
            + index
            + " - "
            + commander.toStringShort()
            + " in "
            + game.toStringShort()
            + " controlling ";
    for (Unit u : units) {
      s += u.toStringShort() + " ";
    }
    return s;
  }
}
