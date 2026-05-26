package dev.tecte.chesswar.board;

import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GameResetEvent;
import dev.tecte.chesswar.game.TurnStartedEvent;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceMoveEvent;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BoardVisualGuideListener implements Listener {
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final MoveValidator moveValidator;

    private final Map<UUID, List<Coordinate>> activeGuides = new HashMap<>();

    @EventHandler
    public void onGameReset(GameResetEvent event) {
        for (UUID uuid : new ArrayList<>(activeGuides.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                clearGuide(player);
            } else {
                activeGuides.remove(uuid);
            }
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        clearGuide(player);

        if (PieceItemUtils.isPieceItem(newItem)) {
            showGuide(player);
        }
    }

    @EventHandler
    public void onPieceMove(PieceMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        clearGuide(player);

        if (PieceItemUtils.isPieceItem(item)) {
            showGuide(player);
        }
    }

    @EventHandler
    public void onTurnStarted(TurnStartedEvent event) {
        for (UUID uuid : new ArrayList<>(activeGuides.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                clearGuide(p);
            } else {
                activeGuides.remove(uuid);
            }
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (PieceItemUtils.isPieceItem(item)) {
            showGuide(player);
        }
    }

    @EventHandler
    public void onCommandTargetSelected(BoardTargetSelectedEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        clearGuide(player);

        if (PieceItemUtils.isPieceItem(item)) {
            showGuide(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        activeGuides.remove(event.getPlayer().getUniqueId());
    }

    private void showGuide(Player player) {
        if (!boardManager.hasBoard()) return;

        Optional<UUID> currentTurnId = gameManager.currentTurnPlayer();
        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(player.getUniqueId())) {
            return;
        }

        Coordinate from = null;
        Team myTeam = null;

        Optional<Coordinate> commandTarget = gameManager.getCommandTarget(player.getUniqueId());

        if (commandTarget.isPresent()) {
            from = commandTarget.get();
            Piece targetPiece = pieceManager.boardPieces().get(from);
            if (targetPiece != null) {
                myTeam = targetPiece.team();
            }
        } else {
            for (var entry : pieceManager.boardPieces().entrySet()) {
                if (player.getUniqueId().equals(entry.getValue().ownerId())) {
                    from = entry.getKey();
                    myTeam = entry.getValue().team();
                    break;
                }
            }
        }

        if (from == null || myTeam == null) {
            return;
        }

        List<Coordinate> validMoves = new ArrayList<>();
        Map<Coordinate, Material> guideMaterials = new HashMap<>();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Coordinate to = Coordinate.of(x, y);

                if (moveValidator.canMove(from, to)) {
                    Optional<Piece> target = pieceManager.findPieceAt(to);
                    
                    if (target.isEmpty()) {
                        validMoves.add(to);
                        guideMaterials.put(to, Material.LIME_STAINED_GLASS);
                    } else if (target.get().team() != myTeam) {
                        validMoves.add(to);
                        guideMaterials.put(to, Material.RED_STAINED_GLASS);
                    }
                }
            }
        }

        for (Coordinate coordinate : validMoves) {
            player.sendBlockChange(
                    boardManager.currentBoard().toCenterLocation(coordinate),
                    guideMaterials.get(coordinate).createBlockData()
            );
        }

        activeGuides.put(player.getUniqueId(), validMoves);
    }

    private void clearGuide(Player player) {
        List<Coordinate> guides = activeGuides.remove(player.getUniqueId());

        if (guides == null || !boardManager.hasBoard()) {
            return;
        }

        for (Coordinate coordinate : guides) {
            Location centerLocation = boardManager.currentBoard().toCenterLocation(coordinate);

            player.sendBlockChange(centerLocation, centerLocation.getBlock().getBlockData());
        }
    }
}
