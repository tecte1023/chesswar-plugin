package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.piece.PieceManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
public class GameCommand extends BaseCommand {
    private final JavaPlugin plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final TimerManager timerManager;

    @Subcommand("start")
    public void onStart(Player player) {
        if (gameManager.participants().isEmpty()) {
            player.sendMessage(Component.text("참가자가 없습니다!", NamedTextColor.RED));
            return;
        }

        if (!boardManager.hasBoard()) {
            player.sendMessage(Component.text("먼저 체스판을 설정해야 합니다!", NamedTextColor.RED));
            return;
        }

        gameManager.advancePhase(plugin, boardManager, pieceManager, timerManager);
    }

    @Subcommand("reset")
    public void onReset(Player player) {
        gameManager.reset(pieceManager, boardManager);
        timerManager.stopHeartbeat();
        if (plugin instanceof ChessWar cw) {
            cw.scoreboardManager().stopHeartbeat();
        }
        timerManager.stopTimer();
        player.sendMessage(Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN));
    }
}
