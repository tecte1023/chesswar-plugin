package dev.tecte.chessWar.infrastructure.config;

/**
 * 설정을 갱신하는 역할을 정의하는 함수형 인터페이스입니다.
 * 각 모듈은 이 인터페이스를 구현하여 자신의 설정을 {@link PluginConfig}에 통합할 수 있습니다.
 */
@FunctionalInterface
public interface ConfigUpdater {
    /**
     * 현재 정적 설정 객체를 받아, 자신의 모듈 설정을 추가하거나 수정한 새로운 정적 설정 객체를 반환합니다.
     *
     * @param currentConfig 현재까지 누적된 정적 설정 객체
     * @return 갱신된 정적 설정 객체
     */
    PluginConfig update(PluginConfig currentConfig);
}
