package dev.tecte.chesswar.game;

import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.core.ChessWarCommand;
import dev.tecte.chesswar.core.Messages;
import dev.tecte.chesswar.piece.PieceLifecycle;
import dev.tecte.chesswar.piece.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@Subcommand("game")
@RequiredArgsConstructor
public final class GameCommand extends ChessWarCommand {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;
    private final GameLifecycle gameLifecycle;
    private final TeamRegistry teamRegistry;

    @Subcommand("start")
    public void onStart(final CommandSender sender) {
        if (!board.isSetup()) {
            Messages.reply(sender, Component.text("체스판 기준점이 설정되지 않았습니다.", NamedTextColor.RED));
            return;
        }

        GameStartResult result = gameLifecycle.start(pieceLifecycle.playerPieces());
        Component message = switch (result) {
            case SUCCESS -> Component.text("게임이 시작되었습니다.", NamedTextColor.GREEN);
            case ALREADY_STARTED -> Component.text("이미 게임이 진행 중입니다.", NamedTextColor.RED);
            case NO_PLAYERS -> Component.text("기물에 배정된 플레이어가 없습니다.", NamedTextColor.RED);
        };
        Messages.reply(sender, message);
    }

    @Subcommand("stop")
    public void onStop(final CommandSender sender) {
        if (!gameLifecycle.isRunning()) {
            Messages.reply(sender, Component.text("진행 중인 게임이 없습니다.", NamedTextColor.RED));
            return;
        }

        gameLifecycle.stop();
    }

    @Subcommand("teams")
    public void onTeams(final CommandSender sender) {
        Messages.reply(sender, formatTeamLine(Team.WHITE));
        Messages.reply(sender, formatTeamLine(Team.BLACK));
    }

    private Component formatTeamLine(final Team team) {
        Set<UUID> members = teamRegistry.membersOf(team);
        String teamName = team == Team.WHITE ? "백" : "흑";
        String header = teamName + " (" + members.size() + "/" + TeamRegistry.MAX_TEAM_SIZE + "): ";

        if (members.isEmpty()) {
            return Component.text(header + "없음", NamedTextColor.GRAY);
        }

        StringBuilder names = new StringBuilder();
        for (UUID playerId : members) {
            if (!names.isEmpty()) {
                names.append(", ");
            }

            names.append(resolvePlayerName(playerId));
        }

        return Component.text(header + names, NamedTextColor.AQUA);
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
