package com.chess.strategy;

import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;
import com.chess.pieces.PieceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Movimiento del rey: un paso en cualquier dirección, más el enroque (corto y
 * largo) cuando se cumplen todas las condiciones reglamentarias.
 */
public class KingMovement implements MovementStrategy {

    @Override
    public List<Move> generateMoves(Board board, Position from, Piece piece) {
        List<Move> moves = new ArrayList<>();
        Color color = piece.color();

        // Pasos de una casilla
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue;
                Position to = from.offset(dRow, dCol);
                if (!to.isInsideBoard()) continue;
                Piece occupant = board.pieceAt(to);
                if (occupant == null || piece.isEnemyOf(occupant)) {
                    moves.add(new Move(from, to));
                }
            }
        }

        addCastlingMoves(board, from, piece, color, moves);
        return moves;
    }

    private void addCastlingMoves(Board board, Position from, Piece king, Color color, List<Move> moves) {
        // El rey no debe haberse movido ni estar en jaque.
        if (king.hasMoved() || board.isKingInCheck(color)) return;

        int row = from.row();
        Color enemy = color.opposite();

        // Enroque corto (hacia la columna h)
        Position kRook = new Position(row, 7);
        if (isUnmovedRook(board, kRook, color)
                && board.isEmpty(new Position(row, 5))
                && board.isEmpty(new Position(row, 6))
                && !board.isSquareAttacked(new Position(row, 5), enemy)
                && !board.isSquareAttacked(new Position(row, 6), enemy)) {
            moves.add(new Move(from, new Position(row, 6), Move.Type.CASTLE_KINGSIDE));
        }

        // Enroque largo (hacia la columna a)
        Position qRook = new Position(row, 0);
        if (isUnmovedRook(board, qRook, color)
                && board.isEmpty(new Position(row, 1))
                && board.isEmpty(new Position(row, 2))
                && board.isEmpty(new Position(row, 3))
                && !board.isSquareAttacked(new Position(row, 3), enemy)
                && !board.isSquareAttacked(new Position(row, 2), enemy)) {
            moves.add(new Move(from, new Position(row, 2), Move.Type.CASTLE_QUEENSIDE));
        }
    }

    private boolean isUnmovedRook(Board board, Position pos, Color color) {
        Piece p = board.pieceAt(pos);
        return p != null && p.color() == color && p.type() == PieceType.ROOK && !p.hasMoved();
    }
}
