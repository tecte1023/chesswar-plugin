package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Board;
import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.team.TeamRosterComponent;
import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class StartingPhaseManager implements GamePhaseChangeListener {
    @NotNull
    private final Plugin plugin;

    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final TeamRosterComponent teamRosterComponent;

    @NotNull
    private final BoardComponent boardComponent;

    @NotNull
    private final StartingPhasePresenter presenter;

    @Override
    public void onPhaseChanged(@NotNull final GamePhase newPhase) {
        if (newPhase != GamePhase.STARTING) {
            return;
        }

        startCountdown();
    }

    private void startCountdown() {
        new BukkitRunnable() {
            private int count = 3;

            @Override
            public void run() {
                if (phaseComponent.phase() != GamePhase.STARTING) {
                    cancel();
                    return;
                }

                if (count > 0) {
                    notifyCountdown(count);
                    count--;
                } else {
                    executeGameStart();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void notifyCountdown(final int secondsLeft) {
        final UUID[][] rosters = teamRosterComponent.teamRosters();

        for (final UUID[] team : rosters) {
            for (final UUID memberId : team) {
                final Player player = Bukkit.getPlayer(memberId);

                if (player == null) {
                    continue;
                }

                presenter.showCountdown(player, secondsLeft);
            }
        }
    }

    private void executeGameStart() {
        final Board board = boardComponent.board();

        Objects.requireNonNull(board, "StartingPhaseManager: Board is null on executeGameStart");
        phaseComponent.phase(phaseComponent.phase().next());

        final UUID[][] rosters = teamRosterComponent.teamRosters();

        for (final TeamSide teamSide : TeamSide.values()) {
            final UUID[] team = rosters[teamSide.ordinal()];
            final Location spawnLocation = board.getBarracks(teamSide).spawnLocation();

            for (final UUID memberId : team) {
                final Player player = Bukkit.getPlayer(memberId);

                if (player == null) {
                    continue;
                }

                player.teleport(spawnLocation);
                presenter.showGameStartFeedback(player);
            }
        }

        // TODO: 벙커에 미리 소환된 기물들을 각 진영으로 텔레포트 배치 (PieceManager 위임)
    }
}
