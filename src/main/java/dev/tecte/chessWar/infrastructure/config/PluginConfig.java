package dev.tecte.chessWar.infrastructure.config;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import lombok.NonNull;
import lombok.With;

@With
public record PluginConfig(BoardConfig boardConfig) {
    @NonNull
    public static PluginConfig empty() {
        return new PluginConfig(null);
    }
}
