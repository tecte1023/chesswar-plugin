package dev.tecte.chessWar.game.application.port;

import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * 게임 타이머의 표시를 관리합니다.
 */
public interface GameTimerDisplay {
    /**
     * 타이머를 보여줍니다.
     *
     * @param targetIds 플레이어 ID 목록
     * @param title     제목
     * @param progress  진행률
     */
    void show(
            @NonNull Collection<UUID> targetIds,
            @NonNull Component title,
            double progress
    );

    /**
     * 타이머를 보여줍니다.
     *
     * @param playerId 플레이어 ID
     */
    void show(@NonNull UUID playerId);

    /**
     * 타이머 상태를 업데이트합니다.
     *
     * @param title    제목
     * @param progress 진행률
     */
    void update(@NonNull Component title, double progress);

    /**
     * 타이머를 숨깁니다.
     */
    void hide();
}
