package com.chess.strategy;

import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Movimiento del peón: avance simple, avance doble inicial, capturas en diagonal,
 * captura al paso y coronación. Es la estrategia más rica del juego.
 */
public class PawnMovement implements MovementStrategy {

    @Override
    public List<Move> generateMoves(Board board, Position from, Piece piece) {
        List<Move> moves = new ArrayList<>();
        Color color = piece.color();
        int dir = color.direction();

        // Avance simple
        Position oneAhead = from.offset(dir, 0);
        if (oneAhead.isInsideBoard() && board.isEmpty(oneAhead)) {
            addForward(moves, from, oneAhead, color);

            // Avance doble desde la fila inicial
            Position twoAhead = from.offset(2 * dir, 0);
            if (from.row() == color.pawnStartRow()
                    && board.isEmpty(twoAhead)) {
                moves.add(new Move(from, twoAhead, Move.Type.DOUBLE_PAWN_PUSH));
            }
        }

        // Capturas diagonales (incluida al paso)
        for (int dCol : new int[]{-1, 1}) {
            Position diag = from.offset(dir, dCol);
            if (!diag.isInsideBoard()) continue;

            Piece target = board.pieceAt(diag);
            if (target != null && piece.isEnemyOf(target)) {
                addForward(moves, from, diag, color); // captura normal (puede coronar)
            } else if (diag.equals(board.enPassantTarget())) {
                moves.add(new Move(from, diag, Move.Type.EN_PASSANT));
            }
        }

        return moves;
    }

    /** Añade un avance/captura, marcándolo como coronación si llega a la última fila. */
    private void addForward(List<Move> moves, Position from, Position to, Color color) {
        if (to.row() == color.promotionRow()) {
            moves.add(new Move(from, to, Move.Type.PROMOTION));
        } else {
            moves.add(new Move(from, to, Move.Type.NORMAL));
        }
    }
}
