package com.chess.factory;

import com.chess.model.Color;

/** Fábrica concreta del ejército negro. */
public class BlackPieceFactory extends AbstractPieceFactory {
    @Override
    protected Color color() {
        return Color.BLACK;
    }
}
