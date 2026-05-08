package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 게임의 준비 단계 상태입니다.
 */
public record SetupState() implements PhaseState {
    /**
     * 초기 준비 상태를 생성합니다.
     *
     * @return 준비 상태
     */
    @NonNull
    public static SetupState initial() {
        return new SetupState();
    }

    @NonNull
    @Override
    public GamePhase phase() {
        return GamePhase.SETUP;
    }
}
