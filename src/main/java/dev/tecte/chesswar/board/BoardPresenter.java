package dev.tecte.chesswar.board;

import dev.tecte.chesswar.team.TeamSide;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class BoardPresenter {
    private static final float GRID_THICKNESS = 0.01f;
    private static final Display.Brightness MAX_BRIGHTNESS = new Display.Brightness(15, 15);

    @NotNull
    private final Plugin plugin;

    private final Map<UUID, List<BlockDisplay>> activeEntities = new HashMap<>();

    public void showGrid(@NotNull final Player player, @NotNull final Board board) {
        if (activeEntities.containsKey(player.getUniqueId())) {
            return;
        }

        final List<BlockDisplay> entities = new ArrayList<>();
        final Grid whiteGrid = board.getBarracks(TeamSide.WHITE).grid();
        final Grid blackGrid = board.getBarracks(TeamSide.BLACK).grid();

        spawnBaseLayer(player, board.grid(), entities);
        spawnBaseLayer(player, whiteGrid, entities);
        spawnBaseLayer(player, blackGrid, entities);
        activeEntities.put(player.getUniqueId(), entities);
    }

    public void hideGrid(@NotNull final Player player) {
        final List<BlockDisplay> entities = activeEntities.remove(player.getUniqueId());

        if (entities == null) {
            return;
        }

        for (final BlockDisplay entity : entities) {
            if (!entity.isValid()) {
                continue;
            }

            entity.remove();
        }
    }

    private void spawnBaseLayer(
            @NotNull final Player player,
            @NotNull final Grid grid,
            @NotNull final List<BlockDisplay> outList
    ) {
        final float offset = (float) Grid.CELL_OFFSET;
        final var translation = new Vector3f(-offset, GRID_THICKNESS, -offset);
        final var scale = new Vector3f(Grid.CELL_SIZE, GRID_THICKNESS, Grid.CELL_SIZE);
        final var rotation = new Quaternionf();
        final var transformation = new Transformation(translation, rotation, scale, rotation);

        for (int y = 0; y < Coordinate.BOARD_SIZE; y++) {
            for (int x = 0; x < Coordinate.BOARD_SIZE; x++) {
                final Coordinate coordinate = Coordinate.of(x, y);
                final Location center = grid.getCenterAt(coordinate);
                final Material material = coordinate.isDarkSquare()
                        ? Material.GRAY_STAINED_GLASS
                        : Material.LIGHT_GRAY_STAINED_GLASS;
                final BlockDisplay display = center.getWorld().spawn(center, BlockDisplay.class);

                display.setBlock(material.createBlockData());
                display.setPersistent(false);
                display.setTransformation(transformation);
                display.setShadowRadius(0f);
                display.setShadowStrength(0f);
                display.setBrightness(MAX_BRIGHTNESS);
                display.setVisibleByDefault(false);
                player.showEntity(plugin, display);
                outList.add(display);
            }
        }
    }
}
