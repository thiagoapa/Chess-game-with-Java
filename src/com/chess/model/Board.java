package com.chess.model;

import com.chess.factory.AbstractPieceFactory;
import com.chess.factory.BlackPieceFactory;
import com.chess.factory.WhitePieceFactory;
import com.chess.pieces.Piece;
import com.chess.pieces.PieceType;

/**
 * El tablero 8x8 y su estado: las piezas, y la casilla de captura al paso vigente.
 *
 * Implementa el patrón PROTOTYPE mediante {@link #copy()}: el juego clona el
 * tablero para simular una jugada y comprobar si deja al rey propio en jaque,
 * sin tocar el tablero real.
 *
 * La detección de ataques ({@link #isSquareAttacked}) se calcula de forma
 * geométrica directa (no reutiliza las estrategias) para evitar recursión con
 * el enroque, que a su vez necesita saber qué casillas están atacadas.
 */
public class Board {
    private final Piece[][] grid = new Piece[8][8]; // grid[fila][columna]
    private Position enPassantTarget; // casilla "saltada" por un peón en su avance doble; null si no aplica

    /** Coloca la posición inicial estándar usando las fábricas (Abstract Factory). */
    public void setupInitialPosition() {
        AbstractPieceFactory white = new WhitePieceFactory();
        AbstractPieceFactory black = new BlackPieceFactory();

        // Peones
        for (int col = 0; col < 8; col++) {
            grid[1][col] = white.createPawn();
            grid[6][col] = black.createPawn();
        }
        // Piezas mayores blancas (fila 0) y negras (fila 7)
        placeBackRank(0, white);
        placeBackRank(7, black);
    }

    private void placeBackRank(int row, AbstractPieceFactory f) {
        grid[row][0] = f.createRook();
        grid[row][1] = f.createKnight();
        grid[row][2] = f.createBishop();
        grid[row][3] = f.createQueen();
        grid[row][4] = f.createKing();
        grid[row][5] = f.createBishop();
        grid[row][6] = f.createKnight();
        grid[row][7] = f.createRook();
    }

    // ---------- Acceso básico ----------

    public Piece pieceAt(Position p) {
        if (p == null || !p.isInsideBoard()) return null;
        return grid[p.row()][p.col()];
    }

    public boolean isEmpty(Position p) {
        return pieceAt(p) == null;
    }

    public void setPiece(Position p, Piece piece) {
        grid[p.row()][p.col()] = piece;
    }

    public Position enPassantTarget()            { return enPassantTarget; }
    public void setEnPassantTarget(Position p)   { this.enPassantTarget = p; }

    // ---------- Aplicación de jugadas ----------

    /**
     * Ejecuta una jugada mutando el tablero. Gestiona captura normal, captura al
     * paso, enroque (mueve también la torre) y coronación. Devuelve la pieza
     * capturada (o null). Tras una jugada, recalcula la casilla de captura al paso.
     */
    public Piece applyMove(Move move) {
        Piece moving = pieceAt(move.from());
        Piece captured = pieceAt(move.to());

        // Captura al paso: el peón capturado NO está en la casilla destino,
        // sino al lado del peón que captura.
        if (move.isEnPassant()) {
            Position capturedPawnPos = new Position(move.from().row(), move.to().col());
            captured = pieceAt(capturedPawnPos);
            setPiece(capturedPawnPos, null);
        }

        // Mover la pieza
        setPiece(move.from(), null);
        setPiece(move.to(), moving);
        if (moving != null) moving.setHasMoved(true);

        // Enroque: además del rey (ya movido arriba), mover la torre.
        if (move.type() == Move.Type.CASTLE_KINGSIDE) {
            Position rookFrom = new Position(move.from().row(), 7);
            Position rookTo   = new Position(move.from().row(), 5);
            moveRookForCastle(rookFrom, rookTo);
        } else if (move.type() == Move.Type.CASTLE_QUEENSIDE) {
            Position rookFrom = new Position(move.from().row(), 0);
            Position rookTo   = new Position(move.from().row(), 3);
            moveRookForCastle(rookFrom, rookTo);
        }

        // Coronación: reemplazar el peón por la pieza elegida (reina por defecto).
        if (move.isPromotion() && moving != null) {
            PieceType promoteTo = move.promotionType() != null ? move.promotionType() : PieceType.QUEEN;
            AbstractPieceFactory f = (moving.color() == Color.WHITE)
                    ? new WhitePieceFactory() : new BlackPieceFactory();
            Piece promoted = f.create(promoteTo);
            promoted.setHasMoved(true);
            setPiece(move.to(), promoted);
        }

        // Recalcular captura al paso: sólo válida el turno inmediatamente siguiente.
        if (move.type() == Move.Type.DOUBLE_PAWN_PUSH) {
            int skippedRow = (move.from().row() + move.to().row()) / 2;
            enPassantTarget = new Position(skippedRow, move.from().col());
        } else {
            enPassantTarget = null;
        }

        return captured;
    }

