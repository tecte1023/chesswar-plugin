package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.board.application.port.BoardRenderer;
import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.service.BoardFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final BoardRepository boardRepository;
    private final BoardRenderer boardRenderer;
    private final BoardNotifier boardNotifier;

    /**
     * 플레이어의 위치와 방향을 기준으로 체스판을 생성합니다.
     *
     * @param player 체스판을 생성할 플레이어
     */
    public void createBoard(@NonNull Player player) {
        String worldName = player.getWorld().getName();
        Vector playerPosition = player.getLocation().toBlockLocation().toVector();
        Orientation orientation = Orientation.from(player.getFacing());
        Board board = boardFactory.createAt(worldName, playerPosition, orientation);

        boardRenderer.render(board, player.getWorld());
        boardRepository.save(board);
        boardNotifier.informCreated(player);
        log.atInfo().log("Player '{}' created a new chessboard at {} in world '{}'",
                player.getName(), playerPosition, worldName);
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
