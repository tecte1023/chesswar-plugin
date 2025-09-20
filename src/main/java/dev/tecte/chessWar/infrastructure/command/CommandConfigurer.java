package dev.tecte.chessWar.infrastructure.command;

import co.aikar.commands.PaperCommandManager;
import lombok.NonNull;

/**
 * ACF({@link PaperCommandManager})에 특정한 설정을 적용하기 위한 함수형 인터페이스입니다.
 * 각 도메인 모듈은 이 인터페이스를 구현하여 자신의 커맨드 컨텍스트, 자동 완성 등을 등록할 수 있습니다.
 */
@FunctionalInterface
public interface CommandConfigurer {
    /**
     * 주어진 {@link PaperCommandManager}에 필요한 설정을 적용합니다.
     *
     * @param commandManager 설정할 커맨드 매니저
     */
    void configure(@NonNull PaperCommandManager commandManager);
}
