package dev.tecte.chessWar.infrastructure.command;

import co.aikar.commands.PaperCommandManager;
import lombok.NonNull;

@FunctionalInterface
public interface CommandConfigurer {
    void configure(@NonNull PaperCommandManager commandManager);
}
