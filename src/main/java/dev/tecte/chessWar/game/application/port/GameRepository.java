package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.domain.model.Game;
import lombok.NonNull;

import java.util.Optional;

/**
 * 게임 애그리거트 루트의 생명주기 및 저장을 관리하는 리포지토리 인터페이스입니다.
 * <p>
 * 이 인터페이스는 {@link Game} 객체 컬렉션에 대한 추상화를 제공하여, 애플리케이션 계층이 영속성 메커니즘에 구애받지 않도록 합니다.
 */
public interface GameRepository {
    /**
     * 현재 게임 인스턴스를 저장하거나 업데이트합니다.
     * <p>
     * 게임이 진행 중인 경우, 이 메서드는 기존 게임 상태를 제공된 게임 상태로 교체합니다.
     * 활성화된 게임이 없는 경우, 제공된 게임을 현재 게임으로 설정합니다.
     *
     * @param game 저장할 {@link Game} 인스턴스
     */
    void save(@NonNull Game game);

    /**
     * 현재 게임 인스턴스를 찾습니다. 이 컨텍스트에서는 한 번에 하나의 게임만 존재합니다.
     *
     * @return {@link Game} 인스턴스가 존재하면 이를 포함하는 {@link Optional}을 반환하고, 그렇지 않으면 빈 {@link Optional}을 반환
     */
    @NonNull
    Optional<Game> find();

    /**
     * 현재 게임이 진행 중인지 확인합니다.
     * 게임 데이터가 존재하면 진행 중인 것으로 간주합니다.
     *
     * @return 게임이 진행 중이면 true, 그렇지 않으면 false
     */
    boolean isGameInProgress();

    /**
     * 현재 게임 인스턴스를 삭제합니다.
     */
    void delete();
}
