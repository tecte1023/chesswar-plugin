package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.exception.GameException;
import dev.tecte.chessWar.game.domain.exception.GameSystemException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceException;
import dev.tecte.chessWar.piece.application.port.PieceStatProvider;
import dev.tecte.chessWar.piece.application.port.dto.PieceStatsDto;
import dev.tecte.chessWar.piece.domain.exception.PieceException;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 기물 선택 및 참전 로직을 처리합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceSelectionService {
    private final PieceStatProvider pieceStatProvider;
    private final GameRepository gameRepository;
    private final SenderNotifier senderNotifier;

    /**
     * 기물을 선택하여 참전합니다.
     *
     * @param player  참전할 플레이어
     * @param pieceId 선택할 기물의 ID
     * @throws GameException  진행 중인 게임이 없거나 게임에 포함되지 않은 기물일 경우
     * @throws PieceException 기물이 선택 불가능하거나 이미 선택된 경우
     */
    @HandleException
    public void selectPiece(@NonNull Player player, @NonNull UUID pieceId) {
        Game game = gameRepository.find().orElseThrow(GameException::notFound);
        Piece piece = game.findPiece(pieceId).orElseThrow(GameException::pieceNotFound);
        PieceSpec spec = piece.spec();
        PieceType type = spec.type();

        if (!piece.isSelectable()) {
            throw PieceException.cannotSelectPawn();
        }

        if (game.isPieceSelected(pieceId)) {
            throw PieceException.alreadySelected();
        }

        try {
            gameRepository.save(game.selectPiece(player.getUniqueId(), pieceId));
        } catch (PersistenceException e) {
            throw GameSystemException.pieceSelectionFailed(pieceId, e);
        }

        applyStats(player, pieceStatProvider.getStats(spec));
        restoreFullHealth(player);
        senderNotifier.notifySuccess(
                player,
                Component.text()
                        .append(type.formattedName().color(NamedTextColor.GOLD))
                        .append(Component.text(" 기물로 전장에 참전합니다."))
                        .build()
        );
    }

    private void applyStats(@NonNull Player player, @NonNull PieceStatsDto stats) {
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance damageAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (healthAttr != null) {
            healthAttr.setBaseValue(stats.maxHealth());
        }

        if (damageAttr != null) {
            damageAttr.setBaseValue(stats.attackDamage());
        }
    }

    private void restoreFullHealth(@NonNull Player player) {
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);

        if (healthAttr != null) {
            player.setHealth(healthAttr.getValue());
        }
    }
}
