package com.chess.state;

import com.chess.model.Color;

/** Partida en curso, sin jaque. Le toca mover a {@code sideToMove}. */
public class OngoingState implements GameState {
    private final Color sideToMove;

    public OngoingState(Color sideToMove) {
        this.sideToMove = sideToMove;
    }

    @Override public boolean isGameOver() { return false; }
    @Override public Color sideToMove()   { return sideToMove; }

    @Override
    public String statusLine() {
        return "Turno de las " + sideToMove.displayName() + ".";
    }
}
