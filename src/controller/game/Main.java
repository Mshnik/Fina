package controller.game;

import static ai.delegating.DelegatingAIControllers.DELEGATING_RANDOM_AI_TYPE;
import static model.game.HumanPlayer.HUMAN_PLAYER_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.game.Game.FogOfWar;
import model.unit.ability.Abilities;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

public final class Main {
  /** Simple main method to test out Frame features */
  public static void main(String[] args) throws Exception {
    // Force unit, building, spell, audio loading.
    Combatants.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);
    Abilities.getAbilitiesForAge(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Backyard.csv";
    humanGame(boardFilename);
  }

  private static void humanGame(String boardFilename) {
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);

    GameController.loadAndStart(
        "game/boards/" + boardFilename, defaultPlayerTypes, FogOfWar.REGULAR, 1, 10, 18, 3);
  }

  private static void genDataLoop(String boardFilename) throws Exception {
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);
    int i = 0;
    while (true) {
      GameController controller =
          GameController.loadAndStartHeadless(
              "game/boards/" + boardFilename,
              defaultPlayerTypes,
              Collections.emptyList(),
              FogOfWar.REGULAR,
              1);
      Thread.sleep(150);
      System.out.println("Game " + i + " started");
      while (controller.game.isRunning()) {
        Thread.sleep(20);
      }
      System.out.println("Game " + i + " finished");
      i++;
    }
  }
}
