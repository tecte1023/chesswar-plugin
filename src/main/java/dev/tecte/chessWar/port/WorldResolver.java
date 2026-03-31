package dev.tecte.chessWar.port;

import lombok.NonNull;
import org.bukkit.World;

import java.util.function.Function;

/**
 * 시스템 내 월드를 식별하고 찾습니다.
 */
public interface WorldResolver {
    /**
     * 월드의 존재 여부를 검증합니다.
     *
     * @param name      월드 이름
     * @param onFailure 검증 실패 시 예외 팩토리
     * @param <E>       예외 타입
     */
    <E extends RuntimeException> void ensureExists(
            @NonNull String name,
            @NonNull Function<String, E> onFailure
    );

    /**
     * 월드를 제공합니다.
     *
     * @param name      월드 이름
     * @param onFailure 실패 시 예외 팩토리
     * @param <E>       예외 타입
     * @return 월드
     */
    @NonNull
    <E extends RuntimeException> World resolve(
            @NonNull String name,
            @NonNull Function<String, E> onFailure
    );
}
