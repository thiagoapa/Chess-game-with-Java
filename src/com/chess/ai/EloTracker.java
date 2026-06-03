package com.chess.ai;

import com.chess.game.Game;
import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;

import java.util.List;

/**
 * Avalia a qualidade de cada jogada do jogador humano comparando-a com o melhor
 * movimento possível (via minimax depth-2) e ajusta um ELO estimado em tempo real.
 *
 * Delta de centipeões vs mudança de ELO:
 *   ≤ 0   → melhor jogada       +8
 *   < 30  → excelente           +5
 *   < 100 → boa jogada          +2
 *   < 200 → imprecisão          -5
 *   < 500 → erro                -15
 *   ≥ 500 → erro grave          -30
 */
public class EloTracker {

    private static final int SEARCH_DEPTH = 3;

    private double elo;
    private int    lastDelta;
    private String lastQuality;

    public EloTracker(double startingElo) {
        this.elo         = startingElo;
        this.lastDelta   = 0;
        this.lastQuality = "";
    }

    public int    getElo()        { return (int) Math.round(elo); }
    public int    getLastDelta()  { return lastDelta; }
    public String getLastQuality(){ return lastQuality; }

    /**
     * Avalia a jogada do humano e atualiza o ELO interno.
     *
     * @param boardBefore estado do tabuleiro ANTES da jogada
     * @param humanMove   jogada que o humano fez
     * @param humanColor  cor do jogador humano
     */
    public void rateMove(Board boardBefore, Move humanMove, Color humanColor) {
        Game tempGame = new Game(boardBefore, humanColor);
        List<Move> legalMoves = tempGame.legalMoves(humanColor);

        if (legalMoves.size() <= 1) {
            lastDelta   = 0;
            lastQuality = "";
            return;
        }

        int bestScore   = Integer.MIN_VALUE / 2;
        int actualScore = Integer.MIN_VALUE / 2;

        for (Move m : legalMoves) {
            Board next = boardBefore.copy();
            next.applyMove(m);
            int score = minimax(next, humanColor.opposite(), SEARCH_DEPTH - 1,
                    Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2, humanColor);

            if (score > bestScore) bestScore = score;
            if (sameSquares(m, humanMove)) actualScore = score;
        }

        if (actualScore == Integer.MIN_VALUE / 2) {
            lastDelta   = 0;
            lastQuality = "";
            return;
        }

        int centipawnLoss = bestScore - actualScore;

        // Thresholds calibrados para busca de profundidade 3 com avaliação posicional.
        // 100 cp = 1 peão; valores baseados nos padrões do chess.com (accuracy por lance).
        if (centipawnLoss <= 10) {
            lastDelta   = 15;
            lastQuality = "Melhor jogada! ★";
        } else if (centipawnLoss < 50) {
            lastDelta   = 8;
            lastQuality = "Excelente ✓";
        } else if (centipawnLoss < 150) {
            lastDelta   = 3;
            lastQuality = "Boa jogada";
        } else if (centipawnLoss < 300) {
            lastDelta   = -12;
            lastQuality = "Imprecisão";
        } else if (centipawnLoss < 600) {
            lastDelta   = -40;
            lastQuality = "Erro";
        } else {
            lastDelta   = -80;
            lastQuality = "Erro grave!";
        }

        elo = Math.max(100, elo + lastDelta);
    }

    private boolean sameSquares(Move a, Move b) {
        return a.from().equals(b.from()) && a.to().equals(b.to());
    }

    private int minimax(Board board, Color toMove, int depth,
                        int alpha, int beta, Color humanColor) {
        Game temp  = new Game(board, toMove);
        List<Move> moves = temp.legalMoves(toMove);

        if (moves.isEmpty()) {
            return board.isKingInCheck(toMove)
                    ? (toMove == humanColor ? -100000 : 100000)
                    : 0;
        }
        if (depth == 0) return evaluate(board, humanColor);

        boolean maximizing = (toMove == humanColor);
        int best = maximizing ? Integer.MIN_VALUE / 2 : Integer.MAX_VALUE / 2;

        for (Move m : moves) {
            Board next = board.copy();
            next.applyMove(m);
            int score = minimax(next, toMove.opposite(), depth - 1, alpha, beta, humanColor);

            if (maximizing) {
                best  = Math.max(best, score);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, score);
                beta = Math.min(beta, best);
            }
            if (beta <= alpha) break;
        }
        return best;
    }

    private int evaluate(Board board, Color humanColor) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.pieceAt(new Position(r, c));
                if (p == null) continue;
                int v = PieceValues.valueOf(p.type()) * 100 + positionalBonus(p, r, c);
                score += (p.color() == humanColor) ? v : -v;
            }
        }
        return score;
    }

    /**
     * Bônus posicional por centralidade e atividade da peça.
     * Sem isso, todas as posições sem capturas avaliavam igual e qualquer
     * lance recebia "Melhor jogada!" independente da qualidade.
     */
    private int positionalBonus(Piece p, int row, int col) {
        // Distância ao centro (casillas d4/d5/e4/e5)
        int dr = Math.min(Math.abs(row - 3), Math.abs(row - 4));
        int dc = Math.min(Math.abs(col - 3), Math.abs(col - 4));
        int centrality = (3 - dr) + (3 - dc); // 0 nas bordas, até 6 no centro

        switch (p.type()) {
            case KNIGHT: return centrality * 8;  // cavalo no centro vale muito mais
            case BISHOP: return centrality * 6;  // bispo ativo
            case PAWN:   return centrality * 3;  // peão central
            case QUEEN:  return centrality * 2;
            default:     return 0;
        }
    }
}
