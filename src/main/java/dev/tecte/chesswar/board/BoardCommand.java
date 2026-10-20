package dev.tecte.chesswar.board;

import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.core.ChessWarCommand;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

@Subcommand("board")
@RequiredArgsConstructor
public final class BoardCommand extends ChessWarCommand {
    private final BoardContext board;

    @Subcommand("setup")
    public void onSetup(final Player player) {
        board.setup(player.getLocation());
        reply(player, Component.text("체스판 기준점이 현재 위치로 설정되었습니다.", NamedTextColor.GREEN));
    }
}
