package com.chess.factory;

import com.chess.model.Color;
import com.chess.pieces.Piece;
import com.chess.pieces.PieceType;
import com.chess.strategy.*;

/**
 * Patrón ABSTRACT FACTORY.
 *
 * Define la familia de productos (las seis piezas) sin fijar el color. Cada
 * fábrica concreta ({@link WhitePieceFactory}, {@link BlackPieceFactory})
 * produce un "ejército" coherente: todas sus piezas salen con el color correcto
 * y ya conectadas a su estrategia de movimiento. El cliente (el tablero) sólo
 * conoce esta abstracción, nunca el color concreto.
 */
public abstract class AbstractPieceFactory {

    /** Color del ejército que produce esta fábrica. Lo definen las subclases. */
    protected abstract Color color();

    public Piece createPawn()   { return new Piece(color(), PieceType.PAWN,   new PawnMovement());   }
    public Piece createKnight() { return new Piece(color(), PieceType.KNIGHT, new KnightMovement()); }
    public Piece createBishop() { return new Piece(color(), PieceType.BISHOP, new BishopMovement()); }
    public Piece createRook()   { return new Piece(color(), PieceType.ROOK,   new RookMovement());   }
    public Piece createQueen()  { return new Piece(color(), PieceType.QUEEN,  new QueenMovement());  }
    public Piece createKing()   { return new Piece(color(), PieceType.KING,   new KingMovement());   }

    /** Crea una pieza por tipo. Útil para la coronación. */
    public Piece create(PieceType type) {
        switch (type) {
            case PAWN:   return createPawn();
            case KNIGHT: return createKnight();
            case BISHOP: return createBishop();
            case ROOK:   return createRook();
            case QUEEN:  return createQueen();
            case KING:   return createKing();
            default: throw new IllegalArgumentException("Tipo desconocido: " + type);
        }
    }
}
