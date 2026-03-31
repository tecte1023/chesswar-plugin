package dev.tecte.chessWar.common.event;

import lombok.NonNull;

import java.util.UUID;

/**
 * 알림이 필요한 사건의 행위자 정보를 정의합니다.
 */
public interface NotifiableEvent {
    /**
     * 사건을 일으킨 행위자 ID를 제공합니다.
     *
     * @return 행위자 ID
     */
    @NonNull
    UUID senderId();
}
