package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 종료 단계에서 사용되는 불변 객체입니다.
 */
public record EndedState() implements PhaseState {
    @NonNull
    @Override
    public GamePhase phase() {
        return GamePhase.ENDED;
    }
}
