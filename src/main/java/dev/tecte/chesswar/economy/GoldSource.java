package dev.tecte.chesswar.economy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * [순수 데이터 객체] 재화 획득 경로 정의.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GoldSource {
    SALARY("월급"),
    KILL("처치 보상"),
    TAX("세금"),
    STEAL("탈취");

    private final String description;
}
