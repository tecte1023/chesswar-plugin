package dev.tecte.chesswar.board;

import dev.tecte.chesswar.ChessWar;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class BoardVisualManager {
    private static final BlockData GUIDE_MOVE = Material.LIME_STAINED_GLASS.createBlockData();
    private static final BlockData GUIDE_CAPTURE = Material.RED_STAINED_GLASS.createBlockData();
    private static final Particle.DustOptions OUTLINE_OPTIONS = new Particle.DustOptions(Color.GREEN, 1.0f);

    private final Map<UUID, List<Coordinate>> activeGuides = new HashMap<>();
    private final ChessWar plugin;
    private final BoardManager boardManager;

    public void showGuide(final Player player, final Map<Coordinate, Boolean> moves) {
        clearGuide(player);

        if (!boardManager.hasBoard() || moves.isEmpty()) {
            return;
        }

        final ChessBoard board = boardManager.currentBoard();
        final List<Coordinate> validMoves = new ArrayList<>(moves.size());
        final Location reusableLoc = board.origin().clone();

        for (final Map.Entry<Coordinate, Boolean> entry : moves.entrySet()) {
            final Coordinate to = entry.getKey();
            final boolean isCapture = entry.getValue();

            board.updateToCenterLocation(to, reusableLoc);
            player.sendBlockChange(reusableLoc, isCapture ? GUIDE_CAPTURE : GUIDE_MOVE);
            validMoves.add(to);
        }

        activeGuides.put(player.getUniqueId(), validMoves);
    }

    public void clearGuide(final Player player) {
        final List<Coordinate> guides = activeGuides.remove(player.getUniqueId());

        if (guides == null || !boardManager.hasBoard()) {
            return;
        }

        final ChessBoard board = boardManager.currentBoard();
        final Location reusableLoc = board.origin().clone();

        for (final Coordinate coordinate : guides) {
            board.updateToCenterLocation(coordinate, reusableLoc);
            player.sendBlockChange(reusableLoc, reusableLoc.getBlock().getBlockData());
        }
    }

    public void clearAllGuides() {
        if (activeGuides.isEmpty()) {
            return;
        }

        if (!boardManager.hasBoard()) {
            activeGuides.clear();
            return;
        }

        final ChessBoard board = boardManager.currentBoard();
        final Location reusableLoc = board.origin().clone();

        for (final Iterator<Map.Entry<UUID, List<Coordinate>>> iterator = activeGuides.entrySet().iterator();
             iterator.hasNext(); ) {
            final Map.Entry<UUID, List<Coordinate>> entry = iterator.next();
            final Player player = Bukkit.getPlayer(entry.getKey());

            iterator.remove();

            if (player == null) {
                continue;
            }

            final List<Coordinate> guides = entry.getValue();

            for (final Coordinate coordinate : guides) {
                board.updateToCenterLocation(coordinate, reusableLoc);
                player.sendBlockChange(reusableLoc, reusableLoc.getBlock().getBlockData());
            }
        }
    }

    public void visualizeBoardOutline(final Player player, final ChessBoard board) {
        final Location origin = board.origin();
        final Vector forwardDir = board.forward().getDirection();
        final Vector rightDir = board.right().getDirection();

        final double startX = origin.getX() + 0.5;
        final double startY = origin.getY() + 0.1;
        final double startZ = origin.getZ() + 0.5;

        final double dxF = forwardDir.getX();
        final double dzF = forwardDir.getZ();
        final double dxR = rightDir.getX();
        final double dzR = rightDir.getZ();

        final int physicalSize = 8 * board.cellSize();
        final int maxOffset = physicalSize - 1;

        final double offsetFX = dxF * maxOffset;
        final double offsetFZ = dzF * maxOffset;
        final double offsetRX = dxR * maxOffset;
        final double offsetRZ = dzR * maxOffset;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 100) {
                    cancel();
                    return;
                }

                for (int i = 0; i < physicalSize; i++) {
                    final double stepRX = dxR * i;
                    final double stepRZ = dzR * i;

                    spawnParticle(startX + stepRX, startY, startZ + stepRZ);
                    spawnParticle(startX + stepRX + offsetFX, startY, startZ + stepRZ + offsetFZ);

                    final double stepFX = dxF * i;
                    final double stepFZ = dzF * i;

                    spawnParticle(startX + stepFX, startY, startZ + stepFZ);
                    spawnParticle(startX + stepFX + offsetRX, startY, startZ + stepFZ + offsetRZ);
                }

                ticks += 5;
            }

            private void spawnParticle(final double x, final double y, final double z) {
                player.spawnParticle(
                        Particle.DUST,
                        x, y, z,
                        1,
                        OUTLINE_OPTIONS
                );
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
