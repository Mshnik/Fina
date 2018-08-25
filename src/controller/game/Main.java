package controller.game;

import model.unit.building.Buildings;
import model.unit.combatant.FileCombatant;

public final class Main {
  /** Simple main method to test out Frame features */
  public static void main(String[] args) {
    // Force unit and building loading.
    FileCombatant.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "sample_board_small.csv";
    GameController.loadAndStart("game/boards/" + boardFilename);
  }
}
