package dev.tecte.chessWar.piece.infrastructure.mythicmobs.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;

/**
 * MythicMobs 연동 과정에서 발생하는 인프라 계층의 시스템 예외입니다.
 * <p>
 * 잘못된 템플릿 ID 매핑이나 플러그인 오류 등 기술적인 문제로 발생하며,
 * 시스템 로그는 기록하지만 사용자에게 알림 메시지는 전달하지 않습니다.
 */
public class MythicMobSpawnException extends SystemException {
    private MythicMobSpawnException(@NonNull String internalMessage) {
        super(internalMessage);
    }

    /**
     * MythicMobs 설정에서 해당 ID의 템플릿을 찾을 수 없을 때 발생합니다.
     *
     * @param mobId 찾을 수 없는 Mob ID
     * @return 생성된 예외
     */
    @NonNull
    public static MythicMobSpawnException notFound(@NonNull String mobId) {
        return new MythicMobSpawnException("MythicMob template not found [MobID: %s]".formatted(mobId));
    }

    /**
     * 플러그인 오류로 인해 엔티티 소환 결과가 null일 때 발생합니다.
     *
     * @param mobId 소환에 실패한 Mob ID
     * @return 생성된 예외
     */
    @NonNull
    public static MythicMobSpawnException spawnFailed(@NonNull String mobId) {
        return new MythicMobSpawnException(
                "Failed to spawn MythicMob [MobID: %s, Reason: Plugin returned null entity]".formatted(mobId)
        );
    }
}
