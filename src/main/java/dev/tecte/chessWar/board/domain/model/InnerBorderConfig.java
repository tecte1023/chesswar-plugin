package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

public record InnerBorderConfig(int thickness, Material block) {
    public static final int MIN_THICKNESS = 0;
    public static final int MAX_THICKNESS = 3;

    public static final int DEFAULT_THICKNESS = 1;
    public static final Material DEFAULT_BLOCK = Material.STRIPPED_OAK_WOOD;
    private static final InnerBorderConfig DEFAULT = new InnerBorderConfig(DEFAULT_THICKNESS, DEFAULT_BLOCK);

    public InnerBorderConfig {
        Objects.requireNonNull(block, "block must not be null");

        if (!isValidThickness(thickness)) {
            throw new IllegalArgumentException("Invalid inner border thickness: " + thickness + ". Value must be between " + MIN_THICKNESS + " and " + MAX_THICKNESS + ".");
        }

        if (!block.isBlock()) {
            throw new IllegalArgumentException("Inner border block must be a placeable block, but was " + block + ".");
        }
    }

    @NonNull
    public static InnerBorderConfig ofDefault() {
        return DEFAULT;
    }

    public static boolean isValidThickness(int thickness) {
        return thickness >= MIN_THICKNESS && thickness <= MAX_THICKNESS;
    }
}
