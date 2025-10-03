package dev.tecte.chessWar.common.notifier;

import java.util.UUID;

/**
 * 플레이어에게 알림을 보내는 기능에 대한 공용 아웃고잉 포트입니다.
 * 이 인터페이스는 특정 도메인에 종속되지 않으며, 모든 계층에서 의존할 수 있는 공용 계약입니다.
 */
public interface PlayerNotifier {

    /**
     * 성공 메시지를 플레이어에게 알립니다.
     *
     * @param playerId 알림을 받을 플레이어의 UUID
     * @param message  보낼 메시지
     */
    void notifySuccess(UUID playerId, String message);

    /**
     * 오류 메시지를 플레이어에게 알립니다.
     *
     * @param playerId 알림을 받을 플레이어의 UUID
     * @param message  보낼 메시지
     */
    void notifyError(UUID playerId, String message);
}
