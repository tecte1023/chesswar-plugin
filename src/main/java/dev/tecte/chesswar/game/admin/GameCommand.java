package dev.tecte.chesswar.game.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

@CommandAlias("chesswar|cw")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class GameCommand extends BaseCommand {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final TimerManager timerManager;

    @Subcommand("start")
    public void onStart(Player player) {
        if (!boardManager.hasBoard()) {
            player.sendMessage(Component.text(
                    "체스판이 존재하지 않습니다. 체스판을 먼저 생성해 주세요.",
                    NamedTextColor.RED
            ));
            return;
        }

        if (gameManager.participants().isEmpty()) {
            player.sendMessage(Component.text("참가자가 없습니다.", NamedTextColor.RED));
            return;
        }

        gameManager.advancePhase();
    }

    @Subcommand("reset")
    public void onReset(Player player) {
        gameManager.reset();
        timerManager.stopHeartbeat();
        plugin.scoreboardManager().stopHeartbeat();
        timerManager.stopTimer();
        player.sendMessage(Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN));
    }
}
