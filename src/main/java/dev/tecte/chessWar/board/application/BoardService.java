package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.BoardCreationParams;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.spec.BorderSpec;
import dev.tecte.chessWar.board.domain.model.spec.GridSpec;
import dev.tecte.chessWar.board.domain.model.spec.SquareSpec;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

/**
 * 체스판 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardService {
    private final BoardFactory boardFactory;
    private final BoardNotifier boardNotifier;
    private final BoardRenderer boardRenderer;
    private final BoardRepository boardRepository;
    private final GridSpec gridSpec;
    private final SquareSpec squareSpec;
    private final BorderSpec borderSpec;

    /**
     * 플레이어의 위치와 방향을 기준으로 체스판을 생성합니다.
     *
     * @param player 체스판을 생성할 플레이어
     */
    public void createBoard(@NonNull Player player) {
        Orientation orientation = Orientation.from(player.getFacing());
        // 체스판의 기준점(a1)은 좌측 하단 모서리
        // 플레이어가 자신의 위치를 기준으로 체스판을 자연스럽게 생성하도록 하려면,
        // 플레이어 위치에서 체스판의 기준점까지의 거리만큼 보정을 해야 함
        // 이 오프셋은 격자 너비의 절반만큼 왼쪽으로, 테두리 두께만큼 앞으로 이동하여 계산
        int gridWidth = squareSpec.width() * gridSpec.colCount();
        int thickness = borderSpec.innerThickness() + borderSpec.frameThickness();
        Vector offset = orientation.left().multiply(gridWidth / 2)
                .add(orientation.forward().multiply(thickness));
        Vector playerPosition = player.getLocation().toBlockLocation().toVector();
        Vector gridAnchor = playerPosition.clone().add(offset);
        World world = player.getWorld();
        BoardCreationParams params = new BoardCreationParams(world.getName(), gridAnchor, orientation);
        Board board = boardFactory.createBoard(params);

        boardRenderer.render(board, world);
        boardRepository.save(board);
        boardNotifier.notifyBoardCreate(player);
        log.info("Player '{}' created a new chessboard at {} in world '{}'",
                player.getName(), playerPosition, world.getName());
    }

    /**
     * 체스판을 찾습니다.
     *
     * @return 찾은 체스판
     */
    @NonNull
    public Optional<Board> findBoard() {
        return boardRepository.find();
    }
}
