package com.chess;

import com.chess.ai.AIPlayer;
import com.chess.ai.EloTracker;
import com.chess.game.Game;
import com.chess.model.Board;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.pieces.Piece;
import com.chess.pieces.PieceType;
import com.chess.state.CheckmateState;
import com.chess.state.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Interfaz gráfica 2D del ajedrez, hecha con Swing (incluido en el JDK, sin
 * librerías externas).
 *
 * Punto importante para la presentación: esta clase es SÓLO una vista. Reutiliza
 * exactamente el mismo motor ({@link Game}) y los mismos cinco patrones de
 * diseño que la versión de terminal. No duplica ni una sola regla del ajedrez:
 * pregunta al motor qué jugadas son legales y le ordena ejecutar la elegida.
 *
 * Interacción: se hace clic en una pieza propia (se resaltan sus destinos
 * legales) y luego clic en la casilla de destino para mover.
 */
public class ChessGUI extends JFrame {

    private static final int SQUARE = 80;            // tamaño de cada casilla en píxeles
    private static final Color2 LIGHT = new Color2(0xEEEED2);
    private static final Color2 DARK  = new Color2(0x769656);
    private static final Color2 SEL   = new Color2(0xF6F669); // casilla seleccionada
    private static final Color2 HINT  = new Color2(0xBBCB6B); // destinos legales

    private final Game game = new Game();
    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel statusLabel = new JLabel();

    private final AIPlayer ai;           // null => dos jugadores; si no, controla las Negras
    private final EloTracker eloTracker; // null em modo 2 jogadores
    private boolean aiThinking = false;

    private JLabel eloLabel;           // exibido no painel de jogadores (apenas vs IA)

    private Position selected;        // casilla de origen elegida (o null)
    private List<Move> selectedMoves; // jugadas legales desde 'selected'

    public ChessGUI() {
        this.ai = askMode();
        this.eloTracker = (ai != null) ? new EloTracker(1200) : null;

        setTitle("Xadrez ♥ Padrões de Design GoF");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new java.awt.Color(0x2B2B2B));

        // ---- Painel de jogadores (estilo chess.com) ----
        JPanel players = buildPlayerPanel();

        // ---- Barra de status + botão ----
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setForeground(java.awt.Color.WHITE);
        statusLabel.setBackground(new java.awt.Color(0x3D3D3D));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JButton newGameButton = new JButton("Nova partida");
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        newGameButton.setBackground(new java.awt.Color(0x769656));
        newGameButton.setForeground(java.awt.Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        newGameButton.addActionListener(e -> restart());

        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setBackground(new java.awt.Color(0x3D3D3D));
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        bottom.add(statusLabel, BorderLayout.CENTER);
        bottom.add(newGameButton, BorderLayout.EAST);

        add(players,    BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(bottom,     BorderLayout.SOUTH);

        refreshStatus();
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Constrói o painel superior com os dois jogadores, igual ao chess.com:
     *   linha de cima  = peças negras (MaxBot ou jogador 2)
     *   linha de baixo = peças brancas (você)
     */
    private JPanel buildPlayerPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 1));
        panel.setBackground(new java.awt.Color(0x2B2B2B));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        String blackName = (ai != null)
                ? AIPlayer.USERNAME + "   ♥ " + AIPlayer.ELO + " Elo"
                : "Jogador 2   ♟  (Negras)";
        String whiteName = (ai != null)
                ? "Você   ♙  (Brancas)"
                : "Jogador 1   ♙  (Brancas)";

        // Linha do jogador preto (estática)
        panel.add(playerLabel(blackName, false));

        // Linha do jogador branco + contador de ELO (apenas vs IA)
        JPanel whiteRow = new JPanel(new BorderLayout(8, 0));
        whiteRow.setBackground(new java.awt.Color(0x2B2B2B));
        whiteRow.add(playerLabel(whiteName, true), BorderLayout.WEST);

        if (ai != null) {
            eloLabel = new JLabel("ELO: 1200");
            eloLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            eloLabel.setForeground(new java.awt.Color(0xF6C90E));
            eloLabel.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
            whiteRow.add(eloLabel, BorderLayout.EAST);
        }

