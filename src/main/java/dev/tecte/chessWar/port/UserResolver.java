package dev.tecte.chessWar.port;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * 시스템 행위자와 사용자를 식별하고 찾습니다.
 */
public interface UserResolver {
    /**
     * 행위자의 ID를 제공합니다.
     *
     * @param sender 행위자
     * @return 행위자 ID
     */
    @NonNull
    UUID resolveActorId(@NonNull CommandSender sender);

    /**
     * ID로 행위자를 제공합니다.
     *
     * @param actorId 행위자 ID
     * @return 행위자
     */
    @NonNull
    CommandSender resolveSender(@NonNull UUID actorId);

    /**
     * 플레이어를 찾습니다.
     *
     * @param playerId 플레이어 ID
     * @return 찾은 플레이어
     */
    @NonNull
    Optional<Player> findPlayer(@NonNull UUID playerId);
}
