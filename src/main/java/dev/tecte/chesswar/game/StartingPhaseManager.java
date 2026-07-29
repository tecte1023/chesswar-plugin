package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Board;
import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.InitialLayoutPolicy;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.TeamRosterComponent;
import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

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
    private final PieceManager pieceManager;

    @NotNull
    private final StartingPhasePresenter presenter;

    @NotNull
    private final InternalEventBus internalEventBus;

    @Override
    public void onPhaseChanged(@NotNull final GamePhase newPhase) {
        if (newPhase != GamePhase.STARTING) {
            return;
        }

        spawnPiecesAsync();
        startCountdown();
    }

    private void spawnPiecesAsync() {
        final Board board = boardComponent.board();

        new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (phaseComponent.phase() != GamePhase.STARTING) {
                    cancel();
                    return;
                }

                int spawnedThisTick = 0;

                while (index < Coordinate.SQUARE_COUNT) {
                    final var coordinate = Coordinate.fromFlatIndex(index);
                    final PieceType type = InitialLayoutPolicy.initialPieceType(coordinate);
                    final TeamSide teamSide = InitialLayoutPolicy.teamSide(coordinate);

                    index++;

                    if (type == null || teamSide == null) {
                        continue;
                    }

                    final Location spawnLocation = board.getCenterAt(coordinate);
                    final float yaw = board.grid().anchor().getYaw() + teamSide.yawOffset();

                    spawnLocation.setYaw(yaw);
                    pieceManager.forceSpawnPiece(coordinate, type, teamSide, spawnLocation);
                    spawnedThisTick++;

                    if (spawnedThisTick >= 2) {
                        return;
                    }
                }

                cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
                    notifyGameStart();
                    applyGameStart();
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

    private void notifyGameStart() {
        final Board board = boardComponent.board();
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
    }

    private void applyGameStart() {
        final GamePhase nextPhase = phaseComponent.phase().next();

        phaseComponent.phase(nextPhase);
        internalEventBus.publishPhaseChange(nextPhase);
    }
}
