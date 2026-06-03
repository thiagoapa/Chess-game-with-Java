package com.chess.ai;

import com.chess.pieces.PieceType;

/**
 * Valores de material estándar de cada pieza (en "peones"). Los usa la IA para
 * estimar qué tan buena es una posición: más material propio y menos del rival
 * es mejor. El rey tiene un valor enorme para que nunca se "regale".
 */
final class PieceValues {

    private PieceValues() {}

    static int valueOf(PieceType type) {
        switch (type) {
            case PAWN:   return 1;
            case KNIGHT: return 3;
            case BISHOP: return 3;
            case ROOK:   return 5;
            case QUEEN:  return 9;
            case KING:   return 1000;
            default:     return 0;
        }
    }
}
