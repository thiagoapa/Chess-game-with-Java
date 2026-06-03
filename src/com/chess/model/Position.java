package com.chess.model;

import java.util.Objects;

/**
 * Coordenada inmutable del tablero (fila 0-7, columna 0-7).
 * Internamente: fila 0 = rango 1 (base de las blancas), columna 0 = columna 'a'.
 */
public final class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() { return row; }
    public int col() { return col; }

    public boolean isInsideBoard() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public Position offset(int dRow, int dCol) {
        return new Position(row + dRow, col + dCol);
    }

    /** Convierte notación tipo "e2" a una Position. Devuelve null si es inválida. */
    public static Position fromAlgebraic(String text) {
        if (text == null || text.length() != 2) return null;
        char file = Character.toLowerCase(text.charAt(0));
        char rank = text.charAt(1);
        if (file < 'a' || file > 'h') return null;
        if (rank < '1' || rank > '8') return null;
        int col = file - 'a';
        int row = rank - '1';
        return new Position(row, col);
    }

    /** Convierte la Position a notación tipo "e2". */
    public String toAlgebraic() {
        char file = (char) ('a' + col);
        char rank = (char) ('1' + row);
        return "" + file + rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
