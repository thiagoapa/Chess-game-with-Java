package com.chess.strategy;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;

import java.util.List;

/**
 * Patrón STRATEGY.
 *
 * Cada tipo de pieza implementa esta interfaz con su propia regla de movimiento.
 * La pieza delega aquí, sin conocer los detalles. Las jugadas devueltas son
 * "pseudo-legales": respetan el movimiento de la pieza pero NO verifican si
 * dejan al rey propio en jaque (de eso se encarga el tablero al filtrarlas).
 */
public interface MovementStrategy {
    List<Move> generateMoves(Board board, Position from, Piece piece);
}