        panel.add(whiteRow);
        return panel;
    }

    /** Atualiza o label de ELO após avaliar a jogada do humano. */
    private void updateEloLabel() {
        if (eloLabel == null || eloTracker == null) return;
        int delta       = eloTracker.getLastDelta();
        String quality  = eloTracker.getLastQuality();
        String deltaStr = (delta > 0 ? "  +" : "  ") + delta;
        String qualStr  = quality.isEmpty() ? "" : "  " + quality;
        eloLabel.setText("ELO: " + eloTracker.getElo() + deltaStr + qualStr);
        eloLabel.setForeground(delta >= 0
                ? new java.awt.Color(0x79D279)   // verde para ganho
                : new java.awt.Color(0xFF6B6B)); // vermelho para perda
    }

    private JLabel playerLabel(String text, boolean white) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(white ? new java.awt.Color(0xEEEED2) : new java.awt.Color(0xAAAAAA));
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        return lbl;
    }

    /** Diálogo inicial: dos jugadores o contra la máquina. Devuelve la IA (Negras) o null. */
    private AIPlayer askMode() {
        String[] options = {"Dos jugadores", "Contra la m\u00e1quina"};
        int choice = JOptionPane.showOptionDialog(
                null, "\u00bfC\u00f3mo quieres jugar?", "Modo de juego",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        // Contra la máquina: el humano lleva Blancas, la IA las Negras.
        return (choice == 1) ? new AIPlayer(Color.BLACK, 3) : null;
    }

    private void restart() {
        // Reinicia creando un juego nuevo (se reconstruye el estado inicial).
        ChessGUI fresh = new ChessGUI();
        fresh.setVisible(true);
        dispose();
    }

    private void refreshStatus() {
        statusLabel.setText(game.state().statusLine());
    }

    /** Maneja el clic en una casilla (fila, columna del modelo). */
    private void handleClick(int row, int col) {
        if (game.state().isGameOver()) return;
        if (aiThinking) return; // ignora clics mientras la máquina calcula su jugada

        Position clicked = new Position(row, col);
        Piece piece = game.board().pieceAt(clicked);

        // Primer clic: seleccionar una pieza propia.
        if (selected == null) {
            if (piece != null && piece.color() == game.currentColor()) {
                selected = clicked;
                selectedMoves = game.legalMovesFrom(clicked);
            }
            boardPanel.repaint();
            return;
        }

        // Si vuelve a hacer clic en otra pieza propia, cambia la selección.
        if (piece != null && piece.color() == game.currentColor()) {
            selected = clicked;
            selectedMoves = game.legalMovesFrom(clicked);
            boardPanel.repaint();
            return;
        }

        // Segundo clic: intentar mover a la casilla destino.
        Move move = game.findLegalMove(selected, clicked);
        if (move != null) {
            if (move.isPromotion()) {
                move.setPromotionType(askPromotion());
            }

            // Captura o estado do tabuleiro antes de aplicar a jogada
            final Board boardBefore  = game.board().copy();
            final Move  committedMove = move;
            final Color humanColor   = game.currentColor();

            game.commitMove(move); // el motor aplica la jugada y avanza el estado
            refreshStatus();
            selected = null;
            selectedMoves = null;
            boardPanel.repaint();

            if (game.state().isGameOver()) {
                announceEnd();
            } else if (ai != null && game.currentColor() == ai.color()) {
                // Avalia a jogada em background enquanto a IA pensa
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() {
                        eloTracker.rateMove(boardBefore, committedMove, humanColor);
                        return null;
                    }
                    @Override protected void done() { updateEloLabel(); }
                }.execute();
                triggerAiMove();
            }
            return;
        }

        // Limpia la selección (haya movido o no).
        selected = null;
        selectedMoves = null;
        boardPanel.repaint();
    }

    /**
     * Hace que la máquina calcule y juegue su movimiento. El cálculo corre en un
     * hilo aparte (SwingWorker) para no congelar la ventana mientras "piensa";
     * al terminar, aplica la jugada en el hilo de la interfaz.
     */
    private void triggerAiMove() {
        aiThinking = true;
        statusLabel.setText(AIPlayer.USERNAME + " est\u00e1 pensando...");

        new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                return ai.chooseMove(game); // cálculo pesado fuera del hilo de UI
            }

            @Override
            protected void done() {
                aiThinking = false;
                try {
                    Move aiMove = get();
                    if (aiMove != null) {
                        game.commitMove(aiMove);
                    }
                } catch (Exception ignored) {
                    // si algo falla, simplemente se devuelve el turno al humano
                }
                refreshStatus();
                boardPanel.repaint();
                if (game.state().isGameOver()) {
                    announceEnd();
                }
            }
        }.execute();
    }

    /** Diálogo de coronación: elige a qué pieza ascender el peón. */
    private PieceType askPromotion() {
        String[] options = {"Dama", "Torre", "Alfil", "Caballo"};
        int choice = JOptionPane.showOptionDialog(
                this, "Corona el peón. ¿A qué pieza?", "Coronación",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        switch (choice) {
            case 1:  return PieceType.ROOK;
            case 2:  return PieceType.BISHOP;
            case 3:  return PieceType.KNIGHT;
            default: return PieceType.QUEEN; // por defecto, dama
        }
    }

    private void announceEnd() {
        String msg;
        GameState st = game.state();
        if (st instanceof CheckmateState) {
            msg = "¡Jaque mate! Ganan las " + ((CheckmateState) st).winner().displayName() + ".";
        } else {
            msg = st.statusLine();
        }
        JOptionPane.showMessageDialog(this, msg, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Símbolo Unicode de una pieza (figuras de ajedrez). */
    private static String glyph(Piece piece) {
        boolean w = piece.color() == Color.WHITE;
        switch (piece.type()) {
            case KING:   return w ? "\u2654" : "\u265A";
            case QUEEN:  return w ? "\u2655" : "\u265B";
            case ROOK:   return w ? "\u2656" : "\u265C";
            case BISHOP: return w ? "\u2657" : "\u265D";
            case KNIGHT: return w ? "\u2658" : "\u265E";
            case PAWN:   return w ? "\u2659" : "\u265F";
            default:     return "";
        }
    }

    /** El tablero dibujado y clicable. */
    private class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(SQUARE * 8, SQUARE * 8));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    int viewCol = e.getX() / SQUARE;
                    int viewRow = e.getY() / SQUARE;
                    if (viewCol < 0 || viewCol > 7 || viewRow < 0 || viewRow > 7) return;
                    // La vista dibuja la fila 8 (rango 7 del modelo) arriba.
                    int modelRow = 7 - viewRow;
                    int modelCol = viewCol;
                    handleClick(modelRow, modelCol);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for (int viewRow = 0; viewRow < 8; viewRow++) {
                for (int viewCol = 0; viewCol < 8; viewCol++) {
                    int modelRow = 7 - viewRow;
                    int modelCol = viewCol;
                    Position pos = new Position(modelRow, modelCol);

                    int x = viewCol * SQUARE;
                    int y = viewRow * SQUARE;

                    // Color base de la casilla
                    boolean lightSquare = (modelRow + modelCol) % 2 != 0;
                    Color2 base = lightSquare ? LIGHT : DARK;

                    // Resaltados: selección y destinos legales
                    if (pos.equals(selected)) {
                        base = SEL;
                    } else if (isLegalTarget(pos)) {
                        base = HINT;
                    }

                    g2.setColor(base.toAwt());
                    g2.fillRect(x, y, SQUARE, SQUARE);

                    // Etiquetas de coordenadas (columna abajo, fila a la izquierda)
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    g2.setColor((lightSquare ? DARK : LIGHT).toAwt());
                    if (viewRow == 7) {
                        g2.drawString(String.valueOf((char) ('a' + modelCol)),
                                x + SQUARE - 12, y + SQUARE - 5);
                    }
                    if (viewCol == 0) {
                        g2.drawString(String.valueOf(modelRow + 1), x + 4, y + 14);
                    }

                    // Dibujar la pieza
                    Piece piece = game.board().pieceAt(pos);
                    if (piece != null) {
                        drawPiece(g2, piece, x, y);
                    }
                }
            }
        }

        private void drawPiece(Graphics2D g2, Piece piece, int x, int y) {
            String s = glyph(piece);
            Font font = new Font("Serif", Font.PLAIN, SQUARE - 16);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (SQUARE - fm.stringWidth(s)) / 2;
            int ty = y + (SQUARE - fm.getHeight()) / 2 + fm.getAscent();

            // Las piezas blancas se pintan en blanco con un contorno oscuro para
            // que se distingan también sobre las casillas claras; las negras, en negro.
            if (piece.color() == Color.WHITE) {
                g2.setColor(java.awt.Color.DARK_GRAY);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) g2.drawString(s, tx + dx, ty + dy);
                    }
                }
                g2.setColor(java.awt.Color.WHITE);
                g2.drawString(s, tx, ty);
            } else {
                g2.setColor(java.awt.Color.BLACK);
                g2.drawString(s, tx, ty);
            }
        }

        private boolean isLegalTarget(Position pos) {
            if (selectedMoves == null) return false;
            for (Move m : selectedMoves) {
                if (m.to().equals(pos)) return true;
            }
            return false;
        }
    }

    /** Pequeño envoltorio de color para no chocar con java.awt.Color en las constantes. */
    private static class Color2 {
        private final int rgb;
        Color2(int rgb) { this.rgb = rgb; }
        java.awt.Color toAwt() { return new java.awt.Color(rgb); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChessGUI().setVisible(true));
    }
}
