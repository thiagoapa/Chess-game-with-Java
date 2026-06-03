package com.chess.ai;

import com.chess.game.Game;
import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;

import java.util.List;

/**
 * Jugador controlado por la máquina. Implementa {@link PlayerStrategy}.
 *
 * Usa el algoritmo <b>minimax con poda alfa-beta</b> a poca profundidad: mira
 * algunas jugadas hacia adelante y elige la que deja la mejor posición posible
 * suponiendo que el rival también juega bien. Es una IA sencilla pero que ya
 * captura material, evita perder piezas y busca el mate.
 *
 * Para explorar variantes sin alterar la partida real, clona el tablero
 * (patrón PROTOTYPE) y reutiliza la propia lógica de legalidad del motor
 * (creando un {@link Game} temporal sobre el clon), sin duplicar reglas.
 */
public class AIPlayer implements PlayerStrategy {

    /** Username e elo exibido no jogo (estilo chess.com). */
    public static final String USERNAME = "MaxBot";
    public static final int    ELO      = 900;

    private final Color color;
    private final int depth;

    public AIPlayer(Color color, int depth) {
        this.color = color;
        this.depth = Math.max(1, depth);
    }

    public Color color() { return color; }

    /** "MaxBot ♥ 900 Elo  (Negras)" */
    @Override
    public String name() {
        return USERNAME + " \u2665 " + ELO + " Elo  (" + color.displayName() + ")";
    }

    /** Linha curta para painel: "MaxBot   ♥ 900 Elo" */
    public static String displayLabel() {
        return USERNAME + "   \u2665 " + ELO + " Elo";
    }

    @Override
    public Move chooseMove(Game game) {
        List<Move> moves = game.legalMoves(color);
        if (moves.isEmpty()) return null;

        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;

        for (Move move : moves) {
            Board next = game.board().copy();   // PROTOTYPE: clon para simular
            next.applyMove(move);
            // Puntúa la posición resultante desde la perspectiva de la máquina.
            int score = minimax(next, color.opposite(), depth - 1, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return best;
    }

    /**
     * Minimax con poda alfa-beta. {@code toMove} es de quién es el turno en el
     * tablero simulado; maximiza cuando le toca a la máquina y minimiza cuando
     * le toca al rival.
     */
    private int minimax(Board board, Color toMove, int depth, int alpha, int beta) {
        List<Move> moves = legalMovesOn(board, toMove);

        // Caso terminal: sin jugadas (mate o ahogado) o profundidad agotada.
        if (moves.isEmpty()) {
            if (board.isKingInCheck(toMove)) {
                // toMove está en mate: pésimo para quien debía mover.
                return (toMove == color) ? -100000 : 100000;
            }
            return 0; // ahogado: tablas
        }
        if (depth == 0) {
            return evaluate(board);
        }

        boolean maximizing = (toMove == color);
        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : moves) {
            Board next = board.copy();
            next.applyMove(move);
            int score = minimax(next, toMove.opposite(), depth - 1, alpha, beta);

            if (maximizing) {
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, score);
                beta = Math.min(beta, best);
            }
            if (beta <= alpha) break; // poda: esta rama ya no puede mejorar
        }
        return best;
    }

    /**
     * Calcula las jugadas legales en un tablero simulado reutilizando el motor:
     * crea un Game temporal sobre ese tablero. Así la legalidad (incluido no
     * dejar al rey en jaque) se evalúa con la MISMA lógica del juego real.
     */
    private List<Move> legalMovesOn(Board board, Color toMove) {
        Game temp = new Game(board, toMove);
        return temp.legalMoves(toMove);
    }

    /**
     * Evalúa la posición desde la perspectiva de la máquina. Combina:
     *  - material (lo más importante: valor de las piezas), y
     *  - un pequeño bonus posicional por ocupar/controlar el centro,
     * para que la IA no haga jugadas pasivas cuando el material está igualado.
     * Positivo = bueno para la máquina; negativo = bueno para el rival.
     */
    private int evaluate(Board board) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.pieceAt(new Position(r, c));
                if (p == null) continue;

                int v = PieceValues.valueOf(p.type()) * 100; // material en centi-peones
                v += centerBonus(p, r, c);                    // bonus posicional pequeño

                score += (p.color() == color) ? v : -v;
            }
        }
        return score;
    }

    /** Bonus por cercanía al centro: el centro vale más para casi todas las piezas. */
    private int centerBonus(Piece p, int row, int col) {
        // Distancia al centro del tablero (entre filas/cols 3 y 4).
        int dr = Math.min(Math.abs(row - 3), Math.abs(row - 4));
        int dc = Math.min(Math.abs(col - 3), Math.abs(col - 4));
        int centrality = (3 - dr) + (3 - dc); // 0 en los bordes, hasta 6 en el centro

        switch (p.type()) {
            case KNIGHT:
            case BISHOP: return centrality * 5; // piezas menores aman el centro
            case PAWN:   return centrality * 2; // peones centrales algo mejores
            case QUEEN:  return centrality;     // la dama, leve
            default:     return 0;              // torres y rey: sin bonus aquí
        }
    }
}
