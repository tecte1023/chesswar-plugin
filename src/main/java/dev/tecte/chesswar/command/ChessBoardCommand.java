package dev.tecte.chesswar.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.Participant;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceType;
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

import java.util.Optional;
import java.util.UUID;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ChessBoardCommand extends BaseCommand {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final TimerManager timerManager;
    private final MoveValidator moveValidator;

    @Subcommand("join")
    public void onJoin(Player player, Team team) {
        gameManager.join(player, team);
        player.sendMessage(Component.text(team.displayName() + "에 참가했습니다!", team.textColor()));
    }

    @Subcommand("select")
    public void onSelectPiece(Player player, PieceType pieceType) {
        if (!gameManager.isParticipant(player)) {
            player.sendMessage(Component.text("먼저 팀에 참가해야 합니다!", NamedTextColor.RED));
            return;
        }

        gameManager.selectPiece(player, pieceType);
        player.sendMessage(Component.text(pieceType.displayName() + " 기물을 선택했습니다!", NamedTextColor.GOLD));
    }

    @Subcommand("setup")
    public void onSetupBoard(Player player) {
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

        gameManager.phase(GamePhase.BATTLE);
        gameManager.prepareTurnOrder();

        int whiteX = 0;
        int blackX = 0;

        for (Participant participant : gameManager.participants().values()) {
            Player onlinePlayer = player.getServer().getPlayer(participant.playerId());

            if (onlinePlayer == null || participant.pieceType() == null) {
                continue;
            }

            Coordinate startCoordinate = participant.team() == Team.WHITE
                    ? Coordinate.of(whiteX++, 0)
                    : Coordinate.of(blackX++, 7);
            Piece piece = Piece.of(participant.playerId(), participant.team(), participant.pieceType());
            Location spawnLocation = boardManager.currentBoard().toCenterLocation(startCoordinate).add(0, 1, 0);

            gameManager.placePiece(startCoordinate, piece);
            onlinePlayer.teleport(spawnLocation);
        }

        timerManager.startTurnTimer();
        gameManager.currentTurnPlayer().ifPresent(uuid -> {
            Player firstPlayer = player.getServer().getPlayer(uuid);
            if (firstPlayer != null) {
                player.getServer().getPluginManager().callEvent(new dev.tecte.chesswar.event.ChessTurnStartedEvent(firstPlayer));
            }
        });
        player.sendMessage(Component.text("게임을 시작합니다! 전장에 배치되었습니다.", NamedTextColor.GREEN));
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

        for (var entry : gameManager.boardPieces().entrySet()) {
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

        Piece myPiece = gameManager.boardPieces().get(from);
        Optional<Piece> targetPiece = gameManager.findPieceAt(to);

        if (targetPiece.isEmpty()) {
            gameManager.removePiece(from);
            gameManager.placePiece(to, myPiece);
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
                gameManager.removePiece(from);
                gameManager.placePiece(to, myPiece);
                player.teleport(boardManager.currentBoard().toCenterLocation(to).add(0, 1, 0));

                if (target.type() == PieceType.KING) {
                    gameManager.win(myPiece.team());
                    timerManager.stopTimer();
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
        gameManager.reset();
        timerManager.stopTimer();
        player.sendMessage(Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN));
    }

    private void finishTurn(Player player) {
        gameManager.nextTurn();
        timerManager.startTurnTimer();
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
