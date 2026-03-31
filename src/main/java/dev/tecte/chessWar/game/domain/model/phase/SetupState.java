package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 준비 단계의 상태 표식입니다.
 */
public record SetupState() implements PhaseState {
    /**
     * 표준 준비 상태를 생성합니다.
     *
     * @return 준비 상태
     */
    @NonNull
    public static SetupState empty() {
        return new SetupState();
    }

    @NonNull
    @Override
    public GamePhase phase() {
        return GamePhase.SETUP;
    }
}
