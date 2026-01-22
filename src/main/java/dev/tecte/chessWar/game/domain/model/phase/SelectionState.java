package dev.tecte.chessWar.game.domain.model.phase;

import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 기물 선택 단계에서 사용되는 상태 데이터입니다.
 *
 * @param selections 플레이어 ID와 기물 엔티티 ID 매핑
 */
public record SelectionState(Map<UUID, UUID> selections) implements PhaseState {
    public SelectionState {
        Objects.requireNonNull(selections, "Selections map cannot be null");
        selections = Map.copyOf(selections);
    }

    /**
     * 빈 선택 상태로 초기화된 객체를 반환합니다.
     *
     * @return 초기화된 {@link SelectionState}
     */
    @NonNull
    public static SelectionState empty() {
        return new SelectionState(Collections.emptyMap());
    }
}
