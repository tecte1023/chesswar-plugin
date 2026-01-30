package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.domain.model.Game;
import lombok.NonNull;

import java.util.Optional;

/**
 * 게임의 영속성을 관리합니다.
 */
public interface GameRepository {
    /**
     * 현재 진행 중인 게임을 찾습니다.
     *
     * @return 찾은 게임
     */
    @NonNull
    Optional<Game> find();

    /**
     * 현재 게임이 진행 중인지 확인합니다.
     *
     * @return 게임 진행 여부
     */
    boolean isGameInProgress();

    /**
     * 게임을 저장하거나 업데이트합니다.
     *
     * @param game 저장할 게임
     */
    void save(@NonNull Game game);

    /**
     * 현재 게임을 삭제합니다.
     */
    void delete();
}
