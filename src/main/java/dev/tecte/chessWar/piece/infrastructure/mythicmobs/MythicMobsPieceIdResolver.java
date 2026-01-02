package dev.tecte.chessWar.piece.infrastructure.mythicmobs;

import dev.tecte.chessWar.piece.application.port.PieceIdResolver;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Singleton;
import lombok.NonNull;

/**
 * MythicMobs 플러그인의 명명 규칙을 기반으로 기물 ID를 해결하는 구현체입니다.
 * <p>
 * 별도의 매핑 설정 없이 <b>[Team][Type]</b> 형식의 PascalCase 규칙을 따릅니다.
 * 예: WHITE 팀의 PAWN -> "WhitePawn"
 */
@Singleton
public class MythicMobsPieceIdResolver implements PieceIdResolver {
    @NonNull
    @Override
    public String resolveId(@NonNull TeamColor teamColor, @NonNull PieceType type) {
        return toPascalCase(teamColor.name()) + toPascalCase(type.name());
    }

    private String toPascalCase(@NonNull String source) {
        if (source.length() < 2) {
            return source.toUpperCase();
        }

        return source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase();
    }
}
