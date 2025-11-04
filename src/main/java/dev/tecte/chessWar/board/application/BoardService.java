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
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

/**
 * 체스판과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자의 요청을 받아 도메인 객체를 생성하고, 렌더링 및 영속성을 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardService {
    private static final Component BOARD_CREATE_SUCCESS = Component.text(
            "체스판이 생성되었습니다.",
            NamedTextColor.WHITE
    );

    private final BoardFactory boardFactory;
    private final BoardRenderer boardRenderer;
    private final BoardRepository boardRepository;
    private final GridSpec gridSpec;
    private final SquareSpec squareSpec;
    private final BorderSpec borderSpec;
    private final SenderNotifier senderNotifier;

    /**
     * 플레이어의 위치와 방향을 기준으로 새로운 체스판을 생성하고, 렌더링하며, 영속화합니다.
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
        player.sendMessage(BOARD_CREATE_SUCCESS);
        senderNotifier.notifySuccess(player, BOARD_CREATE_SUCCESS);
        log.info("Player '{}' created a new chessboard at {} in world '{}'",
                player.getName(), playerPosition, world.getName());
    }

    /**
     * 저장된 체스판 데이터를 조회합니다.
     *
     * @return 체스판이 존재하면 {@link Optional}에 담아 반환하고, 없으면 빈 {@link Optional}을 반환
     */
    @NonNull
    public Optional<Board> getBoard() {
        return boardRepository.get();
    }
}
