package dev.tecte.chessWar.game.domain.model;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/**
 * 체스 기물의 설계도를 나타내는 불변 객체입니다.
 *
 * @param type      기물의 종류
 * @param teamColor 팀 색상
 * @param mobId     해당 기물에 매핑되는 Mythic Mob의 ID
 */
public record PieceSpec(
        PieceType type,
        TeamColor teamColor,
        String mobId
) {
    public PieceSpec {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(teamColor, "Team color cannot be null");
        Objects.requireNonNull(mobId, "Mob id cannot be null");
    }

    /**
     * 기물의 스펙을 생성하는 정적 팩토리 메서드입니다.
     *
     * @param type      기물의 종류
     * @param teamColor 팀 색상
     * @param mobId     Mythic Mob ID
     * @return 생성된 PieceSpec
     */
    @NonNull
    public static PieceSpec of(@NonNull PieceType type, @NonNull TeamColor teamColor, @NonNull String mobId) {
        return new PieceSpec(type, teamColor, mobId);
    }

    /**
     * 기물의 표시 이름을 스타일이 적용된 {@link Component}로 반환합니다.
     *
     * @return 스타일이 적용된 {@link Component}
     */
    @NonNull
    public Component displayName() {
        return Component.text(teamColor.getName())
                .appendSpace()
                .append(Component.text(type.getDisplayName()))
                .color(teamColor.getTextColor());
    }
}
