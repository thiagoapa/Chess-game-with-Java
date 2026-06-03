package com.chess;

import com.chess.ai.AIPlayer;
import com.chess.game.Game;
import com.chess.model.Color;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.observer.ConsoleObserver;
import com.chess.pieces.Piece;
import com.chess.pieces.PieceType;

import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada: ajedrez interactivo por terminal con notación tipo "e2 e4".
 * Reglas completas: enroque, captura al paso, coronación, jaque, jaque mate,
 * ahogado y tablas por material insuficiente.
 */
public class Main {

    public static void main(String[] args) {
        Game game = new Game();
        game.addObserver(new ConsoleObserver()); // Observer: anuncios en consola
        Scanner scanner = new Scanner(System.in);

        printWelcome();

        // Elegir modo: dos humanos, o humano contra la máquina (IA).
        AIPlayer ai = chooseMode(scanner);
        if (ai != null) {
            System.out.println("  \u250c\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510");
            System.out.println("  \u2502  \u265f  " + AIPlayer.USERNAME + "   \u2665 " + AIPlayer.ELO + " Elo    (Negras)    \u2502");
            System.out.println("  \u2502  \u2659  Voc\u00ea                   (Brancas)   \u2502");
            System.out.println("  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518\n");
        }

        while (!game.state().isGameOver()) {
            Color turn = game.currentColor();

            System.out.println(game.board().render(turn == Color.WHITE));
            System.out.println(game.state().statusLine());

            // Turno de la máquina (si hay IA y le toca a su color).
            if (ai != null && turn == ai.color()) {
                System.out.println("  " + AIPlayer.USERNAME + " (" + AIPlayer.ELO + " Elo) est\u00e1 pensando...");
                Move aiMove = ai.chooseMove(game);
                if (aiMove == null) break; // sin jugadas; el estado lo refleja
                game.commitMove(aiMove);
                continue;
            }

            System.out.print("Tu jugada (ej. 'e2 e4', o 'ayuda' / 'salir'): ");

            if (!scanner.hasNextLine()) {
                System.out.println("\n(entrada finalizada)");
                break;
            }
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("salir") || line.equalsIgnoreCase("exit")) {
                System.out.println("Partida abandonada. \u00a1Hasta la pr\u00f3xima!");
                return;
            }
            if (line.equalsIgnoreCase("ayuda") || line.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            if (line.equalsIgnoreCase("mov")) {
                printAllMoves(game, turn);
                continue;
            }

            handleMove(game, scanner, line);
        }

        System.out.println(game.board().render(true));
        System.out.println("Fin de la partida.");
    }

    /** Pregunta el modo de juego. Devuelve un AIPlayer (Negras) o null si son dos humanos. */
    private static AIPlayer chooseMode(Scanner scanner) {
        System.out.println("  Elige modo de juego:");
        System.out.println("    1) Dos jugadores (humano vs humano)");
        System.out.println("    2) Un jugador (humano vs m\u00e1quina)");
        while (true) {
            System.out.print("  Opci\u00f3n (1/2): ");
            if (!scanner.hasNextLine()) return null;
            String opt = scanner.nextLine().trim();
            if (opt.equals("1")) return null;
            if (opt.equals("2")) return new AIPlayer(Color.BLACK, 3); // profundidad 3: rápida y razonable
            System.out.println("  [!] Escribe 1 o 2.");
        }
    }

    /** Interpreta y ejecuta una jugada en notación "origen destino". */
    private static void handleMove(Game game, Scanner scanner, String line) {
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            System.out.println("  [!] Formato inv\u00e1lido. Usa por ejemplo: e2 e4");
            return;
        }

        Position from = Position.fromAlgebraic(parts[0]);
        Position to   = Position.fromAlgebraic(parts[1]);
        if (from == null || to == null) {
            System.out.println("  [!] Casilla inv\u00e1lida. Las columnas van de 'a' a 'h' y las filas de 1 a 8.");
            return;
        }

        Piece piece = game.board().pieceAt(from);
        if (piece == null) {
            System.out.println("  [!] No hay ninguna pieza en " + from + ".");
            return;
        }
        if (piece.color() != game.currentColor()) {
            System.out.println("  [!] Esa pieza es de las " + piece.color().displayName()
                    + " y no es su turno.");
            return;
        }

        Move move = game.findLegalMove(from, to);
        if (move == null) {
            System.out.println("  [!] Movimiento ilegal para " + piece.type()
                    + " de " + from + " a " + to + ".");
            List<Move> options = game.legalMovesFrom(from);
            if (!options.isEmpty()) {
                StringBuilder sb = new StringBuilder("      Destinos legales desde " + from + ": ");
                for (Move m : options) sb.append(m.to()).append(" ");
                System.out.println(sb.toString().trim());
            }
            return;
        }

        if (move.isPromotion()) {
            move.setPromotionType(askPromotion(scanner));
        }

        game.commitMove(move);
    }

    /** Pregunta a qué pieza coronar el peón. */
    private static PieceType askPromotion(Scanner scanner) {
        while (true) {
            System.out.print("  Coronaci\u00f3n. Elige pieza (Q=dama, R=torre, B=alfil, N=caballo): ");
            if (!scanner.hasNextLine()) return PieceType.QUEEN;
            String choice = scanner.nextLine().trim().toUpperCase();
            switch (choice) {
                case "Q": return PieceType.QUEEN;
                case "R": return PieceType.ROOK;
                case "B": return PieceType.BISHOP;
                case "N": return PieceType.KNIGHT;
                default:
                    System.out.println("  [!] Opci\u00f3n inv\u00e1lida. Escribe Q, R, B o N.");
            }
        }
    }

    private static void printAllMoves(Game game, Color turn) {
        List<Move> moves = game.legalMoves(turn);
        System.out.println("  Jugadas legales disponibles (" + moves.size() + "):");
        StringBuilder sb = new StringBuilder("    ");
        for (Move m : moves) sb.append(m).append("  ");
        System.out.println(sb.toString().stripTrailing());
    }

    private static void printWelcome() {
        System.out.println("====================================================");
        System.out.println("            AJEDREZ EN TERMINAL (Java)");
        System.out.println("   Patrones: Abstract Factory, Strategy, State,");
        System.out.println("             Observer y Prototype");
        System.out.println("====================================================");
        printHelp();
    }

    private static void printHelp() {
        System.out.println("\n  Cómo jugar:");
        System.out.println("   - Escribe el origen y el destino: por ejemplo  e2 e4");
        System.out.println("   - Enroque: mueve el REY dos casillas (ej. e1 g1 = enroque corto).");
        System.out.println("   - Captura al paso y coronación se detectan solas.");
        System.out.println("   - Comandos:  'mov' (ver jugadas legales)  'ayuda'  'salir'");
        System.out.println("   - El tablero se dibuja desde la perspectiva del jugador en turno.\n");
    }
}
