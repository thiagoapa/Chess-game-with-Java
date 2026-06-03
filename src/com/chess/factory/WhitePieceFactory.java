package com.chess.factory;

import com.chess.model.Color;

/** Fábrica concreta del ejército blanco. */
public class WhitePieceFactory extends AbstractPieceFactory {
    @Override
    protected Color color() {
        return Color.WHITE;
    }
}
