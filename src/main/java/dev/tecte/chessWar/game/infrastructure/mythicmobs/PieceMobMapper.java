package dev.tecte.chessWar.game.infrastructure.mythicmobs;

import dev.tecte.chessWar.game.domain.model.PieceType;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

/**
 * 도메인 모델(TeamColor, PieceType)을 MythicMob ID 문자열로 변환합니다.
 * 이 클래스는 자바 코드와 MythicMobs 설정 간의 명시적 매핑을 담당합니다.
 */
@Singleton
public class PieceMobMapper {
    private static final Map<PieceIdentifier, String> MOB_ID_MAP = Map.ofEntries(
            entry(new PieceIdentifier(TeamColor.WHITE, PieceType.KING), "WhiteKing"),
            entry(new PieceIdentifier(TeamColor.WHITE, PieceType.QUEEN), "WhiteQueen"),
            entry(new PieceIdentifier(TeamColor.WHITE, PieceType.ROOK), "WhiteRook"),
            entry(new PieceIdentifier(TeamColor.WHITE, PieceType.BISHOP), "WhiteBishop"),
            entry(new PieceIdentifier(TeamColor.WHITE, PieceType.KNIGHT), "WhiteKnight"),
            entry(new PieceIdentifier(TeamColor.WHITE, PieceType.PAWN), "WhitePawn"),

            entry(new PieceIdentifier(TeamColor.BLACK, PieceType.KING), "BlackKing"),
            entry(new PieceIdentifier(TeamColor.BLACK, PieceType.QUEEN), "BlackQueen"),
            entry(new PieceIdentifier(TeamColor.BLACK, PieceType.ROOK), "BlackRook"),
            entry(new PieceIdentifier(TeamColor.BLACK, PieceType.BISHOP), "BlackBishop"),
            entry(new PieceIdentifier(TeamColor.BLACK, PieceType.KNIGHT), "BlackKnight"),
            entry(new PieceIdentifier(TeamColor.BLACK, PieceType.PAWN), "BlackPawn")
    );

    /**
     * 팀 색상과 기물 종류에 해당하는 MythicMob ID를 반환합니다.
     *
     * @param teamColor 팀 색상
     * @param pieceType 기물 종류
     * @return 매핑된 MythicMob ID 문자열
     * @throws IllegalStateException 매핑되는 ID가 맵에 정의되지 않았을 경우
     */
    @NonNull
    public String getMobId(@NonNull TeamColor teamColor, @NonNull PieceType pieceType) {
        return Optional.ofNullable(MOB_ID_MAP.get(new PieceIdentifier(teamColor, pieceType)))
                .orElseThrow(() -> new IllegalStateException(
                        String.format("MythicMob ID for '%s %s' is not defined in PieceMobMapper.", teamColor, pieceType)
                ));
    }

    private record PieceIdentifier(TeamColor teamColor, PieceType pieceType) {
    }
}
