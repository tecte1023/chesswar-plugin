package dev.tecte.chessWar.board.infrastructure.bukkit;

import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.Border;
import dev.tecte.chessWar.board.domain.model.Square;
import dev.tecte.chessWar.board.domain.model.SquareColor;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.model.SquareGrid;
import dev.tecte.chessWar.infrastructure.config.ConfigManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BoundingBox;

/**
 * Bukkit API를 사용하여 체스판을 실제 월드에 렌더링하는 클래스입니다.
 * {@link BoardRenderer} 인터페이스의 구현체입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BukkitBoardRenderer implements BoardRenderer {
    private final ConfigManager configManager;

    @Override
    public void render(@NonNull Board board, @NonNull World world) {
        BoardConfig config = configManager.getPluginConfig().boardConfig();

        renderSquares(board, world, config.squareConfig());
        renderBorder(board.innerBorder(), world, config.innerBorderConfig().block());
        renderBorder(board.frame(), world, config.frameConfig().block());
    }

    private void renderSquares(@NonNull Board board, @NonNull World world, @NonNull SquareConfig squareConfig) {
        int rowCount = squareConfig.rowCount();
        int columnCount = squareConfig.colCount();
        SquareGrid squareGrid = board.squareGrid();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                Square square = squareGrid.getSquareAt(row, col);
                BoundingBox boundingBox = square.boundingBox();
                Material material = square.color() == SquareColor.BLACK ? squareConfig.blackBlock() : squareConfig.whiteBlock();

                // 두 번째 인수인 applyPhysics를 false로 설정하면, 블록 변경 시 주변 블록에 물리적 업데이트(예: 물 흐름, 중력)를 유발하지 않음
                // 대량의 블록을 빠르게 변경할 때 서버 부하를 크게 줄일 수 있음
                for (int x = (int) boundingBox.getMinX(); x < boundingBox.getMaxX(); x++) {
                    for (int z = (int) boundingBox.getMinZ(); z < boundingBox.getMaxZ(); z++) {
                        world.getBlockAt(x, (int) boundingBox.getMinY(), z).setType(material, false);
                    }
                }
            }
        }
    }

    private void renderBorder(@NonNull Border border, @NonNull World world, @NonNull Material material) {
        BlockData blockDataX = createOrientableBlockData(material, Axis.X);
        BlockData blockDataZ = createOrientableBlockData(material, Axis.Z);

        BoundingBox boundingBox = border.boundingBox();
        int thickness = border.thickness();

        int minY = (int) boundingBox.getMinY();
        int minX = (int) boundingBox.getMinX();
        int minZ = (int) boundingBox.getMinZ();
        int maxX = (int) boundingBox.getMaxX();
        int maxZ = (int) boundingBox.getMaxZ();

        // 테두리를 속이 빈 사각형 형태로 만들기 위해, 바깥쪽부터 안쪽으로 두께만큼 블럭을 설치
        for (int i = 0; i < thickness; i++) {
            // Z축에 평행한 테두리
            for (int x = minX + i; x < maxX - i; x++) {
                world.getBlockAt(x, minY, maxZ - 1 - i).setBlockData(blockDataX, false);
                world.getBlockAt(x, minY, minZ + i).setBlockData(blockDataX, false);
            }

            // X축에 평행한 테두리
            for (int z = minZ + i + 1; z < maxZ - i - 1; z++) {
                world.getBlockAt(maxX - 1 - i, minY, z).setBlockData(blockDataZ, false);
                world.getBlockAt(minX + i, minY, z).setBlockData(blockDataZ, false);
            }
        }
    }

    /**
     * 특정 축 방향을 갖는 {@link Orientable} 블록 데이터를 생성합니다.
     * 만약 주어진 재료가 Orientable이 아니면 일반 블록 데이터가 반환됩니다.
     *
     * @param material 블록의 재료
     * @param axis     설정할 축 (X, Y, Z)
     * @return 생성된 블록 데이터
     */
    @NonNull
    private BlockData createOrientableBlockData(@NonNull Material material, @NonNull Axis axis) {
        // Bukkit.createBlockData의 람다를 사용하면,
        // instanceof 패턴 매칭을 통해 불필요한 형변환 코드 없이 안전하게 블록 데이터를 수정할 수 있음
        return Bukkit.createBlockData(material, data -> {
            if (data instanceof Orientable orientable) {
                orientable.setAxis(axis);
            }
        });
    }
}
