/*
package dev.tecte.chesswar.board;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.GameAnnouncer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
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
    private static final BlockData GUIDE_INTERACT = Material.LIGHT_BLUE_STAINED_GLASS.createBlockData();
    private static final Particle.DustOptions OUTLINE_OPTIONS = new Particle.DustOptions(Color.GREEN, 1.0f);

    private final Map<UUID, List<BlockDisplay>> activeGuideEntities = new HashMap<>();
    private final ChessWar plugin;
    private final BoardComponent boardComponent;
    private final BoardManager boardManager;
    private final GameAnnouncer announcer;

    public void showGuide(final Player player, final Map<Coordinate, GuideType> moves) {
        clearGuide(player);

        if (!boardComponent.hasBoard() || moves.isEmpty()) {
            return;
        }

        final ChessBoard board = boardComponent.board();
        final List<BlockDisplay> entities = new ArrayList<>(moves.size());
        final Location centerLoc = board.origin().clone();
        final float size = (float) board.cellSize();

        for (final Map.Entry<Coordinate, GuideType> entry : moves.entrySet()) {
            final Coordinate to = entry.getKey();
            final GuideType type = entry.getValue();

            board.updateToCenterLocation(to, centerLoc);

            // BlockDisplay 스폰 (정확한 3x3 범위를 위해 중심에서 절반만큼 차감하여 코너에서 시작)
            final Location spawnLoc = centerLoc.clone().subtract(size / 2.0, 0, size / 2.0);

            final BlockDisplay display = spawnLoc.getWorld().spawn(spawnLoc, BlockDisplay.class, entity -> {
                if (type == GuideType.CAPTURE) {
                    entity.setBlock(GUIDE_CAPTURE);
                } else if (type == GuideType.INTERACT) {
                    entity.setBlock(GUIDE_INTERACT);
                } else {
                    entity.setBlock(GUIDE_MOVE);
                }

                // 크기 조절 (3x3x0.01), 그림자 제거 및 밝기 최적화
                final Transformation transformation = entity.getTransformation();
                transformation.getScale().set(size, 0.01f, size);
                entity.setTransformation(transformation);

                entity.setShadowRadius(0f);
                entity.setShadowStrength(0f);
                entity.setBrightness(new Display.Brightness(15, 15));
                entity.setVisibleByDefault(false);
            });

            player.showEntity(plugin, display);
            entities.add(display);
        }

        activeGuideEntities.put(player.getUniqueId(), entities);
    }

    public void clearGuide(final Player player) {
        final List<BlockDisplay> entities = activeGuideEntities.remove(player.getUniqueId());

        if (entities != null) {
            for (final BlockDisplay entity : entities) {
                entity.remove();
            }
        }
    }

    public void clearAllGuides() {
        for (final Iterator<Map.Entry<UUID, List<BlockDisplay>>> iterator = activeGuideEntities.entrySet().iterator();
             iterator.hasNext(); ) {
            final Map.Entry<UUID, List<BlockDisplay>> entry = iterator.next();
            iterator.remove();

            final List<BlockDisplay> entities = entry.getValue();
            for (final BlockDisplay entity : entities) {
                entity.remove();
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

        final int physicalSize = Coordinate.BOARD_SIZE * board.cellSize();
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

                    announcer.announceBoardOutline(player, startX + stepRX, startY, startZ + stepRZ, OUTLINE_OPTIONS);
                    announcer.announceBoardOutline(player, startX + stepRX + offsetFX, startY, startZ + stepRZ + offsetFZ, OUTLINE_OPTIONS);

                    final double stepFX = dxF * i;
                    final double stepFZ = dzF * i;

                    announcer.announceBoardOutline(player, startX + stepFX, startY, startZ + stepFZ, OUTLINE_OPTIONS);
                    announcer.announceBoardOutline(player, startX + stepFX + offsetRX, startY, startZ + stepFZ + offsetRZ, OUTLINE_OPTIONS);
                }

                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
*/
