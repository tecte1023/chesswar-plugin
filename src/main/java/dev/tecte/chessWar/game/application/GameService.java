package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.domain.exception.GameStartConditionException;
import dev.tecte.chessWar.team.application.TeamService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;

/**
 * 게임의 생명주기와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameService {
    private static final int MIN_PlAYERS = 1;

    private final BoardService boardService;
    private final TeamService teamService;

    /**
     * 게임을 시작합니다.
     * 게임 시작 조건을 확인한 후, 게임을 시작 상태로 전환합니다.
     *
     * @param sender 명령어를 실행한 주체
     * @throws GameStartConditionException 게임 시작 조건을 충족하지 못했을 경우
     */
    @HandleException
    public void startGame(@NonNull CommandSender sender) {
        if (!boardService.isExists()) {
            throw GameStartConditionException.forBoardNotExists();
        }

        if (!teamService.areAllTeamsReadyToStart(MIN_PlAYERS)) {
            throw GameStartConditionException.forTeamsNotReady(MIN_PlAYERS);
        }

        log.info("게임 시작");
    }
}
