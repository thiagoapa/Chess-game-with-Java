package com.chess.strategy;

/** Movimiento de la torre: rectas horizontales y verticales. */
public class RookMovement extends SlidingMovement {
    @Override
    protected int[][] directions() {
        return new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    }
}
