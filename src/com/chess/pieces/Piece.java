package com.chess.pieces;

import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.strategy.MovementStrategy;

import java.util.List;

/**
 * Una pieza concreta del tablero.
 *
 * Punto clave del patrón STRATEGY: la pieza NO sabe cómo se mueve. Delega la
 * generación de jugadas a un objeto {@link MovementStrategy} intercambiable.
 * Esto separa la identidad de la pieza (su tipo y color) de su comportamiento
 * (cómo se mueve), y permitiría, por ejemplo, variantes de ajedrez cambiando
 * sólo la estrategia.
 */
public class Piece {
    private final Color color;
    private final PieceType type;
    private final MovementStrategy movementStrategy;
    private boolean hasMoved = false;

    public Piece(Color color, PieceType type, MovementStrategy movementStrategy) {
        this.color = color;
        this.type = type;
        this.movementStrategy = movementStrategy;
    }

    /** Delega en la estrategia la generación de jugadas pseudo-legales. */
    public List<Move> generateMoves(Board board, Position from) {
        return movementStrategy.generateMoves(board, from, this);
    }

    public Color color()    { return color; }
    public PieceType type() { return type; }

    public boolean hasMoved()              { return hasMoved; }
    public void setHasMoved(boolean moved) { this.hasMoved = moved; }

    public boolean isEnemyOf(Piece other) {
        return other != null && other.color != this.color;
    }

    public boolean isAllyOf(Piece other) {
        return other != null && other.color == this.color;
    }

    /**
     * Símbolo ASCII para dibujar el tablero de forma portable en cualquier
     * terminal: MAYÚSCULA = blancas, minúscula = negras.
     * (P/p peón, N/n caballo, B/b alfil, R/r torre, Q/q dama, K/k rey).
     */
    public char symbol() {
        char letter = type.letter(); // ya viene en mayúscula
        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
    }

    /** Copia superficial usada por la clonación del tablero (patrón Prototype). */
    public Piece copy() {
        Piece c = new Piece(color, type, movementStrategy); // la estrategia no tiene estado, se reutiliza
        c.hasMoved = this.hasMoved;
        return c;
    }

    @Override
    public String toString() {
        return color.displayName() + " " + type;
    }
}
