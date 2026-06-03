package com.chess.ai;

import com.chess.game.Game;
import com.chess.model.Color;
import com.chess.model.Move;

/**
 * Jugador humano. Implementa la misma interfaz {@link PlayerStrategy} que la
 * máquina, de modo que el juego trata a ambos por igual.
 *
 * La jugada no se calcula aquí: proviene de la entrada del usuario (teclado en
 * la terminal, o clic en la interfaz gráfica). Por eso {@link #chooseMove}
 * devuelve null: quien controla el bucle (Main o la GUI) obtiene la jugada del
 * usuario y la valida con el motor.
 */
public class HumanPlayer implements PlayerStrategy {

    private final Color color;

    public HumanPlayer(Color color) {
        this.color = color;
    }

    @Override
    public Move chooseMove(Game game) {
        return null; // la jugada la aporta la interfaz a partir de la entrada del usuario
    }

    @Override
    public String name() {
        return "Humano (" + color.displayName() + ")";
    }

    public boolean isHuman() {
        return true;
    }
}
