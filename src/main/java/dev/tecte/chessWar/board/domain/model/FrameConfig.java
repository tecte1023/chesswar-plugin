package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

public record FrameConfig(int thickness, Material block) {
    public static final int MIN_THICKNESS = 0;
    public static final int MAX_THICKNESS = 6;

    public static final int DEFAULT_THICKNESS = 2;
    public static final Material DEFAULT_BLOCK = Material.DARK_OAK_WOOD;
    private static final FrameConfig DEFAULT = new FrameConfig(DEFAULT_THICKNESS, DEFAULT_BLOCK);

    public FrameConfig {
        Objects.requireNonNull(block, "block must not be null");

        if (!isValidThickness(thickness)) {
            throw new IllegalArgumentException("Invalid frame thickness: " + thickness + ". Value must be between " + MIN_THICKNESS + " and " + MAX_THICKNESS + ".");
        }

        if (!block.isBlock()) {
            throw new IllegalArgumentException("Frame block must be a placeable block, but was " + block + ".");
        }
    }

    @NonNull
    public static FrameConfig ofDefault() {
        return DEFAULT;
    }

    public static boolean isValidThickness(int thickness) {
        return thickness >= MIN_THICKNESS && thickness <= MAX_THICKNESS;
    }
}
