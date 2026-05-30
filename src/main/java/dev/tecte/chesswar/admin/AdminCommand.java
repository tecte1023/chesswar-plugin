package dev.tecte.chesswar.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.CombatManager;
import dev.tecte.chesswar.game.manager.EnvironmentManager;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandAlias("chesswaradmin|cwa")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public class AdminCommand extends BaseCommand {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final MoveValidator moveValidator;
    private final CombatManager combatManager;
    private final EnvironmentManager environmentManager;

    @Subcommand("move")
    public void onMove(Player player, int x, int y) {
        combatManager.handleMove(player);
    }

    @Subcommand("setup")
    public void onSetupBoard(Player player) {
        if (gameManager.phase() != GamePhase.WAITING) {
            player.sendMessage(Component.text("대기 단계에서만 체스판을 설정할 수 있습니다!", NamedTextColor.RED));
            return;
        }

        BlockFace forward = getCardinalDirection(player);
        ChessBoard board = new ChessBoard(player.getLocation(), forward, 3);

        boardManager.currentBoard(board);
        environmentManager.optimize(board.origin().getWorld());

        player.sendMessage(Component.text("체스판이 설정되었습니다! (3x3 배율)", NamedTextColor.GREEN));
        player.sendMessage(Component.text("기준점: " + formatLocation(board.origin()), NamedTextColor.GRAY));
        player.sendMessage(Component.text(
                "방향: %s | 칸 크기: %s".formatted(board.forward(), board.cellSize()),
                NamedTextColor.GRAY
        ));
        visualizeBoard(player, board);
    }

    @Subcommand("join")
    public void onJoin(Player player, Team team) {
        gameManager.join(player, team);
        player.sendMessage(Component.text("[Admin] " + team.teamName() + "에 강제 참가했습니다!", team.color()));
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

    private String formatLocation(Location location) {
        return String.format("%.0f, %.0f, %.0f", location.getX(), location.getY(), location.getZ());
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

            private void spawnParticle(Location location) {
                player.spawnParticle(
                        Particle.DUST,
                        location.add(0.5, 0.1, 0.5),
                        1,
                        new Particle.DustOptions(Color.GREEN, 1.0f)
                );
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
