package dev.tecte.chesswar.board;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.Participant;
import dev.tecte.chesswar.game.Statistics;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BoardCommand extends BaseCommand {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final TimerManager timerManager;
    private final MoveValidator moveValidator;

    @Subcommand("join")
    public void onJoin(Player player, Team team) {
        gameManager.join(player, team);
        player.sendMessage(Component.text(team.displayName() + "에 참가했습니다!", team.textColor()));
    }

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

    @Subcommand("record")
    public void onRecord(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        org.bukkit.inventory.meta.BookMeta meta = (org.bukkit.inventory.meta.BookMeta) book.getItemMeta();

        meta.setTitle("전투 기록 일지");
        meta.setAuthor("ChessWar System");

        Component content = Component.text()
                .append(Component.text("=== 전투 기록 리포트 ===\n\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .build();

        for (Participant p : gameManager.participants().values()) {
            Statistics s = gameManager.getStats(p.playerId());
            Player participantPlayer = Bukkit.getPlayer(p.playerId());
            String name = (participantPlayer != null) ? participantPlayer.getName() : "오프라인";

            content = content.append(Component.text()
                    .append(Component.text("-" + name + " (" + p.team().displayName() + ")\n", p.team().textColor()))
                    .append(Component.text("  가한 피해: " + (int) s.getDamageDealt() + "\n", NamedTextColor.DARK_GRAY))
                    .append(Component.text("  받은 피해: " + (int) s.getDamageTaken() + "\n", NamedTextColor.DARK_GRAY))
                    .append(Component.text("  킬/데스: " + s.getKills() + "/" + s.getDeaths() + "\n\n", NamedTextColor.DARK_GRAY))
                    .build());
        }

        meta.addPages(content);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        player.sendMessage(Component.text("전투 기록 일지를 지급했습니다.", NamedTextColor.GREEN));
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
        player.sendMessage(Component.text("체스판이 설정되었습니다! (3x3 배율)", NamedTextColor.GREEN));
        player.sendMessage(Component.text("기준점: " + formatLocation(board.origin()), NamedTextColor.GRAY));
        player.sendMessage(Component.text(
                "방향: %s | 칸 크기: %s".formatted(board.forward(), board.cellSize()),
                NamedTextColor.GRAY
        ));
        visualizeBoard(player, board);
    }

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

    @Subcommand("dev move")
    public void onMove(Player player, int x, int y) {
        if (gameManager.phase() != GamePhase.BATTLE) {
            player.sendMessage(Component.text("전투 단계가 아닙니다!", NamedTextColor.RED));
            return;
        }

        Optional<UUID> currentTurnId = gameManager.currentTurnPlayer();

        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        Coordinate to = Coordinate.of(x, y);

        if (!to.isValid()) {
            player.sendMessage(Component.text("유효하지 않은 좌표입니다!", NamedTextColor.RED));
            return;
        }

        Coordinate from = null;

        for (Map.Entry<Coordinate, Piece> entry : pieceManager.boardPieces().entrySet()) {
            if (player.getUniqueId().equals(entry.getValue().ownerId())) {
                from = entry.getKey();
                break;
            }
        }

        if (from == null) {
            player.sendMessage(Component.text("보드 위에 당신의 기물이 없습니다!", NamedTextColor.RED));
            return;
        }

        if (!moveValidator.canMove(from, to)) {
            player.sendMessage(Component.text("그곳으로는 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        Piece myPiece = pieceManager.boardPieces().get(from);
        Optional<Piece> targetPiece = pieceManager.findPieceAt(to);

        if (targetPiece.isEmpty()) {
            pieceManager.removePiece(from);
            pieceManager.placePiece(to, myPiece);
            player.teleport(boardManager.currentBoard().toCenterLocation(to).add(0, 1, 0));
            player.sendMessage(Component.text(x + ", " + y + " 좌표로 이동했습니다.", NamedTextColor.GREEN));
            finishTurn(player);
        } else {
            Piece target = targetPiece.get();

            if (target.team() == myPiece.team()) {
                player.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
                return;
            }

            double damage = myPiece.type().baseDamage();

            target.currentHealth(target.currentHealth() - damage);
            player.sendMessage(Component.text(
                    "%s에게 %s의 피해를 입혔습니다!".formatted(target.type().displayName(), damage),
                    NamedTextColor.GOLD
            ));

            if (target.currentHealth() <= 0) {
                player.sendMessage(Component.text(
                        "%s 기물을 처치했습니다!".formatted(target.type().displayName()),
                        NamedTextColor.AQUA
                ));
                pieceManager.removePiece(from);
                pieceManager.placePiece(to, myPiece);
                player.teleport(boardManager.currentBoard().toCenterLocation(to).add(0, 1, 0));

                if (target.type() == PieceType.KING) {
                    gameManager.win(plugin, boardManager, pieceManager, timerManager, myPiece.team());
                    return;
                }
            } else {
                player.sendMessage(Component.text(
                        "상대가 아직 살아있어 제자리에 유지됩니다. (남은 체력: %s)".formatted(target.currentHealth()),
                        NamedTextColor.YELLOW
                ));
            }

            finishTurn(player);
        }
    }

    @Subcommand("reset")
    public void onReset(Player player) {
        gameManager.reset(pieceManager, boardManager);
        timerManager.stopTimer();
        player.sendMessage(Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN));
    }

    private void finishTurn(Player player) {
        gameManager.nextTurn(pieceManager);
        timerManager.startTurnTimer(30);
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
