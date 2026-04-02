package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 기물 선택 단계의 점유 현황을 관리합니다.
 *
 * @param selections 플레이어와 기물의 매핑 데이터
 */
public record SelectionState(Map<UUID, UUID> selections) implements PhaseState {
    public SelectionState {
        Objects.requireNonNull(selections, "Selections map cannot be null");
        selections = Map.copyOf(selections);
    }

    /**
     * 선택 정보를 기반으로 상태를 생성합니다.
     *
     * @param selections 플레이어-기물 매핑 데이터
     * @return 선택 상태
     */
    @NonNull
    public static SelectionState of(@NonNull Map<UUID, UUID> selections) {
        return new SelectionState(selections);
    }

    /**
     * 비어 있는 초기 상태를 생성합니다.
     *
     * @return 초기 상태
     */
    @NonNull
    public static SelectionState empty() {
        return new SelectionState(Collections.emptyMap());
    }

    /**
     * 기물 선택 정보가 추가된 새로운 상태를 제공합니다.
     *
     * @param playerId 플레이어 ID
     * @param pieceId  선택된 기물 ID
     * @return 업데이트된 상태
     */
    @NonNull
    public SelectionState withSelection(@NonNull UUID playerId, @NonNull UUID pieceId) {
        Map<UUID, UUID> newSelections = new HashMap<>(selections);

        newSelections.put(playerId, pieceId);

        return new SelectionState(newSelections);
    }

    /**
     * 기물의 선택 여부를 확인합니다.
     *
     * @param pieceId 기물 ID
     * @return 기물 선택 여부
     */
    public boolean isSelected(@NonNull UUID pieceId) {
        return selections.containsValue(pieceId);
    }

    /**
     * 플레이어의 선택 완료 여부를 확인합니다.
     *
     * @param playerId 플레이어 ID
     * @return 선택 완료 여부
     */
    public boolean hasSelectionFor(@NonNull UUID playerId) {
        return selections.containsKey(playerId);
    }

    @NonNull
    @Override
    public GamePhase phase() {
        return GamePhase.PIECE_SELECTION;
    }
}
