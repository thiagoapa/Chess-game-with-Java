package com.chess.state;

import com.chess.model.Color;

/** Tablas por otra causa (p. ej. material insuficiente). */
public class DrawState implements GameState {
    private final String reason;

    public DrawState(String reason) {
        this.reason = reason;
    }

    @Override public boolean isGameOver() { return true; }
    @Override public Color sideToMove()   { return null; }

    @Override
    public String statusLine() {
        return "TABLAS: " + reason + ".";
    }
}
