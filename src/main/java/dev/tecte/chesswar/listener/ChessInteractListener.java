package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.event.ChessPieceMoveEvent;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ChessInteractListener implements Listener {
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final MoveValidator moveValidator;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (gameManager.phase() == GamePhase.WAITING) {
            handleWaitingInteraction(event, player, item);
            return;
        }

        if (gameManager.phase() == GamePhase.BATTLE) {
            handleBattleInteraction(event, player, item);
        }
    }

    private void handleWaitingInteraction(PlayerInteractEvent event, Player player, ItemStack item) {
        if (item == null || (
                event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK
        )) {
            return;
        }

        Team targetTeam = null;

        if (item.getType() == Material.WHITE_WOOL) {
            targetTeam = Team.WHITE;
        } else if (item.getType() == Material.BLACK_WOOL) {
            targetTeam = Team.BLACK;
        }

        if (targetTeam != null) {
            gameManager.join(player, targetTeam);
            player.sendMessage(Component.text(targetTeam.displayName() + "에 참가했습니다!", targetTeam.textColor()));
        }
    }

    private void handleBattleInteraction(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!PieceItemUtils.isPieceItem(item)) {
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        Optional<UUID> currentTurnId = gameManager.currentTurnPlayer();

        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!boardManager.hasBoard()) {
            player.sendMessage(Component.text("체스판이 설정되지 않았습니다!", NamedTextColor.RED));
            return;
        }

        Coordinate to = boardManager.currentBoard().toCoordinate(player.getLocation());

        if (!to.isValid()) {
            player.sendMessage(Component.text("체스판 밖에서는 이동을 확정할 수 없습니다!", NamedTextColor.RED));
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

        Optional<Coordinate> commandTarget = gameManager.getCommandTarget(player.getUniqueId());
        Coordinate finalFrom = (commandTarget.isPresent()) ? commandTarget.get() : from;

        if (finalFrom.equals(to)) {
            player.sendMessage(Component.text("현재 위치와 같은 곳으로 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        if (!moveValidator.canMove(finalFrom, to)) {
            player.sendMessage(Component.text("그곳으로는 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        Piece movingPiece = gameManager.boardPieces().get(finalFrom);
        Optional<Piece> targetPiece = gameManager.findPieceAt(to);

        if (targetPiece.isPresent()) {
            player.sendMessage(Component.text(
                    "그곳에 다른 기물이 있어 이동할 수 없습니다. (공격은 좌클릭으로 하세요!)",
                    NamedTextColor.YELLOW
            ));

            return;
        }

        gameManager.removePiece(finalFrom);
        gameManager.placePiece(to, movingPiece);

        if (commandTarget.isPresent()) {
            NamespacedKey coordXKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_x");
            NamespacedKey coordYKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_y");

            for (org.bukkit.entity.Entity entity : boardManager.currentBoard().origin().getWorld().getEntities()) {
                Integer ex = entity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
                Integer ey = entity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

                if (ex != null && ey != null && finalFrom.x() == ex && finalFrom.y() == ey) {
                    entity.teleport(boardManager.currentBoard().toCenterLocation(to));
                    entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, to.x());
                    entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, to.y());
                    break;
                }
            }
            gameManager.clearCommandTarget(player.getUniqueId());
            player.sendMessage(Component.text(movingPiece.type().displayName() + " 기물을 " + to.x() + ", " + to.y() + " 좌표로 이동시켰습니다.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text(to.x() + ", " + to.y() + " 좌표로 이동했습니다.", NamedTextColor.GREEN));
        }

        Bukkit.getPluginManager().callEvent(new ChessPieceMoveEvent(player));
        gameManager.finishTurn();
    }
}
