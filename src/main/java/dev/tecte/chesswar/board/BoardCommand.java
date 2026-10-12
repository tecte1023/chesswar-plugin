package dev.tecte.chesswar.board;

import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.core.ChessWarCommand;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Subcommand("board")
@RequiredArgsConstructor
public final class BoardCommand extends ChessWarCommand {
    private final BoardContext board;

    @Subcommand("setup")
    public void onSetup(final Player player) {
        Location loc = player.getLocation();
        board.setup(player.getWorld().getUID(), loc.toVector(), loc.getYaw());
        reply(player, Component.text("보드 기준 좌표가 현재 위치로 설정되었습니다.", NamedTextColor.GREEN));
    }
}
