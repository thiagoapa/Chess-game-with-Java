package com.chess.strategy;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

/** Movimiento del caballo: ocho saltos en "L", ignora piezas intermedias. */
public class KnightMovement implements MovementStrategy {

    private static final int[][] JUMPS = {
        {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
        {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    @Override
    public List<Move> generateMoves(Board board, Position from, Piece piece) {
        List<Move> moves = new ArrayList<>();
        for (int[] j : JUMPS) {
            Position to = from.offset(j[0], j[1]);
            if (!to.isInsideBoard()) continue;
            Piece occupant = board.pieceAt(to);
            if (occupant == null || piece.isEnemyOf(occupant)) {
                moves.add(new Move(from, to));
            }
        }
        return moves;
    }
}
