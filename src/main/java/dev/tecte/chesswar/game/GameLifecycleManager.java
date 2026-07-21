package dev.tecte.chesswar.game;

import dev.tecte.chesswar.team.TeamRosterComponent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class GameLifecycleManager {
    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final TeamRosterComponent teamRosterComponent;

    @NotNull
    private final GameLifecyclePresenter presenter;

    @NotNull
    private final InternalEventBus internalEventBus;

    public void tryStopGame() {
        if (phaseComponent.phase() == GamePhase.WAITING) {
            return;
        }

        phaseComponent.phase(GamePhase.WAITING);
        internalEventBus.publishPhaseChange(GamePhase.WAITING);

        final UUID[][] rosters = teamRosterComponent.teamRosters();

        for (final UUID[] team : rosters) {
            for (final UUID memberId : team) {
                final Player player = Bukkit.getPlayer(memberId);

                if (player == null) {
                    continue;
                }

                presenter.showGameStopFeedback(player);
            }
        }

        // TODO: 향후 기물 삭제, 보드 초기화, 플레이어를 대기실로 텔레포트하는 등 전역 초기화 로직 추가
    }
}
