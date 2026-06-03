package com.chess.state;

import com.chess.model.Color;

/** Ahogado: el bando en turno no está en jaque pero no tiene jugadas legales. */
public class StalemateState implements GameState {
    @Override public boolean isGameOver() { return true; }
    @Override public Color sideToMove()   { return null; }

    @Override
    public String statusLine() {
        return "TABLAS por ahogado (rey ahogado, sin jugadas legales).";
    }
}
