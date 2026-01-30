package dev.tecte.chessWar.piece.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.game.application.PieceSelectionService;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 기물 관련 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT_ALIAS)
@Subcommand(PieceCommandConstants.PIECE)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class PieceSelectCommand extends BaseCommand {
    private final PieceSelectionService pieceSelectionService;
    private final SenderNotifier senderNotifier;

    /**
     * 플레이어가 대상 기물이 되어 전장에 참전합니다.
     *
     * @param player  명령어를 실행한 플레이어
     * @param pieceId 참전할 대상 기물의 식별자
     */
    @Subcommand(PieceCommandConstants.SELECT)
    @Description("플레이어가 대상 기물이 되어 전장에 참전합니다.")
    public void select(@NonNull Player player, @NonNull String pieceId) {
        UUID uuid;

        try {
            uuid = UUID.fromString(pieceId);
        } catch (IllegalArgumentException e) {
            senderNotifier.notifyError(player, "입력하신 ID가 올바르지 않습니다.");

            return;
        }

        pieceSelectionService.selectPiece(player, uuid);
    }
}
