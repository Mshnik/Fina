package ai.evolutionary;

/** Wrapper for a result of a game. */
final class EvoGameResult {
  /** True if the first player won, false if this was a tie. */
  private final boolean player1Won;

  /** The winner if a player won, otherwise one of the two players. */
  private final EvoPlayer player1;

  /** The loser if a player lost, otherwise one of the two players. */
  private final EvoPlayer player2;

  /** Constructs a EvoGameResult. */
  private EvoGameResult(boolean player1Won, EvoPlayer player1, EvoPlayer player2) {
    this.player1Won = player1Won;
    this.player1 = player1;
    this.player2 = player2;
  }

  /** Creates a EvoGameResult for a winner and loser. */
  static EvoGameResult forWinnerAndLoser(EvoPlayer winner, EvoPlayer loser) {
    return new EvoGameResult(true, winner, loser);
  }

  /** Creates a EvoGameResult for a tie. */
  static EvoGameResult forTie(EvoPlayer player1, EvoPlayer player2) {
    return new EvoGameResult(false, player1, player2);
  }

  /** True if this has a winner and loser, false for a tie. */
  boolean hasWinner() {
    return player1Won;
  }

  /** Returns the winner if there is one, throws if this was a tie. */
  EvoPlayer getWinner() {
    if (player1Won) {
      return player1;
    } else {
      throw new RuntimeException("No winner, was a tie");
    }
  }

  /** Returns the loser if there is one, throws if this was a tie. */
  EvoPlayer getLoser() {
    if (player1Won) {
      return player2;
    } else {
      throw new RuntimeException("No loser, was a tie");
    }
  }
}
