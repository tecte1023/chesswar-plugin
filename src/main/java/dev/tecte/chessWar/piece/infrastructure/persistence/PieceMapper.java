package dev.tecte.chessWar.piece.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.tecte.chessWar.piece.infrastructure.persistence.PiecePersistenceConstants.Keys;

/**
 * 기물 객체와 YAML 데이터 간의 변환을 담당하는 매퍼입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceMapper {
    private final YmlParser parser;

    /**
     * 기물 객체를 YML 저장 가능한 맵 형태로 변환합니다.
     *
     * @param piece 변환할 기물 객체
     * @return 직렬화된 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull UnitPiece piece) {
        Map<String, Object> map = new HashMap<>();
        PieceSpec pieceSpec = piece.spec();

        map.put(Keys.ENTITY_ID, piece.entityId().toString());
        map.put(Keys.PIECE_TYPE, pieceSpec.type().name());
        map.put(Keys.TEAM_COLOR, pieceSpec.teamColor().name());

        if (piece.playerId() != null) {
            map.put(Keys.PLAYER_ID, piece.playerId().toString());
        }

        return map;
    }

    /**
     * YML 섹션 데이터로부터 기물 객체를 복원합니다.
     *
     * @param section 데이터가 담긴 섹션
     * @return 복원된 기물 객체
     */
    @NonNull
    public UnitPiece fromSection(@NonNull ConfigurationSection section) {
        UUID entityId = parser.requireUUID(section, Keys.ENTITY_ID);
        PieceType type = parser.requireEnum(section, Keys.PIECE_TYPE, PieceType::from);
        TeamColor teamColor = parser.requireEnum(section, Keys.TEAM_COLOR, TeamColor::from);
        UUID playerId = parser.findUUID(section, Keys.PLAYER_ID).orElse(null);

        return UnitPiece.of(entityId, PieceSpec.of(type, teamColor), playerId);
    }
}
