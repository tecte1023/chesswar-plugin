package dev.tecte.chessWar.piece.infrastructure.persistence;

import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.piece.domain.model.HeroPiece;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.PieceRole;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.piece.infrastructure.persistence.PiecePersistenceConstants.Keys;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 기물 객체와 YAML 데이터 간 변환을 수행합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceMapper {
    private final YmlParser parser;

    /**
     * 기물을 YAML 데이터 맵으로 변환합니다.
     *
     * @param piece 변환할 기물
     * @return YAML 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull Piece piece) {
        Map<String, Object> map = new HashMap<>();
        PieceSpec spec = piece.spec();

        map.put(Keys.ID, piece.id().toString());
        map.put(Keys.TYPE, spec.type().name());
        map.put(Keys.TEAM, spec.teamColor().name());
        map.put(Keys.ROLE, piece.role().name());

        return map;
    }

    /**
     * YAML 데이터로부터 기물을 복원합니다.
     *
     * @param section 데이터 섹션
     * @return 복원된 기물
     */
    @NonNull
    public Piece fromSection(@NonNull final ConfigurationSection section) {
        UUID id = parser.requireUUID(section, Keys.ID);
        PieceType type = parser.requireEnum(section, Keys.TYPE, PieceType::from);
        TeamColor teamColor = parser.requireEnum(section, Keys.TEAM, TeamColor::from);
        PieceSpec spec = PieceSpec.of(type, teamColor);
        PieceRole role = parser.requireEnum(section, Keys.ROLE, PieceRole::from);

        return switch (role) {
            case HERO -> HeroPiece.of(id, spec);
            case UNIT -> UnitPiece.of(id, spec);
        };
    }
}
