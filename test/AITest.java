import com.chess.ai.AIPlayer;
import com.chess.factory.*;
import com.chess.game.Game;
import com.chess.model.*;

public class AITest {
    static int passed=0, failed=0;
    static final WhitePieceFactory W = new WhitePieceFactory();
    static final BlackPieceFactory B = new BlackPieceFactory();
    static Position pos(String s){ return Position.fromAlgebraic(s); }
    static void check(String n, boolean c){ if(c){passed++;System.out.println("[OK]   "+n);} else {failed++;System.out.println("[FALLA] "+n);} }

    public static void main(String[] a){
        testCapturaGratis();
        testMateEn1();
        testNoSeSuicida();
        System.out.println("\nPasaron: "+passed+" | Fallaron: "+failed);
        if(failed>0) System.exit(1);
    }

    // La IA (blancas) tiene un caballo en d5 y hay una dama negra indefensa en e7: debe capturarla? 
    // Mejor: torre blanca en a1, dama negra en a8 indefensa -> Txa8.
    static void testCapturaGratis(){
        Board b = new Board();
        b.setPiece(pos("e1"), W.createKing());
        b.setPiece(pos("a1"), W.createRook());
        b.setPiece(pos("a8"), B.createQueen()); // dama negra indefensa en la columna a
        b.setPiece(pos("h8"), B.createKing());
        Game g = new Game(b, Color.WHITE);
        AIPlayer ai = new AIPlayer(Color.WHITE, 3);
        Move m = ai.chooseMove(g);
        check("IA captura dama indefensa (a1->a8)", m!=null && m.to().equals(pos("a8")));
    }

    // Mate en 1: dama blanca y rey, rey negro en esquina. Dama da mate.
    static void testMateEn1(){
        Board b = new Board();
        b.setPiece(pos("h8"), B.createKing());
        b.setPiece(pos("f7"), W.createQueen()); // Qf7->g7? busquemos mate: Q en g6 da mate con rey apoyando
        b.setPiece(pos("g6"), W.createKing());  // rey blanco apoya g7
        // Dama en f7, rey negro h8, rey blanco g6: Qg7 es mate.
        Game g = new Game(b, Color.WHITE);
        AIPlayer ai = new AIPlayer(Color.WHITE, 3);
        Move m = ai.chooseMove(g);
        if(m!=null){
            Board sim = b.copy(); sim.applyMove(m);
            boolean esMate = sim.isKingInCheck(Color.BLACK) && new Game(sim, Color.BLACK).legalMoves(Color.BLACK).isEmpty();
            check("IA encuentra el mate en 1 ("+m+")", esMate);
        } else check("IA encuentra el mate en 1", false);
    }

    // No debe entregar la dama: dama blanca en d1 puede ir a d7 donde la captura un peón. No debe elegir un movimiento que la pierda gratis.
    static void testNoSeSuicida(){
        Board b = new Board();
        b.setPiece(pos("e1"), W.createKing());
        b.setPiece(pos("d1"), W.createQueen());
        b.setPiece(pos("e8"), B.createKing());
        b.setPiece(pos("c8"), B.createRook()); // controla la columna c
        Game g = new Game(b, Color.WHITE);
        AIPlayer ai = new AIPlayer(Color.WHITE, 3);
        Move m = ai.chooseMove(g);
        // Tras la jugada, la dama no debería poder ser capturada gratis. Verificamos que no la dejó en c-file defendida por la torre.
        check("IA hace una jugada legal sin regalar la dama", m!=null && !m.to().equals(pos("c1")) && !m.to().equals(pos("c2")));
    }
}
