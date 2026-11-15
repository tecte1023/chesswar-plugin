package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.Messages;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.View;
import dev.tecte.chesswar.piece.PieceComponent;
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
    public static GameView create(final SimulationEventBus eventBus) {
        var view = new GameView();

        eventBus.subscribe(TurnStartEvent.class, view::onTurnStart);
        eventBus.subscribe(GameStopEvent.class, view::onGameStop);

        return view;
    }

    private void onTurnStart(final TurnStartEvent event) {
        Entity piece = event.piece();
        Team team = piece.component(PieceComponent.class).team();
        UUID playerId = piece.component(PlayerComponent.class).playerId();

        String teamName = team == Team.WHITE ? "백" : "흑";
        String playerName = resolvePlayerName(playerId);
        Component message = Component.text("[" + teamName + "] " + playerName + "님의 턴입니다.", NamedTextColor.YELLOW);

        Messages.reply(Bukkit.getServer(), message);
    }

    private void onGameStop(final GameStopEvent event) {
        Messages.reply(Bukkit.getServer(), Component.text("게임이 종료되었습니다.", NamedTextColor.YELLOW));
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
