package com.chess.pieces;

/**
 * Tipos de pieza. Cada uno conoce su letra estándar (notación inglesa), que se
 * usa para dibujar el tablero: MAYÚSCULA para blancas, minúscula para negras.
 */
public enum PieceType {
    PAWN  ('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK  ('R'),
    QUEEN ('Q'),
    KING  ('K');

    private final char letter;

    PieceType(char letter) {
        this.letter = letter;
    }

    public char letter() {
        return letter;
    }
}
