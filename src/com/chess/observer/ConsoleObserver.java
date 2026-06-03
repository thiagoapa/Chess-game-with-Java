package com.chess.observer;

import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.pieces.Piece;
import com.chess.state.GameState;

/**
 * Observador concreto que imprime los eventos de la partida en la terminal.
 * Es uno de los posibles "oyentes"; podrían añadirse otros sin modificar el motor.
 */
public class ConsoleObserver implements GameObserver {

    @Override
    public void onMovePlayed(Color mover, Move move, Piece captured) {
        StringBuilder sb = new StringBuilder();
        sb.append("  > ").append(mover.displayName()).append(": ").append(move);

        if (move.isCastle()) {
            sb.append(move.type() == Move.Type.CASTLE_KINGSIDE ? "  (enroque corto)" : "  (enroque largo)");
        }
        if (move.isEnPassant()) {
            sb.append("  (captura al paso)");
        }
        if (move.isPromotion()) {
            sb.append("  (coronaci\u00f3n a ").append(move.promotionType()).append(")");
        }
        if (captured != null) {
            sb.append("  \u00a1captura ").append(captured.type()).append("!");
        }
        System.out.println(sb);
    }

    @Override
    public void onCheck(Color sideInCheck) {
        System.out.println("  ** \u00a1JAQUE a las " + sideInCheck.displayName() + "! **");
    }

    @Override
    public void onGameEnded(GameState finalState) {
        System.out.println("\n==============================================");
        System.out.println("  " + finalState.statusLine());
        System.out.println("==============================================");
    }
}
