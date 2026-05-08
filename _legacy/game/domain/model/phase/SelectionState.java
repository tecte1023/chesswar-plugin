package dev.tecte.chessWar.game.domain.model.phase;

import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import lombok.NonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 기물 선택 단계의 진행 상태입니다.
 *
 * @param selections    참가자와 기물의 선택 현황
 * @param timerSettings 타이머 설정
 * @param remainingTime 남은 시간
 */
public record SelectionState(
        Map<UUID, UUID> selections,
        PhaseTimerSettings timerSettings,
        Duration remainingTime
) implements TimedState {
    public SelectionState {
        Objects.requireNonNull(selections, "Selections map cannot be null");
        Objects.requireNonNull(timerSettings, "Settings cannot be null");
        Objects.requireNonNull(remainingTime, "Remaining time cannot be null");

        selections = Map.copyOf(selections);
    }

    /**
     * 기물 선택 상태를 생성합니다.
     *
     * @param selections    선택 현황
     * @param timerSettings 타이머 설정
     * @param remainingTime 남은 시간
     * @return 기물 선택 상태
     */
    @NonNull
    public static SelectionState of(
            @NonNull Map<UUID, UUID> selections,
            @NonNull PhaseTimerSettings timerSettings,
            @NonNull Duration remainingTime
    ) {
        return new SelectionState(selections, timerSettings, remainingTime);
    }

    /**
     * 초기 기물 선택 상태를 생성합니다.
     *
     * @param timerSettings 초기 타이머 설정
     * @return 초기화된 선택 상태
     */
    @NonNull
    public static SelectionState initial(@NonNull PhaseTimerSettings timerSettings) {
        return new SelectionState(Collections.emptyMap(), timerSettings, timerSettings.initialDuration());
    }

    /**
     * 기물을 선택한 새로운 상태를 제공합니다.
     *
     * @param participantId 참가자 ID
     * @param pieceId       선택된 기물 ID
     * @return 업데이트된 상태
     */
    @NonNull
    public SelectionState select(@NonNull UUID participantId, @NonNull UUID pieceId) {
        Map<UUID, UUID> newSelections = new HashMap<>(selections);

        newSelections.put(participantId, pieceId);

        return new SelectionState(newSelections, timerSettings, remainingTime);
    }

    @NonNull
    @Override
    public SelectionState remaining(@NonNull Duration remainingTime) {
        return new SelectionState(selections, timerSettings, remainingTime);
    }

    /**
     * 기물이 이미 선택되었는지 확인합니다.
     *
     * @param pieceId 기물 ID
     * @return 기물 선택 여부
     */
    public boolean isSelected(@NonNull UUID pieceId) {
        return selections.containsValue(pieceId);
    }

    /**
     * 참가자의 기물 선택 완료 여부를 확인합니다.
     *
     * @param participantId 참가자 ID
     * @return 선택 완료 여부
     */
    public boolean hasSelectionFor(@NonNull UUID participantId) {
        return selections.containsKey(participantId);
    }

    /**
     * 주어진 모든 참가자의 기물 선택 완료 여부를 확인합니다.
     *
     * @param participantIds 참가자 ID 목록
     * @return 모두 선택 완료 여부
     */
    public boolean hasSelectionFor(@NonNull Collection<UUID> participantIds) {
        return selections.keySet().containsAll(participantIds);
    }

    @NonNull
    @Override
    public GamePhase phase() {
        return GamePhase.PIECE_SELECTION;
    }
}
