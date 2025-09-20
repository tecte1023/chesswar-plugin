package dev.tecte.chessWar.board.infrastructure.bootstrap;

import dev.tecte.chessWar.board.infrastructure.persistence.YmlBoardConfigRepository;
import dev.tecte.chessWar.infrastructure.config.ConfigUpdater;
import dev.tecte.chessWar.infrastructure.config.PluginConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 체스판 모듈의 설정을 {@link PluginConfig}에 통합하는 역할을 합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardConfigUpdater implements ConfigUpdater {
    private final YmlBoardConfigRepository repository;

    @NonNull
    @Override
    public PluginConfig update(@NonNull PluginConfig pluginConfig) {
        return pluginConfig.withBoardConfig(repository.getBoardConfig());
    }
}
