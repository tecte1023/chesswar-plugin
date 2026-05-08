package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.common.identity.ProjectIdentity;
import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Bukkit 기반으로 사용자를 찾습니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitUserResolver implements UserResolver {
    private final ConsoleCommandSender console;

    @NonNull
    @Override
    public UUID resolveActorId(@NonNull CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getUniqueId();
        }

        return ProjectIdentity.SYSTEM_ID;
    }

    @NonNull
    @Override
    public CommandSender resolveSender(@NonNull UUID actorId) {
        if (actorId.equals(ProjectIdentity.SYSTEM_ID)) {
            return console;
        }

        return findPlayer(actorId)
                .map(player -> (CommandSender) player)
                .orElse(console);
    }

    @NonNull
    @Override
    public Optional<Player> findPlayer(@NonNull UUID playerId) {
        return Optional.ofNullable(Bukkit.getPlayer(playerId));
    }
}
