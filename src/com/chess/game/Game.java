package com.chess.game;

import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.observer.GameObserver;
import com.chess.pieces.Piece;
import com.chess.pieces.PieceType;
import com.chess.state.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de la partida. Coordina los cinco patrones:
 *  - usa la fábrica (Abstract Factory) al inicializar el tablero,
 *  - delega movimientos en las estrategias (Strategy) de cada pieza,
 *  - clona el tablero (Prototype) para validar la legalidad,
 *  - mantiene el estado de la partida (State),
 *  - y avisa a sus observadores (Observer) de cada evento.
 */
public class Game {

    private final Board board;
    private final List<GameObserver> observers = new ArrayList<>();
    private GameState state;

    /** Partida nueva desde la posición inicial estándar. */
    public Game() {
        this.board = new Board();
        this.board.setupInitialPosition();
        this.state = new OngoingState(Color.WHITE); // empiezan las blancas
    }

    /** Partida desde una posición dada (útil para cargar finales o para pruebas). */
    public Game(Board board, Color toMove) {
        this.board = board;
        this.state = new OngoingState(toMove);
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public Board board()      { return board; }
    public GameState state()  { return state; }

    /** Color al que le toca mover (null si la partida terminó). */
    public Color currentColor() {
        return state.sideToMove();
    }

    // ---------- Generación de jugadas LEGALES ----------

    /** Todas las jugadas legales del color indicado (ya filtradas por seguridad del rey). */
    public List<Move> legalMoves(Color color) {
        List<Move> legal = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.pieceAt(from);
                if (piece != null && piece.color() == color) {
                    for (Move m : piece.generateMoves(board, from)) {
                        if (isKingSafeAfter(m, color)) {
                            legal.add(m);
                        }
                    }
                }
            }
        }
        return legal;
    }

    /** Jugadas legales que parten de una casilla concreta. */
    public List<Move> legalMovesFrom(Position from) {
        List<Move> result = new ArrayList<>();
        Piece piece = board.pieceAt(from);
        if (piece == null || piece.color() != currentColor()) return result;
        for (Move m : piece.generateMoves(board, from)) {
            if (isKingSafeAfter(m, piece.color())) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * PROTOTYPE en acción: clona el tablero, aplica la jugada en la copia y
     * comprueba que el rey propio no quede en jaque. El tablero real no se toca.
     */
    private boolean isKingSafeAfter(Move move, Color color) {
        Board simulated = board.copy();
        simulated.applyMove(move);
        return !simulated.isKingInCheck(color);
    }

    /** Busca la jugada legal que va de {@code from} a {@code to}, o null si no existe. */
    public Move findLegalMove(Position from, Position to) {
        for (Move m : legalMovesFrom(from)) {
            if (m.to().equals(to)) {
                return m;
            }
        }
        return null;
    }

    // ---------- Ejecución de una jugada ya validada ----------

    /**
     * Aplica una jugada legal: actualiza el tablero, notifica a los observadores
     * y transiciona al nuevo estado (turno rival, jaque, mate, ahogado o tablas).
     */
    public void commitMove(Move move) {
        Color mover = currentColor();
        Piece captured = board.applyMove(move);

        notifyMovePlayed(mover, move, captured);

        Color next = mover.opposite();
        transitionStateFor(next);
    }

    private void transitionStateFor(Color next) {
        boolean inCheck = board.isKingInCheck(next);
        boolean hasMoves = !legalMoves(next).isEmpty();

        if (!hasMoves && inCheck) {
            state = new CheckmateState(next);
            notifyGameEnded();
        } else if (!hasMoves) {
            state = new StalemateState();
            notifyGameEnded();
        } else if (isInsufficientMaterial()) {
            state = new DrawState("material insuficiente");
            notifyGameEnded();
        } else if (inCheck) {
            state = new CheckState(next);
            notifyCheck(next);
        } else {
            state = new OngoingState(next);
        }
    }

    /** Tablas elementales: en el tablero sólo quedan los dos reyes. */
    private boolean isInsufficientMaterial() {
        int count = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.pieceAt(new Position(r, c));
                if (p != null && p.type() != PieceType.KING) {
                    count++;
                }
            }
        }
        return count == 0;
    }

    // ---------- Notificaciones a observadores ----------

    private void notifyMovePlayed(Color mover, Move move, Piece captured) {
        for (GameObserver o : observers) o.onMovePlayed(mover, move, captured);
    }

    private void notifyCheck(Color sideInCheck) {
        for (GameObserver o : observers) o.onCheck(sideInCheck);
    }

    private void notifyGameEnded() {
        for (GameObserver o : observers) o.onGameEnded(state);
    }
}
