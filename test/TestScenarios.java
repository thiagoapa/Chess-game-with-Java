import com.chess.factory.*;
import com.chess.game.Game;
import com.chess.model.*;
import com.chess.pieces.*;

/**
 * Banco de pruebas (NO forma parte del juego entregable). Verifica las reglas
 * especiales construyendo posiciones concretas y comprobando el resultado.
 */
public class TestScenarios {

    private static int passed = 0, failed = 0;
    private static final WhitePieceFactory W = new WhitePieceFactory();
    private static final BlackPieceFactory B = new BlackPieceFactory();

    public static void main(String[] args) {
        testCastlingKingside();
        testEnPassant();
        testPromotion();
        testStalemate();
        testPinnedPieceCannotMove();
        testCheckmateScholars();

        System.out.println("\n========== RESULTADO ==========");
        System.out.println("Pasaron: " + passed + " | Fallaron: " + failed);
        if (failed > 0) System.exit(1);
    }

    static Position pos(String s) { return Position.fromAlgebraic(s); }

    static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("[OK]   " + name); }
        else           { failed++; System.out.println("[FALLA] " + name); }
    }

    // ---- Enroque corto ----
    static void testCastlingKingside() {
        Board b = new Board();
        b.setPiece(pos("e1"), W.createKing());
        b.setPiece(pos("h1"), W.createRook());
        b.setPiece(pos("e8"), B.createKing());
        Game g = new Game(b, Color.WHITE);

        Move m = g.findLegalMove(pos("e1"), pos("g1"));
        check("Enroque corto: jugada disponible", m != null && m.type() == Move.Type.CASTLE_KINGSIDE);
        if (m != null) {
            g.commitMove(m);
            Piece king = b.pieceAt(pos("g1"));
            Piece rook = b.pieceAt(pos("f1"));
            check("Enroque corto: rey en g1", king != null && king.type() == PieceType.KING);
            check("Enroque corto: torre en f1", rook != null && rook.type() == PieceType.ROOK);
            check("Enroque corto: h1 vacía", b.isEmpty(pos("h1")));
        }
    }

    // ---- Captura al paso ----
    static void testEnPassant() {
        Board b = new Board();
        b.setPiece(pos("e5"), W.createPawn());
        b.setPiece(pos("d7"), B.createPawn());
        b.setPiece(pos("e1"), W.createKing());
        b.setPiece(pos("h8"), B.createKing());
        Game g = new Game(b, Color.BLACK);

        // Negras avanzan dos: habilita captura al paso en d6
        Move dbl = g.findLegalMove(pos("d7"), pos("d5"));
        check("Al paso: avance doble negro disponible", dbl != null && dbl.type() == Move.Type.DOUBLE_PAWN_PUSH);
        if (dbl != null) g.commitMove(dbl);

        Move ep = g.findLegalMove(pos("e5"), pos("d6"));
        check("Al paso: captura disponible en d6", ep != null && ep.type() == Move.Type.EN_PASSANT);
        if (ep != null) {
            g.commitMove(ep);
            check("Al paso: peón blanco llega a d6", b.pieceAt(pos("d6")) != null);
            check("Al paso: peón negro de d5 eliminado", b.isEmpty(pos("d5")));
        }
    }

    // ---- Coronación ----
    static void testPromotion() {
        Board b = new Board();
        b.setPiece(pos("a7"), W.createPawn());
        b.setPiece(pos("e1"), W.createKing());
        b.setPiece(pos("e8"), B.createKing());
        Game g = new Game(b, Color.WHITE);

        Move m = g.findLegalMove(pos("a7"), pos("a8"));
        check("Coronación: jugada marcada como promoción", m != null && m.isPromotion());
        if (m != null) {
            m.setPromotionType(PieceType.QUEEN);
            g.commitMove(m);
            Piece p = b.pieceAt(pos("a8"));
            check("Coronación: aparece dama blanca en a8",
                    p != null && p.type() == PieceType.QUEEN && p.color() == Color.WHITE);
        }
    }

    // ---- Ahogado (stalemate) ----
    static void testStalemate() {
        Board b = new Board();
        b.setPiece(pos("a8"), B.createKing());
        b.setPiece(pos("c7"), W.createQueen());
        b.setPiece(pos("e1"), W.createKing());
        Game g = new Game(b, Color.BLACK);

        boolean noMoves = g.legalMoves(Color.BLACK).isEmpty();
        boolean notInCheck = !b.isKingInCheck(Color.BLACK);
        check("Ahogado: el negro no tiene jugadas legales", noMoves);
        check("Ahogado: el rey negro NO está en jaque", notInCheck);
    }

    // ---- Pieza clavada no puede moverse ----
    static void testPinnedPieceCannotMove() {
        Board b = new Board();
        b.setPiece(pos("e1"), W.createKing());
        b.setPiece(pos("e2"), W.createKnight()); // clavado por la torre en e8
        b.setPiece(pos("e8"), B.createRook());
        b.setPiece(pos("a8"), B.createKing());
        Game g = new Game(b, Color.WHITE);

        check("Clavada: el caballo clavado no tiene jugadas legales",
                g.legalMovesFrom(pos("e2")).isEmpty());
    }

    // ---- Jaque mate del pastor ----
    static void testCheckmateScholars() {
        // Posición de mate del pastor (dama en f7 apoyada por alfil en c4).
        Board b = new Board();
        b.setupInitialPosition();
        Game g = new Game(b, Color.WHITE);
        play(g, "e2","e4"); play(g, "e7","e5");
        play(g, "f1","c4"); play(g, "b8","c6");
        play(g, "d1","h5"); play(g, "g8","f6");
        play(g, "h5","f7"); // Dxf7#
        check("Mate del pastor: la partida termina", g.state().isGameOver());
        check("Mate del pastor: es jaque mate (no ahogado)",
                g.state().statusLine().contains("MATE"));
    }

    static void play(Game g, String from, String to) {
        Move m = g.findLegalMove(pos(from), pos(to));
        if (m == null) { System.out.println("  (jugada ilegal en test: " + from + " " + to + ")"); return; }
        if (m.isPromotion()) m.setPromotionType(PieceType.QUEEN);
        g.commitMove(m);
    }
}
