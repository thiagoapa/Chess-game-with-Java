package com.chess.strategy;

/** Movimiento del alfil: las cuatro diagonales. */
public class BishopMovement extends SlidingMovement {
    @Override
    protected int[][] directions() {
        return new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    }
}
