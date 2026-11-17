package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.Messages;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.View;
import dev.tecte.chesswar.piece.PieceComponent;
import dev.tecte.chesswar.piece.PieceDeathEvent;
import dev.tecte.chesswar.piece.PlayerComponent;
import dev.tecte.chesswar.piece.Team;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameView implements View {
    private final GameLifecycle gameLifecycle;

    public static GameView create(final GameLifecycle gameLifecycle, final SimulationEventBus eventBus) {
        var view = new GameView(gameLifecycle);

        eventBus.subscribe(PieceDeathEvent.class, view::onPieceDeath);
        eventBus.subscribe(TurnStartEvent.class, view::onTurnStart);
        eventBus.subscribe(TurnTimeoutEvent.class, view::onTurnTimeout);
        eventBus.subscribe(GameWinEvent.class, view::onGameWin);
        eventBus.subscribe(GameStopEvent.class, view::onGameStop);

        return view;
    }

    private void onPieceDeath(final PieceDeathEvent event) {
        Entity piece = event.piece();
        if (!piece.hasComponent(PlayerComponent.class)) {
            return;
        }

        Team team = piece.component(PieceComponent.class).team();
        UUID playerId = piece.component(PlayerComponent.class).playerId();
        String actorName = formatActorName(team, playerId);
        Component message = Component.text(
                actorName + "님이 사망하여 관전 모드로 전환됩니다.",
                NamedTextColor.GRAY
        );

        Messages.reply(Bukkit.getServer(), message);
    }

    private void onTurnStart(final TurnStartEvent event) {
        Entity piece = event.piece();
        Team team = piece.component(PieceComponent.class).team();
        UUID playerId = piece.component(PlayerComponent.class).playerId();

        String actorName = formatActorName(team, playerId);
        String timerText = formatTimerText(gameLifecycle.remainingTeamTicks(team));
        Component message = Component.text(actorName + "님의 턴입니다. (" + timerText + ")", NamedTextColor.YELLOW);

        Messages.reply(Bukkit.getServer(), message);
    }

    private void onTurnTimeout(final TurnTimeoutEvent event) {
        Entity piece = event.piece();
        Team team = piece.component(PieceComponent.class).team();
        UUID playerId = piece.component(PlayerComponent.class).playerId();

        String actorName = formatActorName(team, playerId);
        Component message = Component.text(
                actorName + "님의 제한 시간이 초과되어 턴이 넘어갑니다.",
                NamedTextColor.RED
        );

        Messages.reply(Bukkit.getServer(), message);
    }

    private void onGameWin(final GameWinEvent event) {
        String teamName = formatTeamName(event.winnerTeam());
        int endingSeconds = Math.ceilDiv(GameLifecycle.ENDING_TICKS, 20);
        Component message = Component.text(
                teamName + " 진영이 승리했습니다! " + endingSeconds + "초 후 필드가 초기화됩니다.",
                NamedTextColor.GOLD
        );

        Messages.reply(Bukkit.getServer(), message);
    }

    private void onGameStop(final GameStopEvent event) {
        Messages.reply(Bukkit.getServer(), Component.text("게임이 종료되고 필드가 초기화되었습니다.", NamedTextColor.YELLOW));
    }

    private String formatActorName(final Team team, final UUID playerId) {
        return "[" + formatTeamName(team) + "] " + resolvePlayerName(playerId);
    }

    private String formatTeamName(final Team team) {
        return team == Team.WHITE ? "백" : "흑";
    }

    private String formatTimerText(final int remainingTicks) {
        if (remainingTicks <= 0) {
            return Math.ceilDiv(GameLifecycle.OVERTIME_TICKS, 20) + "초 초읽기";
        }

        int totalSeconds = Math.ceilDiv(remainingTicks, 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("남은 시간: %02d:%02d", minutes, seconds);
    }

    private String resolvePlayerName(final UUID playerId) {
        Player onlinePlayer = Bukkit.getPlayer(playerId);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        String offlineName = Bukkit.getOfflinePlayer(playerId).getName();
        return offlineName != null ? offlineName : "알 수 없음";
    }
}
