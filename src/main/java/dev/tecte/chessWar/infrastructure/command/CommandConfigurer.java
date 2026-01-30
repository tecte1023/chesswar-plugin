package dev.tecte.chessWar.infrastructure.command;

import co.aikar.commands.PaperCommandManager;
import lombok.NonNull;

/**
 * 커맨드 매니저 설정을 위한 인터페이스입니다.
 */
@FunctionalInterface
public interface CommandConfigurer {
    /**
     * 커맨드 매니저에 설정을 적용합니다.
     *
     * @param commandManager 설정할 커맨드 매니저
     */
    void configure(@NonNull PaperCommandManager commandManager);
}
