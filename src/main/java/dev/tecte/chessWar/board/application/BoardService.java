package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardConfig;
import dev.tecte.chessWar.board.domain.model.FrameConfig;
import dev.tecte.chessWar.board.domain.model.InnerBorderConfig;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import dev.tecte.chessWar.board.domain.service.BoardCreationSpec;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import dev.tecte.chessWar.infrastructure.config.ConfigManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 체스판과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자의 요청을 받아 도메인 객체를 생성하고, 렌더링 및 영속성을 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardService {
    private final BoardFactory boardFactory;
    private final BoardRenderer boardRenderer;
    private final BoardRepository boardRepository;
    private final ConfigManager configManager;

    /**
     * 플레이어의 위치와 방향을 기준으로 새로운 체스판을 생성하고, 렌더링하며, 영속화합니다.
     *
     * @param player 체스판을 생성할 플레이어
     */
    public void createBoard(@NonNull Player player) {
        Orientation orientation = Orientation.from(player.getFacing());
        BoardConfig boardConfig = configManager.getPluginConfig().boardConfig();
        SquareConfig squareConfig = boardConfig.squareConfig();
        InnerBorderConfig innerBorderConfig = boardConfig.innerBorderConfig();
        FrameConfig frameConfig = boardConfig.frameConfig();

        // 체스판의 기준점(a1)은 좌측 하단 모서리
        // 플레이어가 자신의 위치를 기준으로 체스판을 자연스럽게 생성하도록 하려면,
        // 플레이어 위치에서 체스판의 기준점까지의 거리만큼 보정을 해야 함
        // 이 오프셋은 격자 너비의 절반만큼 왼쪽으로, 테두리 두께만큼 앞으로 이동하여 계산
        int gridWidth = squareConfig.colCount() * squareConfig.width();
        int thickness = innerBorderConfig.thickness() + frameConfig.thickness();
        Vector offset = orientation.getLeft().clone().multiply(gridWidth / 2)
                .add(orientation.getForward().clone().multiply(thickness));
        Vector playerPosition = player.getLocation().toBlockLocation().toVector();
        BoardCreationSpec spec = BoardCreationSpec.builder()
                .gridAnchor(playerPosition.clone().add(offset))
                .orientation(orientation)
                .squareConfig(squareConfig)
                .innerBorderConfig(innerBorderConfig)
                .frameConfig(frameConfig)
                .build();
        Board board = boardFactory.createBoard(spec);
        World world = player.getWorld();

        boardRenderer.render(board, world);
        boardRepository.save(board);
        log.info("Player '{}' created a new chessboard at {} in world '{}'",
                player.getName(), playerPosition, world.getName());
    }
}