    private void moveRookForCastle(Position from, Position to) {
        Piece rook = pieceAt(from);
        setPiece(from, null);
        setPiece(to, rook);
        if (rook != null) rook.setHasMoved(true);
    }

    // ---------- Detección de ataques (para jaque y enroque) ----------

    /** ¿La casilla {@code target} está atacada por alguna pieza de color {@code byColor}? */
    public boolean isSquareAttacked(Position target, Color byColor) {
        return attackedByPawn(target, byColor)
                || attackedByKnight(target, byColor)
                || attackedByKing(target, byColor)
                || attackedBySliding(target, byColor, true)    // diagonales: alfil/dama
                || attackedBySliding(target, byColor, false);  // rectas: torre/dama
    }

    private boolean attackedByPawn(Position target, Color byColor) {
        // Un peón de byColor que estuviera una fila "detrás" (según su dirección)
        // y a una columna de distancia, atacaría la casilla target.
        int srcRow = target.row() - byColor.direction();
        for (int dCol : new int[]{-1, 1}) {
            Position p = new Position(srcRow, target.col() + dCol);
            Piece piece = pieceAt(p);
            if (piece != null && piece.color() == byColor && piece.type() == PieceType.PAWN) {
                return true;
            }
        }
        return false;
    }

    private boolean attackedByKnight(Position target, Color byColor) {
        int[][] jumps = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] j : jumps) {
            Piece piece = pieceAt(target.offset(j[0], j[1]));
            if (piece != null && piece.color() == byColor && piece.type() == PieceType.KNIGHT) {
                return true;
            }
        }
        return false;
    }

    private boolean attackedByKing(Position target, Color byColor) {
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue;
                Piece piece = pieceAt(target.offset(dRow, dCol));
                if (piece != null && piece.color() == byColor && piece.type() == PieceType.KING) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attackedBySliding(Position target, Color byColor, boolean diagonal) {
        int[][] dirs = diagonal
                ? new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}
                : new int[][]{{1,0},{-1,0},{0,1},{0,-1}};
        PieceType longRange = diagonal ? PieceType.BISHOP : PieceType.ROOK;

        for (int[] d : dirs) {
            Position p = target.offset(d[0], d[1]);
            while (p.isInsideBoard()) {
                Piece piece = pieceAt(p);
                if (piece != null) {
                    if (piece.color() == byColor
                            && (piece.type() == longRange || piece.type() == PieceType.QUEEN)) {
                        return true;
                    }
                    break; // una pieza (de cualquier color) bloquea el rayo
                }
                p = p.offset(d[0], d[1]);
            }
        }
        return false;
    }

    /** Ubica al rey del color dado. Devuelve null si no está (no debería pasar en una partida válida). */
    public Position findKing(Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = grid[r][c];
                if (piece != null && piece.color() == color && piece.type() == PieceType.KING) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    public boolean isKingInCheck(Color color) {
        Position king = findKing(color);
        return king != null && isSquareAttacked(king, color.opposite());
    }

    // ---------- PROTOTYPE: clonación profunda del tablero ----------

    /**
     * Patrón PROTOTYPE. Crea una copia profunda del tablero (cada pieza se clona)
     * para poder simular una jugada sin alterar el estado real, y así comprobar
     * la legalidad (que el rey propio no quede en jaque).
     */
    public Board copy() {
        Board clone = new Board();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = grid[r][c];
                clone.grid[r][c] = (piece == null) ? null : piece.copy();
            }
        }
        clone.enPassantTarget = this.enPassantTarget;
        return clone;
    }

    // ---------- Dibujo en terminal ----------

    public String render(boolean whiteAtBottom) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        int startRow = whiteAtBottom ? 7 : 0;
        int endRow   = whiteAtBottom ? -1 : 8;
        int step     = whiteAtBottom ? -1 : 1;

        for (int r = startRow; r != endRow; r += step) {
            sb.append(" ").append(r + 1).append(" |");
            for (int c = 0; c < 8; c++) {
                Piece piece = grid[r][c];
                char symbol = (piece == null) ? '.' : piece.symbol();
                sb.append(' ').append(symbol).append(' ');
            }
            sb.append("\n");
        }
        sb.append("   +").append("-".repeat(24)).append("\n");
        sb.append("     a  b  c  d  e  f  g  h\n");
        return sb.toString();
    }
}
