package dev.tecte.chessWar.piece.domain.model;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/**
 * 기물의 설계도를 나타내는 불변 객체입니다.
 *
 * @param type      기물의 종류
 * @param teamColor 팀 색상
 */
public record PieceSpec(PieceType type, TeamColor teamColor) {
    public PieceSpec {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(teamColor, "Team color cannot be null");
    }

    /**
     * 기물의 스펙을 생성하는 정적 팩토리 메서드입니다。
     *
     * @param type      기물의 종류
     * @param teamColor 팀 색상
     * @return 생성된 PieceSpec
     */
    @NonNull
    public static PieceSpec of(@NonNull PieceType type, @NonNull TeamColor teamColor) {
        return new PieceSpec(type, teamColor);
    }

    /**
     * 기물의 종류와 소속 팀 정보가 모두 포함된 상세 표시 이름을 생성합니다.
     * <p>
     * 색상만으로는 팀 구분이 어려운 환경이나 로그 기록 등 정보의 명확성이 최우선인 상황을 위해 사용합니다.
     * {@link PieceType#formattedName(Component)}의 기본 형식에 팀 명칭을 명시적으로 결합한 컴포넌트를 반환합니다.
     *
     * @return 팀 명칭과 색상이 모두 반영된 상세 컴포넌트
     */
    @NonNull
    public Component displayName() {
        return type.formattedName(Component.text(teamColor.getName())).color(teamColor.getTextColor());
    }
}
