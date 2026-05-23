package dev.tecte.chesswar.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.event.ChessTurnStartedEvent;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.Participant;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
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
        setupBarracks(player, board);
    }

    private void setupBarracks(Player player, ChessBoard mainBoard) {
        int offsetDistance = 5 + (8 * mainBoard.cellSize());
        Location whiteOrigin = mainBoard.origin().clone()
                .subtract(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard whiteBarracks = new ChessBoard(whiteOrigin, mainBoard.forward(), mainBoard.cellSize());
        Location blackOrigin = mainBoard.origin().clone()
                .add(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard blackBarracks = new ChessBoard(blackOrigin, mainBoard.forward(), mainBoard.cellSize());

        visualizeBoard(player, whiteBarracks);
        visualizeBoard(player, blackBarracks);

        PieceType[] backRow = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        NamespacedKey typeKey = new NamespacedKey(plugin, "barracks_piece_type");
        NamespacedKey teamKey = new NamespacedKey(plugin, "barracks_piece_team");
        MobManager mobManager = MythicProvider.get().getMobManager();

        for (int x = 0; x < 8; x++) {
            PieceType type = backRow[x];

            spawnBarracksPiece(
                    whiteBarracks.toCenterLocation(Coordinate.of(x, 0)),
                    type,
                    Team.WHITE,
                    mainBoard.forward().getDirection(),
                    typeKey,
                    teamKey,
                    mobManager
            );
            spawnBarracksPiece(
                    blackBarracks.toCenterLocation(Coordinate.of(x, 7)),
                    type,
                    Team.BLACK,
                    mainBoard.forward().getDirection().multiply(-1),
                    typeKey,
                    teamKey,
                    mobManager
            );
        }

        setupReadyChest(whiteBarracks, Team.WHITE, 4);
        setupReadyChest(blackBarracks, Team.BLACK, 3);
    }

    private void setupReadyChest(ChessBoard barracks, Team team, int row) {
        Location origin = barracks.origin();
        BlockFace right = barracks.right();
        BlockFace forward = barracks.forward();
        int cellSize = barracks.cellSize();
        Location leftBlock = origin.clone()
                .add(right.getDirection().multiply(4 * cellSize - 1))
                .add(forward.getDirection().multiply(row * cellSize + 1));
        Location rightBlock = origin.clone()
                .add(right.getDirection().multiply(4 * cellSize))
                .add(forward.getDirection().multiply(row * cellSize + 1));
        BlockFace facing = team == Team.WHITE ? forward.getOppositeFace() : forward;

        leftBlock.getBlock().setType(Material.CHEST, false);
        rightBlock.getBlock().setType(Material.CHEST, false);
        gameManager.addBarracksChest(leftBlock);
        gameManager.addBarracksChest(rightBlock);

        Chest leftData = (Chest) leftBlock.getBlock().getBlockData();
        Chest rightData = (Chest) rightBlock.getBlock().getBlockData();

        leftData.setFacing(facing);
        rightData.setFacing(facing);

        if (facing == BlockFace.NORTH) {
            leftData.setType(Chest.Type.RIGHT);
            rightData.setType(Chest.Type.LEFT);
        } else if (facing == BlockFace.SOUTH) {
            leftData.setType(Chest.Type.LEFT);
            rightData.setType(Chest.Type.RIGHT);
        } else if (facing == BlockFace.EAST) {
            leftData.setType(Chest.Type.RIGHT);
            rightData.setType(Chest.Type.LEFT);
        } else {
            leftData.setType(Chest.Type.LEFT);
            rightData.setType(Chest.Type.RIGHT);
        }

        leftBlock.getBlock().setBlockData(leftData, true);
        rightBlock.getBlock().setBlockData(rightData, true);

        InventoryHolder leftHolder = (InventoryHolder) leftBlock.getBlock().getState();
        InventoryHolder rightHolder = (InventoryHolder) rightBlock.getBlock().getState();
        Inventory leftInv = leftHolder.getInventory();
        Inventory rightInv = rightHolder.getInventory();

        leftInv.clear();
        rightInv.clear();

        int participantCount = (int) gameManager.participants().values().stream()
                .filter(p -> p.team() == team)
                .count();

        if (participantCount > 0) {
            NamespacedKey orderKey = new NamespacedKey(plugin, "turn_order");

            for (int i = 1; i <= participantCount; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(Component.text(i + "번 순서", NamedTextColor.GOLD, TextDecoration.BOLD));
                meta.getPersistentDataContainer().set(orderKey, PersistentDataType.INTEGER, i);
                item.setItemMeta(meta);
                leftInv.addItem(item);
            }
        }

        NamespacedKey readyKey = new NamespacedKey(plugin, "ready_button");
        ItemStack readyBtn = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta readyMeta = readyBtn.getItemMeta();

        readyMeta.displayName(Component.text("[ 준비 완료 ]", NamedTextColor.GREEN, TextDecoration.BOLD));
        readyMeta.getPersistentDataContainer().set(readyKey, PersistentDataType.BYTE, (byte) 1);
        readyBtn.setItemMeta(readyMeta);
        rightInv.setItem(49, readyBtn);
    }

    private void spawnBarracksPiece(
            Location location,
            PieceType type,
            Team team,
            Vector direction,
            NamespacedKey typeKey,
            NamespacedKey teamKey,
            MobManager mobManager
    ) {
        String mobId = toPascalCase(team.name()) + toPascalCase(type.name());

        mobManager.getMythicMob(mobId).ifPresent(mythicMob -> {
            ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), 1);

            if (activeMob == null) {
                return;
            }

            Entity entity = activeMob.getEntity().getBukkitEntity();

            location.setDirection(direction);
            entity.teleport(location);
            entity.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
            entity.getPersistentDataContainer().set(teamKey, PersistentDataType.STRING, team.name());
            gameManager.addSpawnedEntity(entity.getUniqueId());
        });
    }

    private String toPascalCase(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        return source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase();
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

        gameManager.advancePhase(plugin, boardManager, timerManager);
        player.sendMessage(Component.text("게임을 시작합니다! 기물 선택 단계로 이동합니다.", NamedTextColor.GREEN));
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

        for (Map.Entry<Coordinate, Piece> entry : gameManager.boardPieces().entrySet()) {
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
