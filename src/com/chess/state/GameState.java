package com.chess.state;

import com.chess.model.Color;

/**
 * Patrón STATE.
 *
 * Representa la situación de la partida tras cada jugada. El bucle principal
 * pregunta al estado si la partida terminó y qué mostrar, sin usar una maraña de
 * banderas booleanas. Tras cada movimiento, el juego transiciona al estado que
 * corresponda (en curso, jaque, jaque mate, ahogado, tablas).
 */
public interface GameState {

    /** ¿La partida terminó? Si es true, el bucle principal se detiene. */
    boolean isGameOver();

    /** Color que debe mover ahora (null si la partida terminó). */
    Color sideToMove();

    /** Línea de estado para mostrar en la terminal. */
    String statusLine();
}
