package dev.tecte.chesswar.game.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GamePhase {
    WAITING("대기"),
    PIECE_SELECTION("기물 선택"),
    TURN_ORDER("순서 조율"),
    BATTLE("전투"),
    ENDED("종료");

    private final String displayName;
}
