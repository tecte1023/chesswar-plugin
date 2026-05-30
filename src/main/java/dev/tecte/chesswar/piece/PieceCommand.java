package dev.tecte.chesswar.piece;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
public class PieceCommand extends BaseCommand {
    private final GameManager gameManager;
    private final TimerManager timerManager;

    @Subcommand("select")
    public void onSelectPiece(Player player, int x, int y) {
        if (gameManager.phase() != GamePhase.PIECE_SELECTION) {
            player.sendMessage(Component.text("기물 선택 단계가 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!gameManager.isParticipant(player)) {
            player.sendMessage(Component.text("먼저 팀에 참가해야 합니다!", NamedTextColor.RED));
            return;
        }

        Coordinate coordinate = Coordinate.of(x, y);
        gameManager.selectPiece(player, coordinate);

        if (gameManager.areAllPiecesSelected()) {
            timerManager.accelerateTo(10);
            Bukkit.broadcast(Component.text()
                    .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("모든 플레이어가 기물을 선택했습니다! 10초 후 준비 단계로 넘어갑니다.", NamedTextColor.AQUA, TextDecoration.BOLD))
                    .build());
        }
    }
}
