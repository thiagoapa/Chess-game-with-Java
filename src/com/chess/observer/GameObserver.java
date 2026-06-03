package com.chess.observer;

import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.pieces.Piece;
import com.chess.state.GameState;

/**
 * Patrón OBSERVER.
 *
 * El juego (sujeto) notifica eventos a sus observadores sin saber qué hacen con
 * ellos. Así la lógica del ajedrez queda separada de la presentación: hoy un
 * observador imprime en la terminal; mañana podría llevar un registro PGN,
 * reproducir sonidos o actualizar una interfaz gráfica, sin tocar el motor.
 */
public interface GameObserver {

    /** Se jugó un movimiento; {@code captured} es la pieza capturada o null. */
    void onMovePlayed(Color mover, Move move, Piece captured);

    /** El bando {@code sideInCheck} quedó en jaque tras la última jugada. */
    void onCheck(Color sideInCheck);

    /** La partida terminó; {@code finalState} describe el resultado. */
    void onGameEnded(GameState finalState);
}
