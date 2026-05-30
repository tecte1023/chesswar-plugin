package dev.tecte.chesswar.board;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.Statistics;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BoardVisualManager {
    private final ChessWar plugin;

    private final Map<UUID, List<Coordinate>> activeGuides = new HashMap<>();

    public void showGuide(Player player) {
        if (!plugin.boardManager().hasBoard()) {
            return;
        }

        Optional<UUID> currentTurnId = plugin.gameManager().currentTurnPlayer();
        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(player.getUniqueId())) {
            return;
        }

        Coordinate from = null;
        Team myTeam = null;

        Optional<Coordinate> commandTarget = plugin.gameManager().findCommandTarget(player.getUniqueId());

        if (commandTarget.isPresent()) {
            from = commandTarget.get();
            Piece targetPiece = plugin.pieceManager().boardPieces().get(from);
            if (targetPiece != null) {
                myTeam = targetPiece.team();
            }
        } else {
            for (var entry : plugin.pieceManager().boardPieces().entrySet()) {
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

                if (plugin.moveValidator().canMove(from, to)) {
                    Optional<Piece> target = plugin.pieceManager().findPieceAt(to);

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
                    plugin.boardManager().currentBoard().toCenterLocation(coordinate),
                    guideMaterials.get(coordinate).createBlockData()
            );
        }

        activeGuides.put(player.getUniqueId(), validMoves);
    }

    public void clearGuide(Player player) {
        List<Coordinate> guides = activeGuides.remove(player.getUniqueId());

        if (guides == null || !plugin.boardManager().hasBoard()) {
            return;
        }

        for (Coordinate coordinate : guides) {
            Location centerLocation = plugin.boardManager().currentBoard().toCenterLocation(coordinate);

            player.sendBlockChange(centerLocation, centerLocation.getBlock().getBlockData());
        }
    }

    public void clearAllGuides() {
        for (UUID uuid : new ArrayList<>(activeGuides.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                clearGuide(player);
            } else {
                activeGuides.remove(uuid);
            }
        }
    }

    public void displayStatisticsHologram() {
        if (!plugin.boardManager().hasBoard()) {
            return;
        }

        Location center = plugin.boardManager().currentBoard().toCenterLocation(Coordinate.of(3, 3))
                .add(plugin.boardManager().currentBoard().right().getDirection().multiply(plugin.boardManager().currentBoard().cellSize() / 2.0))
                .add(0, 2, 0);

        List<net.kyori.adventure.text.Component> lines = new ArrayList<>();
        lines.add(net.kyori.adventure.text.Component.text(" [ 전투 결과 통계 ] ", net.kyori.adventure.text.format.NamedTextColor.GOLD, net.kyori.adventure.text.format.TextDecoration.BOLD));
        lines.add(net.kyori.adventure.text.Component.empty());

        plugin.gameManager().participants().values().forEach(p -> {
            Statistics s = plugin.gameManager().stats(p.playerId());
            Player player = Bukkit.getPlayer(p.playerId());
            String name = (player != null) ? player.getName() : "오프라인";

            lines.add(net.kyori.adventure.text.Component.text()
                    .append(net.kyori.adventure.text.Component.text(name, p.team().color()))
                    .append(net.kyori.adventure.text.Component.text(" | ", net.kyori.adventure.text.format.NamedTextColor.GRAY))
                    .append(net.kyori.adventure.text.Component.text("⚔" + (int) s.getDamageDealt(), net.kyori.adventure.text.format.NamedTextColor.RED))
                    .append(net.kyori.adventure.text.Component.text(" 🛡" + (int) s.getDamageTaken(), net.kyori.adventure.text.format.NamedTextColor.BLUE))
                    .append(net.kyori.adventure.text.Component.text(" ➕" + (int) s.getHealingDone(), net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    .append(net.kyori.adventure.text.Component.text(" ☠" + s.getKills() + "/" + s.getDeaths(), net.kyori.adventure.text.format.NamedTextColor.DARK_RED))
                    .build());
        });

        for (int i = 0; i < lines.size(); i++) {
            final int index = i;
            Location lineLoc = center.clone().subtract(0, i * 0.3, 0);
            lineLoc.getWorld().spawn(lineLoc, org.bukkit.entity.ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setCustomNameVisible(true);
                as.customName(lines.get(index));
                as.setMarker(true);
                plugin.pieceManager().addSpawnedEntity(as.getUniqueId());
            });
        }
    }
}
