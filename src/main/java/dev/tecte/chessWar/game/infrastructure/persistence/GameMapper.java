package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.phase.BattleState;
import dev.tecte.chessWar.game.domain.model.phase.EndedState;
import dev.tecte.chessWar.game.domain.model.phase.PhaseState;
import dev.tecte.chessWar.game.domain.model.phase.SelectionState;
import dev.tecte.chessWar.game.domain.model.phase.TurnOrderState;
import dev.tecte.chessWar.game.infrastructure.persistence.GamePersistenceConstants.Keys;
import dev.tecte.chessWar.infrastructure.persistence.YmlParser;
import dev.tecte.chessWar.infrastructure.persistence.exception.YmlMappingException;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.infrastructure.persistence.PieceMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 게임 객체와 YAML 데이터 간 변환을 수행합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameMapper {
    // 기술적 중복 방지와 데이터 일관성 유지를 위해 타 도메인 매퍼를 유틸리티로 활용
    private final PieceMapper pieceMapper;
    private final YmlParser parser;

    /**
     * 게임을 YAML 데이터 맵으로 변환합니다.
     *
     * @param game 변환할 게임
     * @return YAML 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull Game game) {
        Map<String, Object> stateMap = new HashMap<>();

        stateMap.put(Keys.PIECES, toMapPieces(game.pieces()));
        stateMap.put(Keys.PHASE, toMapPhaseState(game.state()));

        return Map.of(Keys.STATE, stateMap);
    }

    /**
     * YAML 데이터로부터 게임을 복원합니다.
     *
     * @param section 데이터 섹션
     * @param board   게임이 진행될 체스판
     * @return 복원된 게임
     */
    @NonNull
    public Game fromSection(@NonNull ConfigurationSection section, @NonNull Board board) {
        ConfigurationSection stateSection = parser.requireSection(section, Keys.STATE);
        Map<Coordinate, Piece> pieces = fromSectionPieces(parser.requireSection(stateSection, Keys.PIECES));
        PhaseState state = fromSectionPhaseState(parser.requireSection(stateSection, Keys.PHASE));

        return Game.of(board, pieces, state);
    }

    @NonNull
    private Map<String, Object> toMapPieces(@NonNull Map<Coordinate, Piece> pieces) {
        Map<String, Object> map = new HashMap<>();

        pieces.forEach((coordinate, piece) -> map.put(coordinate.toNotation(), pieceMapper.toMap(piece)));

        return map;
    }

    @NonNull
    private Map<String, Object> toMapPhaseState(@NonNull PhaseState state) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.CURRENT, state.phase().name());

        switch (state) {
            case SelectionState s -> map.put(Keys.SELECTIONS, toMapSelections(s));
            case TurnOrderState ignored -> {
            }
            case BattleState ignored -> {
            }
            case EndedState ignored -> {
            }
        }

        return map;
    }

    @NonNull
    private Map<String, String> toMapSelections(@NonNull SelectionState state) {
        Map<String, String> map = new HashMap<>();

        state.selections().forEach((playerId, pieceId) -> map.put(playerId.toString(), pieceId.toString()));

        return map;
    }

    @NonNull
    private Map<Coordinate, Piece> fromSectionPieces(@NonNull ConfigurationSection section) {
        Map<Coordinate, Piece> pieces = new HashMap<>();

        for (String key : section.getKeys(false)) {
            Coordinate coordinate;

            try {
                coordinate = Coordinate.from(key);
            } catch (IllegalArgumentException e) {
                throw YmlMappingException.forInvalidFormat(key, section.getCurrentPath(), Coordinate.NOTATION_RANGE, e);
            }

            Piece piece = pieceMapper.fromSection(parser.requireSection(section, key));

            pieces.put(coordinate, piece);
        }

        return pieces;
    }

    @NonNull
    private PhaseState fromSectionPhaseState(@NonNull ConfigurationSection section) {
        GamePhase phase = parser.requireEnum(section, Keys.CURRENT, GamePhase::from);

        return switch (phase) {
            case PIECE_SELECTION -> fromSectionSelectionState(section);
            case TURN_ORDER_SELECTION -> new TurnOrderState();
            case BATTLE -> new BattleState();
            case ENDED -> new EndedState();
        };
    }

    @NonNull
    private SelectionState fromSectionSelectionState(@NonNull ConfigurationSection section) {
        return parser.findSection(section, Keys.SELECTIONS)
                .map(this::createSelectionState)
                .orElseGet(SelectionState::empty);
    }

    @NonNull
    private SelectionState createSelectionState(@NonNull ConfigurationSection section) {
        Map<UUID, UUID> selections = new HashMap<>();

        for (String key : section.getKeys(false)) {
            UUID playerId = parser.parseKeyAsUUID(section, key);
            UUID pieceId = parser.requireUUID(section, key);

            selections.put(playerId, pieceId);
        }

        return SelectionState.of(selections);
    }
}
