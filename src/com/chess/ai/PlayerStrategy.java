package com.chess.ai;

import com.chess.game.Game;
import com.chess.model.Move;

/**
 * Patrón STRATEGY aplicado a la toma de decisiones de un jugador.
 *
 * Define cómo un jugador (humano o máquina) elige su jugada. El motor no sabe ni
 * le importa si detrás hay una persona o un algoritmo: sólo pide "elige tu
 * jugada". Esto permite enfrentar humano vs máquina, o máquina vs máquina, sin
 * tocar la lógica del ajedrez.
 */
public interface PlayerStrategy {

    /**
     * Devuelve la jugada elegida para la posición actual, o {@code null} si no
     * hay jugada disponible (lo decide el motor, no esta estrategia).
     */
    Move chooseMove(Game game);

    /** Nombre legible para mostrar de quién es el turno. */
    String name();
}
