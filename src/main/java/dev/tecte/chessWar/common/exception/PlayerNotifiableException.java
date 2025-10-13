package dev.tecte.chessWar.common.exception;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * 플레이어에게 메시지를 전송해야 하는 비즈니스 로직상의 예외 상황을 나타내는 최상위 예외 클래스입니다.
 * 이 예외를 상속받는 모든 예외는 {@link PlayerNotificationHandler}에 의해 자동으로 처리되어,
 * 예외 메시지가 해당 플레이어에게 전송됩니다.
 */
@Getter
public class PlayerNotifiableException extends RuntimeException {
    protected UUID playerId;

    /**
     * @param message  플레이어에게 전송할 메시지
     * @param playerId 메시지를 수신할 플레이어의 UUID
     */
    public PlayerNotifiableException(@NonNull String message, @NonNull UUID playerId) {
        super(message);
        this.playerId = playerId;
    }
}
