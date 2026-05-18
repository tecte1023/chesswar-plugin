package dev.tecte.chesswar.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum Team {
    // WHY: 기획서에 명시된 팀 고유의 논리 규칙을 데이터 주도로 통합 관리
    WHITE("백팀", NamedTextColor.WHITE, 1, -5),
    BLACK("흑팀", NamedTextColor.DARK_GRAY, -1, 12);

    private final String displayName;
    private final NamedTextColor textColor;
    private final int forwardMultiplier;
    private final int campRankOffset;

    public Team opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
