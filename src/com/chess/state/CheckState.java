package com.chess.state;

import com.chess.model.Color;

/** El bando al que le toca mover está en jaque (pero tiene salida). */
public class CheckState implements GameState {
    private final Color sideToMove;

    public CheckState(Color sideToMove) {
        this.sideToMove = sideToMove;
    }

    @Override public boolean isGameOver() { return false; }
    @Override public Color sideToMove()   { return sideToMove; }

    @Override
    public String statusLine() {
        return "\u00a1JAQUE a las " + sideToMove.displayName() + "! Deben responder al jaque.";
    }
}
