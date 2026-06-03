package com.chess.strategy;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica compartida por las piezas "deslizantes" (torre, alfil, dama): avanzan
 * en línea recta por una o varias direcciones hasta toparse con el borde o una
 * pieza. Evita duplicar el mismo recorrido en tres estrategias.
 */
abstract class SlidingMovement implements MovementStrategy {

    /** Cada subclase indica sus direcciones {fila, columna}. */
    protected abstract int[][] directions();

    @Override
    public List<Move> generateMoves(Board board, Position from, Piece piece) {
        List<Move> moves = new ArrayList<>();
        for (int[] d : directions()) {
            Position p = from.offset(d[0], d[1]);
            while (p.isInsideBoard()) {
                Piece occupant = board.pieceAt(p);
                if (occupant == null) {
                    moves.add(new Move(from, p));            // casilla libre
                } else {
                    if (piece.isEnemyOf(occupant)) {
                        moves.add(new Move(from, p));        // captura
                    }
                    break;                                   // bloqueado: detener este rayo
                }
                p = p.offset(d[0], d[1]);
            }
        }
        return moves;
    }
}
