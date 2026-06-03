package com.chess.strategy;

/** Movimiento de la dama: combina rectas y diagonales. */
public class QueenMovement extends SlidingMovement {
    @Override
    protected int[][] directions() {
        return new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
    }
}
