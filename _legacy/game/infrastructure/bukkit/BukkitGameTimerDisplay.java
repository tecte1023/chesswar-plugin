package dev.tecte.chessWar.game.infrastructure.bukkit;

import dev.tecte.chessWar.game.application.port.GameTimerDisplay;
import dev.tecte.chessWar.infrastructure.bukkit.BukkitBossBarManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * 보스바를 활용하여 게임 타이머의 표시를 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitGameTimerDisplay implements GameTimerDisplay {
    private final BukkitBossBarManager bossBarManager;

    @Override
    public void show(@NonNull Collection<UUID> targetIds, @NonNull Component title, double progress) {
        bossBarManager.update(title, progress);
        bossBarManager.show(targetIds);
    }

    @Override
    public void show(@NonNull UUID playerId) {
        bossBarManager.show(playerId);
    }

    @Override
    public void update(@NonNull Component title, double progress) {
        bossBarManager.update(title, progress);
    }

    @Override
    public void hide() {
        bossBarManager.hide();
    }
}
