package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.PieceStatProvider;
import dev.tecte.chessWar.game.application.port.dto.PieceStatsDto;
import dev.tecte.chessWar.game.domain.exception.GameNotFoundException;
import dev.tecte.chessWar.game.domain.exception.PieceSelectionException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.UnitPiece;
import dev.tecte.chessWar.game.domain.model.PieceSpec;
import dev.tecte.chessWar.game.domain.model.PieceType;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 게임 진행 중 플레이어의 기물 선택과 관련된 비즈니스 로직을 처리하는 서비스입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceSelectionService {
    private final PieceStatProvider pieceStatProvider;
    private final GameRepository gameRepository;
    private final SenderNotifier senderNotifier;

    /**
     * 플레이어가 선택한 기물로 게임에 참전 처리합니다.
     * <p>
     * 참전 시 해당 기물의 스펙에 맞춰 플레이어의 능력치가 조정되며, 체력이 최대치로 회복됩니다.
     * <p>
     * <b>제약 사항:</b>
     * <ul>
     *   <li>폰은 플레이어가 직접 선택할 수 없습니다.</li>
     *   <li>이미 다른 플레이어가 참전 중인 기물은 선택할 수 없습니다.</li>
     * </ul>
     *
     * @param player        참전할 플레이어
     * @param targetPieceId 대상 기물의 식별자 UUID
     * @throws GameNotFoundException   진행 중인 게임이 없을 경우
     * @throws PieceSelectionException 기물을 찾을 수 없거나 참전 제약 사항을 위반했을 경우
     */
    @HandleException
    public void selectPiece(@NonNull Player player, @NonNull UUID targetPieceId) {
        Game game = gameRepository.find().orElseThrow(GameNotFoundException::noGameInProgress);
        UnitPiece piece = game.findPiece(targetPieceId).orElseThrow(PieceSelectionException::pieceNotFound);
        PieceSpec spec = piece.spec();
        PieceType type = spec.type();

        if (type == PieceType.PAWN) {
            throw PieceSelectionException.pawnIsNotSelectable();
        }

        if (piece.isSelected()) {
            throw PieceSelectionException.alreadySelected();
        }

        gameRepository.save(game.updatePiece(piece.selectedBy(player.getUniqueId())));
        applyStats(player, pieceStatProvider.getStats(spec.mobId()));
        restoreFullHealth(player);
        senderNotifier.notifySuccess(
                player,
                Component.text()
                        .append(Component.text()
                                .color(NamedTextColor.GOLD)
                                .append(Component.text("[ "))
                                .append(Component.text(type.getSymbol() + " "))
                                .append(Component.text(type.getDisplayName()).decorate(TextDecoration.BOLD))
                                .append(Component.text(" ]")))
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
