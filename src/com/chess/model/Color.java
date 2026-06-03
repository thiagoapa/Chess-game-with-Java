package com.chess.model;

/**
 * Representa el bando de cada pieza.
 * Guarda la dirección de avance de los peones (+1 fila para blancas, -1 para negras)
 * y la fila de coronación, para no repetir esa lógica por todos lados.
 */
public enum Color {
    WHITE(1, 7, 1),   // avanza hacia filas mayores; corona en la fila 7; peones inician en fila 1
    BLACK(-1, 0, 6);  // avanza hacia filas menores; corona en la fila 0; peones inician en fila 6

    private final int direction;
    private final int promotionRow;
    private final int pawnStartRow;

    Color(int direction, int promotionRow, int pawnStartRow) {
        this.direction = direction;
        this.promotionRow = promotionRow;
        this.pawnStartRow = pawnStartRow;
    }

    public int direction()     { return direction; }
    public int promotionRow()  { return promotionRow; }
    public int pawnStartRow()  { return pawnStartRow; }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    public String displayName() {
        return this == WHITE ? "Blancas" : "Negras";
    }
}
