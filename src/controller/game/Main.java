package controller.game;

import static model.game.HumanPlayer.HUMAN_PLAYER_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.game.Game.FogOfWar;
import model.unit.ability.Abilities;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

public final class Main {
  /**
   * Runnable method that starts a game between two human players.
   */
  public static void main(String[] args) throws Exception {
    // Force unit, building, spell, audio loading.
    Combatants.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);
    Abilities.getAbilitiesForAge(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Backyard.csv";
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(HUMAN_PLAYER_TYPE);
    defaultPlayerTypes.add(HUMAN_PLAYER_TYPE);

    GameController.loadAndStart(
        "game/boards/" + boardFilename,
        defaultPlayerTypes.stream().map(CreatePlayerOptions::new).collect(Collectors.toList()),
        FogOfWar.REGULAR,
        1,
        10,
        18,
        3);
  }
}
