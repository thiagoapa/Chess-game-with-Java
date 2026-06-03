package com.chess.model;

import com.chess.pieces.PieceType;

/**
 * Representa una jugada (pseudo-legal o legal): de dónde a dónde, más banderas
 * para los movimientos especiales del ajedrez. La estrategia de movimiento de
 * cada pieza genera objetos Move ya etiquetados con el tipo correcto.
 */
public final class Move {

    public enum Type {
        NORMAL,
        DOUBLE_PAWN_PUSH,  // peón avanza dos: habilita captura al paso
        EN_PASSANT,        // captura al paso
        CASTLE_KINGSIDE,   // enroque corto
        CASTLE_QUEENSIDE,  // enroque largo
        PROMOTION          // coronación (el tipo elegido se guarda en promotionType)
    }

    private final Position from;
    private final Position to;
    private final Type type;
    private PieceType promotionType; // sólo se usa cuando type == PROMOTION

    public Move(Position from, Position to, Type type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public Move(Position from, Position to) {
        this(from, to, Type.NORMAL);
    }

    public Position from() { return from; }
    public Position to()   { return to; }
    public Type type()     { return type; }

    public PieceType promotionType()             { return promotionType; }
    public void setPromotionType(PieceType type) { this.promotionType = type; }

    public boolean isPromotion()    { return type == Type.PROMOTION; }
    public boolean isEnPassant()    { return type == Type.EN_PASSANT; }
    public boolean isCastle()       { return type == Type.CASTLE_KINGSIDE || type == Type.CASTLE_QUEENSIDE; }

    @Override
    public String toString() {
        return from.toAlgebraic() + " " + to.toAlgebraic();
    }
}
