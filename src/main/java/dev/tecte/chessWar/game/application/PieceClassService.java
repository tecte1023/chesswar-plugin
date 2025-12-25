package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.PieceStatProvider;
import dev.tecte.chessWar.game.application.port.dto.PieceStatsDto;
import dev.tecte.chessWar.game.domain.exception.GameNotFoundException;
import dev.tecte.chessWar.game.domain.exception.PieceClassException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.Piece;
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
 * 게임 진행 중 플레이어의 전직(Class Change)과 관련된 비즈니스 로직을 처리하는 서비스입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceClassService {
    private final PieceStatProvider pieceStatProvider;
    private final GameRepository gameRepository;
    private final SenderNotifier senderNotifier;

    /**
     * 플레이어를 특정 직업으로 전직시킵니다.
     * <p>
     * 해당 기물의 정의에서 체력과 공격력 스탯을 가져와 플레이어에게 적용합니다.
     * 폰은 장교가 될 수 없으므로 전직이 제한됩니다.
     * 또한 이미 다른 플레이어가 선택한 기물은 선택할 수 없습니다.
     *
     * @param player        전직할 플레이어
     * @param targetPieceId 대상 기물의 식별자 UUID
     * @throws BusinessException 게임이 없거나, 기물을 찾을 수 없거나, 잘못된 전직 대상일 경우
     */
    @HandleException
    public void changeClass(@NonNull Player player, @NonNull UUID targetPieceId) {
        Game game = gameRepository.find().orElseThrow(GameNotFoundException::noGameInProgress);
        Piece piece = game.findPiece(targetPieceId).orElseThrow(PieceClassException::pieceNotFound);
        PieceSpec spec = piece.spec();
        PieceType type = spec.type();

        if (type == PieceType.PAWN) {
            throw PieceClassException.pawnIsNotSelectable();
        }

        if (piece.isSelected()) {
            throw PieceClassException.alreadySelected();
        }

        Piece selectedPiece = piece.selectedBy(player.getUniqueId());

        gameRepository.save(game.updatePiece(selectedPiece));
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
