package dev.tecte.chesswar.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.ChessBoard;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ChessBoardCommand extends BaseCommand {
    private final ChessWar plugin;

    @Subcommand("setup")
    public void onSetupBoard(Player player) {
        BlockFace forward = getCardinalDirection(player);
        ChessBoard board = new ChessBoard(player.getLocation(), forward, 3);

        plugin.boardManager().currentBoard(board);
        player.sendMessage(Component.text("체스판이 설정되었습니다! (3x3 배율)", NamedTextColor.GREEN));
        player.sendMessage(Component.text("기준점: " + formatLocation(board.origin()), NamedTextColor.GRAY));
        player.sendMessage(Component.text("방향: " + board.forward() + " | 칸 크기: " + board.cellSize(), NamedTextColor.GRAY));

        visualizeBoard(player, board);
    }

    private BlockFace getCardinalDirection(Player player) {
        float yaw = (player.getLocation().getYaw() + 360) % 360;

        if (yaw <= 45 || yaw > 315) {
            return BlockFace.SOUTH;
        } else if (yaw > 45 && yaw <= 135) {
            return BlockFace.WEST;
        } else if (yaw > 135 && yaw <= 225) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }

    private void visualizeBoard(Player player, ChessBoard board) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 100) {
                    cancel();

                    return;
                }

                int physicalSize = 8 * board.cellSize();
                int maxOffset = physicalSize - 1;

                for (int i = 0; i < physicalSize; i++) {
                    spawnParticle(board.origin().clone()
                            .add(board.right().getDirection().multiply(i)));
                    spawnParticle(board.origin().clone()
                            .add(board.right().getDirection().multiply(i))
                            .add(board.forward().getDirection().multiply(maxOffset)));
                    spawnParticle(board.origin().clone()
                            .add(board.forward().getDirection().multiply(i)));
                    spawnParticle(board.origin().clone()
                            .add(board.forward().getDirection().multiply(i))
                            .add(board.right().getDirection().multiply(maxOffset)));
                }

                ticks += 5;
            }

            private void spawnParticle(Location loc) {
                player.spawnParticle(
                        Particle.DUST,
                        loc.add(0.5, 0.1, 0.5),
                        1,
                        new Particle.DustOptions(Color.GREEN, 1.0f)
                );
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
