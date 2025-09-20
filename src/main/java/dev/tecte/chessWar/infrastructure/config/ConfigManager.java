package dev.tecte.chessWar.infrastructure.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 플러그인의 정적 설정을 관리하는 클래스입니다.
 * 여러 {@link ConfigUpdater}를 통해 각 모듈의 설정을 취합하고, 최종 정적 설정 객체인 {@link PluginConfig}를 생성하여 제공합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ConfigManager {
    private final Set<ConfigUpdater> updaters;

    @Getter
    private volatile PluginConfig pluginConfig;

    /**
     * 등록된 모든 {@link ConfigUpdater}를 실행하여 정적 설정을 로드하고 갱신합니다.
     */
    public void load() {
        PluginConfig newConfig = PluginConfig.ofDefaults();

        for (ConfigUpdater updater : updaters) {
            newConfig = updater.update(newConfig);
        }

        pluginConfig = newConfig;
    }
}
