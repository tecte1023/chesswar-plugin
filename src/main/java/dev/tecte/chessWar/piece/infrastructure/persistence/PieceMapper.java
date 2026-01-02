package dev.tecte.chessWar.piece.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.port.persistence.SingleYmlMapper;
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
public class PieceMapper implements SingleYmlMapper<UnitPiece> {
    private final YmlParser parser;

    @NonNull
    @Override
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

    @NonNull
    @Override
    public UnitPiece fromSection(@NonNull ConfigurationSection section) {
        UUID entityId = parser.requireUUID(section, Keys.ENTITY_ID);
        PieceType type = parser.requireEnum(section, Keys.PIECE_TYPE, PieceType::from);
        TeamColor teamColor = parser.requireEnum(section, Keys.TEAM_COLOR, TeamColor::from);
        UUID playerId = parser.findUUID(section, Keys.PLAYER_ID).orElse(null);

        return UnitPiece.of(entityId, PieceSpec.of(type, teamColor), playerId);
    }
}
