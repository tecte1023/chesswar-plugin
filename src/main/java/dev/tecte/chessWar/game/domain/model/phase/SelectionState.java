package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 기물 선택 단계에서 사용되는 불변 객체입니다.
 *
 * @param selections 플레이어 식별자와 기물 식별자 매핑
 */
public record SelectionState(Map<UUID, UUID> selections) implements PhaseState {
    public SelectionState {
        Objects.requireNonNull(selections, "Selections map cannot be null");
        selections = Map.copyOf(selections);
    }

    /**
     * 주어진 선택 정보로 상태를 생성합니다.
     *
     * @param selections 플레이어 식별자와 기물 식별자 매핑
     * @return 생성된 상태
     */
    @NonNull
    public static SelectionState of(@NonNull Map<UUID, UUID> selections) {
        return new SelectionState(selections);
    }

    /**
     * 빈 선택 상태로 초기화된 객체를 반환합니다.
     *
     * @return 초기화된 상태
     */
    @NonNull
    public static SelectionState empty() {
        return new SelectionState(Collections.emptyMap());
    }

    /**
     * 선택 정보가 추가된 새로운 상태를 반환합니다.
     *
     * @param playerId 선택한 플레이어의 식별자
     * @param pieceId  선택된 기물의 식별자
     * @return 업데이트된 상태
     */
    @NonNull
    public SelectionState withSelection(@NonNull UUID playerId, @NonNull UUID pieceId) {
        Map<UUID, UUID> newSelections = new HashMap<>(selections);

        newSelections.put(playerId, pieceId);

        return new SelectionState(newSelections);
    }

    /**
     * 해당 기물이 선택되었는지 확인합니다.
     *
     * @param pieceId 기물의 식별자
     * @return 선택 여부
     */
    public boolean isSelected(@NonNull UUID pieceId) {
        return selections.containsValue(pieceId);
    }

    @Override
    public @NonNull GamePhase phase() {
        return GamePhase.PIECE_SELECTION;
    }
}
