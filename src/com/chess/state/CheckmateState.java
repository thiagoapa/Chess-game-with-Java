package com.chess.state;

import com.chess.model.Color;

/** Jaque mate: {@code loser} no tiene jugadas legales estando en jaque. */
public class CheckmateState implements GameState {
    private final Color loser;

    public CheckmateState(Color loser) {
        this.loser = loser;
    }

    @Override public boolean isGameOver() { return true; }
    @Override public Color sideToMove()   { return null; }

    public Color winner() { return loser.opposite(); }

    @Override
    public String statusLine() {
        return "JAQUE MATE. Ganan las " + loser.opposite().displayName() + ".";
    }
}
