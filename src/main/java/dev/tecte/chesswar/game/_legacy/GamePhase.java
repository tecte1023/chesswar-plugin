/*
package dev.tecte.chesswar.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GamePhase {
    WAITING("대기"),
    PIECE_SELECTION("병력 편성"),
    TURN_ORDER("순서 조율"),
    BATTLE("전투"),
    ENDED("종료");

    private final String displayName;

    public GamePhase next() {
        return switch (this) {
            case WAITING -> PIECE_SELECTION;
            case PIECE_SELECTION -> TURN_ORDER;
            case TURN_ORDER -> BATTLE;
            case BATTLE -> ENDED;
            case ENDED -> WAITING;
        };
    }
}
*/
