package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.application.port.BoardRepository;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.infrastructure.persistence.BoardMapper;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.Piece;
import dev.tecte.chessWar.game.domain.model.PieceSpec;
import dev.tecte.chessWar.game.domain.model.PieceType;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
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

import static dev.tecte.chessWar.game.infrastructure.persistence.GamePersistenceConstants.Keys;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameMapper implements SingleYmlMapper<Game> {
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
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
        ConfigurationSection piecesSection = parser.requireSection(section, Keys.PIECES);
        Map<Coordinate, Piece> pieces = fromSectionPieces(piecesSection);
        GamePhase phase = parser.requireEnum(section, Keys.PHASE, GamePhase::from);
        TeamColor currentTurn = parser.findEnum(section, Keys.CURRENT_TURN, TeamColor::from).orElse(null);

        return Game.of(board, pieces, phase, currentTurn);
    }

    @NonNull
    private Map<String, Object> toMapPieces(@NonNull Map<Coordinate, Piece> pieces) {
        Map<String, Object> map = new HashMap<>();

        for (var entry : pieces.entrySet()) {
            String coordinateKey = boardMapper.serializeCoordinate(entry.getKey());
            Map<String, Object> pieceMap = toMapPiece(entry.getValue());

            map.put(coordinateKey, pieceMap);
        }

        return map;
    }

    @NonNull
    private Map<Coordinate, Piece> fromSectionPieces(@NonNull ConfigurationSection section) {
        Map<Coordinate, Piece> pieces = new HashMap<>();

        for (String coordinateKey : section.getKeys(false)) {
            ConfigurationSection pieceSection = parser.requireSection(section, coordinateKey);
            Coordinate coordinate = boardMapper.deserializeCoordinate(coordinateKey, section.getCurrentPath());
            Piece piece = fromSectionPiece(pieceSection);

            pieces.put(coordinate, piece);
        }

        return pieces;
    }

    @NonNull
    private Map<String, Object> toMapPiece(@NonNull Piece piece) {
        Map<String, Object> map = new HashMap<>();
        PieceSpec pieceSpec = piece.spec();

        map.put(Keys.ENTITY_ID, piece.entityId().toString());
        map.put(Keys.PIECE_TYPE, pieceSpec.type().name());
        map.put(Keys.TEAM_COLOR, pieceSpec.teamColor().name());
        map.put(Keys.MOB_ID, pieceSpec.mobId());

        if (piece.playerId() != null) {
            map.put(Keys.PLAYER_ID, piece.playerId().toString());
        }

        return map;
    }

    @NonNull
    private Piece fromSectionPiece(@NonNull ConfigurationSection section) {
        UUID entityId = parser.requireUUID(section, Keys.ENTITY_ID);
        PieceType type = parser.requireEnum(section, Keys.PIECE_TYPE, PieceType::from);
        TeamColor teamColor = parser.requireEnum(section, Keys.TEAM_COLOR, TeamColor::from);
        String mobId = parser.requireValue(section, Keys.MOB_ID, String.class);
        UUID playerId = parser.findUUID(section, Keys.PLAYER_ID).orElse(null);

        return Piece.of(entityId, PieceSpec.of(type, teamColor, mobId), playerId);
    }
}
