package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.event.DomainEventDispatcher;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.event.AllParticipantsSelectedEvent;
import dev.tecte.chessWar.game.domain.event.PieceSelectedEvent;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.exception.GameSystemException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceException;
import dev.tecte.chessWar.piece.application.port.PieceInfoRenderer;
import dev.tecte.chessWar.piece.application.port.PieceStatProvider;
import dev.tecte.chessWar.piece.application.port.dto.PieceStatsDto;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.team.application.TeamService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * 기물 선택 단계의 상호작용과 참전 로직을 관리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceSelectionCoordinator {
    private final GameRepository gameRepository;
    private final TeamService teamService;
    private final PieceStatProvider pieceStatProvider;
    private final PieceInfoRenderer pieceInfoRenderer;
    private final DomainEventDispatcher eventDispatcher;

    /**
     * 기물 정보를 표시합니다.
     *
     * @param player 수신자
     * @param target 대상 엔티티
     */
    public void inspectPiece(@NonNull Player player, @NonNull Entity target) {
        gameRepository.find()
                .filter(game -> game.phase() == GamePhase.PIECE_SELECTION)
                .ifPresent(game -> findSelectableUnit(game, player, target.getUniqueId())
                        .ifPresent(unit -> pieceInfoRenderer.renderInfo(
                                player,
                                unit,
                                game.isAlreadySelected(unit.id())
                        )));
    }

    /**
     * 기물을 선택하여 참전합니다.
     *
     * @param player  참가자
     * @param pieceId 기물 ID
     */
    public void selectPiece(@NonNull Player player, @NonNull UUID pieceId) {
        Game game = gameRepository.find().orElseThrow(GameException::notFound);
        Piece piece = game.findPiece(pieceId).orElseThrow(GameException::pieceNotFound);

        game = game.selectPiece(player.getUniqueId(), pieceId);

        try {
            gameRepository.save(game);
        } catch (PersistenceException e) {
            throw GameSystemException.pieceSelectionFailed(pieceId, e);
        }

        applyPieceStats(player, piece.spec());
        eventDispatcher.dispatch(PieceSelectedEvent.of(player.getUniqueId(), pieceId, piece.spec()));

        if (game.hasSelectedPiece(teamService.findAllParticipantIds())) {
            eventDispatcher.dispatch(AllParticipantsSelectedEvent.create());
        }
    }

    /**
     * 선택한 기물의 능력을 플레이어에게 적용합니다.
     *
     * @param player 대상 플레이어
     * @param spec   기물 명세
     */
    public void applyPieceStats(@NonNull Player player, @NonNull PieceSpec spec) {
        PieceStatsDto stats = pieceStatProvider.getStats(spec);
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(stats.maxHealth());
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(stats.attackDamage());
        }

        restoreFullHealth(player);
    }

    private Optional<UnitPiece> findSelectableUnit(Game game, Player player, UUID targetId) {
        return game.findPiece(targetId)
                .filter(piece -> piece instanceof UnitPiece)
                .map(piece -> (UnitPiece) piece)
                .filter(Piece::isSelectable)
                .filter(unit -> isFriendlyPiece(player, unit));
    }

    private void restoreFullHealth(@NonNull Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        }
    }

    private boolean isFriendlyPiece(@NonNull Player player, @NonNull Piece piece) {
        return teamService.findTeam(player)
                .map(piece::isTeam)
                .orElse(false);
    }
}
