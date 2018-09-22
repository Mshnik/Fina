package controller.game;

import ai.AIController;

/**
 * Options for creating a player for a new game. Usually just a type, but for some AI types requires
 * additional info.
 *
 * @author Mshnik
 */
public class CreatePlayerOptions {
  final String typeName;
  final String aiFilename;
  final int aiFileRow;
  final AIController explicitController;

  public CreatePlayerOptions(String typeName) {
    if (typeName.equals(AIController.PROVIDED_AI_TYPE)) {
      throw new RuntimeException("Expected other args for provided ai type");
    }
    this.typeName = typeName;
    this.aiFilename = null;
    this.aiFileRow = -1;
    this.explicitController = null;
  }

  public CreatePlayerOptions(String typeName, String aiFilename, int aiFileRow) {
    this.typeName = typeName;
    this.aiFilename = aiFilename;
    this.aiFileRow = aiFileRow;
    explicitController = null;
  }

  public CreatePlayerOptions(String typeName, AIController explicitController) {
    this.typeName = typeName;
    this.aiFilename = null;
    this.aiFileRow = -1;
    this.explicitController = explicitController;
  }
}
