package dev.tecte.chessWar.piece.infrastructure.mythicmobs.exception;

import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * MythicMobs 관련 작업 중 발생하는 런타임 예외입니다.
 * <p>
 * 주로 잘못된 mobId 매핑이나 MythicMobs 플러그인 연동 실패 시 발생하며,
 * 시스템 관리자에게 통지되어야 할 기술적 오류를 나타냅니다.
 */
public class MythicMobSpawnException extends RuntimeException implements Loggable, Notifiable {
    private final String mobId;

    private MythicMobSpawnException(String message, String mobId) {
        super(message);
        this.mobId = mobId;
    }

    /**
     * 정의된 MythicMob을 찾을 수 없을 때 예외를 생성합니다.
     *
     * @param mobId 찾을 수 없는 Mob ID
     * @return 생성된 예외
     */
    @NonNull
    public static MythicMobSpawnException notFound(@NonNull String mobId) {
        return new MythicMobSpawnException(
                String.format("MythicMob definition not found for ID: '%s'", mobId),
                mobId
        );
    }

    /**
     * MythicMob 스폰 결과가 null일 때(스폰 실패) 예외를 생성합니다.
     *
     * @param mobId 스폰에 실패한 Mob ID
     * @return 생성된 예외
     */
    @NonNull
    public static MythicMobSpawnException spawnFailed(@NonNull String mobId) {
        return new MythicMobSpawnException(
                String.format("Failed to spawn MythicMob entity (result was null) for ID: '%s'", mobId),
                mobId
        );
    }

    @NonNull
    @Override
    public Component getNotificationComponent() {
        return Component.text("시스템 오류: 기물 데이터('")
                .append(Component.text(mobId))
                .append(Component.text("') 로드 실패. 관리자에게 문의하세요."));
    }
}
