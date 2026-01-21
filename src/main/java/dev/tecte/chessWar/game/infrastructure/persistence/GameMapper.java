package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.infrastructure.persistence.BoardMapper;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.piece.infrastructure.persistence.PieceMapper;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

import static dev.tecte.chessWar.game.infrastructure.persistence.GamePersistenceConstants.Keys;

/**
 * 게임 객체와 YAML 데이터 간의 변환을 담당하는 매퍼입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameMapper {
    // 기술적 중복 방지와 데이터 일관성 유지를 위해 타 도메인 매퍼를 유틸리티로 활용
    private final BoardMapper boardMapper;
    private final PieceMapper pieceMapper;
    private final YmlParser parser;

    /**
     * 게임 객체를 YML 저장 가능한 맵 형태로 변환합니다.
     *
     * @param entity 변환할 게임 엔티티
     * @return 직렬화된 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull Game entity) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.PIECES, toMapPieces(entity.pieces()));
        map.put(Keys.PHASE, entity.phase().name());
        entity.activeTurn().ifPresent(teamColor -> map.put(Keys.CURRENT_TURN, teamColor.name()));

        return map;
    }

    /**
     * YML 섹션 데이터와 체스판 객체로부터 게임 객체를 복원합니다.
     *
     * @param section 데이터가 담긴 섹션
     * @param board   게임이 진행될 체스판
     * @return 복원된 게임 객체
     */
    @NonNull
    public Game fromSection(@NonNull ConfigurationSection section, @NonNull Board board) {
        Map<Coordinate, UnitPiece> pieces = fromSectionPieces(parser.requireSection(section, Keys.PIECES));
        GamePhase phase = parser.requireEnum(section, Keys.PHASE, GamePhase::from);
        TeamColor currentTurn = parser.findEnum(section, Keys.CURRENT_TURN, TeamColor::from).orElse(null);

        return Game.of(board, pieces, phase, currentTurn);
    }

    @NonNull
    private Map<String, Object> toMapPieces(@NonNull Map<Coordinate, UnitPiece> pieces) {
        Map<String, Object> map = new HashMap<>();

        pieces.forEach((coordinate, piece) ->
                map.put(boardMapper.serializeCoordinate(coordinate), pieceMapper.toMap(piece))
        );

        return map;
    }

    @NonNull
    private Map<Coordinate, UnitPiece> fromSectionPieces(@NonNull ConfigurationSection section) {
        Map<Coordinate, UnitPiece> pieces = new HashMap<>();

        for (String coordinateKey : section.getKeys(false)) {
            Coordinate coordinate = boardMapper.deserializeCoordinate(coordinateKey, section.getCurrentPath());
            UnitPiece piece = pieceMapper.fromSection(parser.requireSection(section, coordinateKey));

            pieces.put(coordinate, piece);
        }

        return pieces;
    }
}
