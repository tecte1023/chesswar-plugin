package dev.tecte.chessWar.infrastructure.bukkit;

import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bukkit 기반으로 보스바의 표시를 관리합니다.
 */
@Singleton
public class BukkitBossBarManager {
    private final UserResolver userResolver;
    private final BossBar bossBar;

    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    @Inject
    public BukkitBossBarManager(@NonNull UserResolver userResolver) {
        this.userResolver = userResolver;
        this.bossBar = BossBar.bossBar(
                Component.empty(),
                BossBar.MAX_PROGRESS,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );
    }

    /**
     * 보스바를 보여줍니다.
     *
     * @param playerIds 플레이어 ID 목록
     */
    public void show(@NonNull Collection<UUID> playerIds) {
        playerIds.forEach(this::show);
    }

    /**
     * 보스바를 보여줍니다.
     *
     * @param playerId 플레이어 ID
     */
    public void show(@NonNull UUID playerId) {
        Optional<Player> player = userResolver.findPlayer(playerId);

        if (player.isEmpty()) {
            return;
        }

        show(player.get());
    }

    /**
     * 보스바를 보여줍니다.
     *
     * @param player 플레이어
     */
    public void show(@NonNull Player player) {
        viewers.add(player.getUniqueId());
        player.showBossBar(bossBar);
    }

    /**
     * 시각적 상태를 업데이트합니다.
     *
     * @param title    제목
     * @param progress 진행률
     */
    public void update(@NonNull Component title, double progress) {
        float clampedProgress = (float) Math.clamp(progress, BossBar.MIN_PROGRESS, BossBar.MAX_PROGRESS);

        bossBar.name(title);
        bossBar.progress(clampedProgress);
        bossBar.color(determineColor(clampedProgress));
    }

    /**
     * 보스바를 숨깁니다.
     *
     * @param playerId 플레이어 ID
     */
    public void hide(@NonNull UUID playerId) {
        hideUI(playerId);
        viewers.remove(playerId);
    }

    /**
     * 보스바를 숨깁니다.
     */
    public void hide() {
        viewers.forEach(this::hideUI);
        viewers.clear();
    }

    private void hideUI(@NonNull UUID id) {
        userResolver.findPlayer(id)
                .ifPresent(player -> player.hideBossBar(bossBar));
    }

    @NonNull
    private BossBar.Color determineColor(float progress) {
        if (progress > 0.5f) {
            return BossBar.Color.GREEN;
        } else if (progress > 0.2f) {
            return BossBar.Color.YELLOW;
        } else {
            return BossBar.Color.RED;
        }
    }
}
