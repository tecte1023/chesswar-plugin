package dev.tecte.chessWar.game.infrastructure.persistence;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.game.domain.model.PhaseTimerSettings;
import dev.tecte.chessWar.game.domain.model.phase.BattleState;
import dev.tecte.chessWar.game.domain.model.phase.EndedState;
import dev.tecte.chessWar.game.domain.model.phase.PhaseState;
import dev.tecte.chessWar.game.domain.model.phase.SelectionState;
import dev.tecte.chessWar.game.domain.model.phase.SetupState;
import dev.tecte.chessWar.game.domain.model.phase.TimedState;
import dev.tecte.chessWar.game.domain.model.phase.TurnOrderState;
import dev.tecte.chessWar.game.domain.policy.GamePhaseTimerPolicy;
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

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 게임과 YAML 데이터 간의 변환을 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameMapper {
    private final GamePhaseTimerPolicy timerPolicy;
    // 데이터 일관성 유지를 위해 타 도메인 매퍼를 유틸리티로 활용
    private final PieceMapper pieceMapper;
    private final YmlParser parser;

    /**
     * 게임을 데이터 맵으로 변환합니다.
     *
     * @param game 게임
     * @return 데이터 맵
     */
    @NonNull
    public Map<String, Object> toMap(@NonNull Game game) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.PIECES, toMapPieces(game.pieces()));
        map.put(Keys.PHASE, toMapPhaseState(game.state()));

        return map;
    }

    /**
     * 데이터 섹션으로부터 게임을 복원합니다.
     *
     * @param section 데이터 섹션
     * @param board   체스판
     * @return 게임
     */
    @NonNull
    public Game fromSection(@NonNull ConfigurationSection section, @NonNull Board board) {
        Map<Coordinate, Piece> pieces = fromSectionPieces(parser.requireSection(section, Keys.PIECES));
        PhaseState state = fromSectionPhaseState(parser.requireSection(section, Keys.PHASE));

        return Game.of(board, pieces, state);
    }

    @NonNull
    private Map<String, Object> toMapPieces(@NonNull Map<Coordinate, Piece> pieces) {
        Map<String, Object> map = new HashMap<>();

        pieces.forEach((coordinate, piece) ->
                map.put(coordinate.toNotation(), pieceMapper.toMap(piece)));

        return map;
    }

    @NonNull
    private Map<String, Object> toMapPhaseState(@NonNull PhaseState state) {
        Map<String, Object> map = new HashMap<>();

        map.put(Keys.CURRENT, state.phase().name());

        if (state instanceof TimedState timedState) {
            map.put(Keys.REMAINING_TIME, timedState.remainingTime().toString());
        }

        switch (state) {
            case SelectionState selection -> map.put(Keys.SELECTIONS, toMapSelections(selection));
            case SetupState ignored -> {
            }
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

        state.selections().forEach((playerId, pieceId) ->
                map.put(playerId.toString(), pieceId.toString()));

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
                throw YmlMappingException.forInvalidFormat(
                        key,
                        section.getCurrentPath(),
                        Coordinate.NOTATION_RANGE,
                        e
                );
            }

            Piece piece = pieceMapper.fromSection(parser.requireSection(section, key));

            pieces.put(coordinate, piece);
        }

        return pieces;
    }

    @NonNull
    private PhaseState fromSectionPhaseState(@NonNull ConfigurationSection section) {
        GamePhase phase = parser.requireEnum(section, Keys.CURRENT, GamePhase::from);
        Optional<Duration> remainingTime = parser.findValue(section, Keys.REMAINING_TIME, String.class)
                .map(Duration::parse);

        return switch (phase) {
            case SETUP -> SetupState.initial();
            case PIECE_SELECTION -> {
                PhaseTimerSettings timerSettings = requireTimerSettings(phase, section);
                Duration time = remainingTime.orElse(timerSettings.duration());

                yield fromSectionSelectionState(section, timerSettings, time);
            }
            case TURN_ORDER_SELECTION -> new TurnOrderState();
            case BATTLE -> new BattleState();
            case ENDED -> new EndedState();
        };
    }

    @NonNull
    private PhaseTimerSettings requireTimerSettings(GamePhase phase, ConfigurationSection section) {
        return timerPolicy.findSettings(phase)
                .orElseThrow(() -> YmlMappingException.forMissingPhaseSetting(
                        phase,
                        PhaseTimerSettings.class,
                        section.getCurrentPath()
                ));
    }

    @NonNull
    private SelectionState fromSectionSelectionState(
            @NonNull ConfigurationSection section,
            @NonNull PhaseTimerSettings timerSettings,
            @NonNull Duration remainingTime
    ) {
        Map<UUID, UUID> selections = parser.findSection(section, Keys.SELECTIONS)
                .map(this::parseSelectionMap)
                .orElseGet(Collections::emptyMap);

        return SelectionState.of(selections, timerSettings, remainingTime);
    }

    @NonNull
    private Map<UUID, UUID> parseSelectionMap(@NonNull ConfigurationSection section) {
        Map<UUID, UUID> selections = new HashMap<>();

        section.getKeys(false).forEach(key -> {
            UUID playerId = parser.parseKeyAsUUID(section, key);
            UUID pieceId = parser.requireUUID(section, key);

            selections.put(playerId, pieceId);
        });

        return selections;
    }
}
