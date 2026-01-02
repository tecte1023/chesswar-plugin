package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.infrastructure.persistence.BoardMapper;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.piece.infrastructure.persistence.PieceMapper;
import dev.tecte.chessWar.port.persistence.SingleYmlMapper;
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
 * <p>
 * 전체적인 게임 데이터를 YAML 형식으로 직렬화하거나 섹션으로부터 복원합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameMapper implements SingleYmlMapper<Game> {
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final PieceMapper pieceMapper;
    private final YmlParser parser;

    @NonNull
    @Override
    public Map<String, Object> toMap(@NonNull Game entity) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.PIECES, toMapPieces(entity.pieces()));
        map.put(Keys.PHASE, entity.phase().name());
        entity.activeTurn().ifPresent(teamColor -> map.put(Keys.CURRENT_TURN, teamColor.name()));

        return map;
    }

    @NonNull
    @Override
    public Game fromSection(@NonNull ConfigurationSection section) {
        Board board = boardRepository.find().orElseThrow(YmlMappingException::forMissingBoard);
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
