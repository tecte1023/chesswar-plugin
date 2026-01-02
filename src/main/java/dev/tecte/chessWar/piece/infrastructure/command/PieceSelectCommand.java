package dev.tecte.chessWar.piece.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.piece.application.PieceSelectionService;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 기물 선택 및 참전에 관한 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT)
@Subcommand("piece")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class PieceSelectCommand extends BaseCommand {
    private final PieceSelectionService pieceSelectionService;
    private final SenderNotifier senderNotifier;

    /**
     * 대상 기물을 선택하여 전장에 참전합니다.
     *
     * @param player  명령어를 실행한 플레이어
     * @param pieceId 참전할 대상 기물의 엔티티 UUID
     */
    @Subcommand("select")
    @Description("대상 기물을 선택하여 전장에 참전합니다.")
    public void selectPiece(@NonNull Player player, @NonNull String pieceId) {
        UUID uuid;

        try {
            uuid = UUID.fromString(pieceId);
        } catch (IllegalArgumentException e) {
            senderNotifier.notifyError(player, "해당 기물을 찾을 수 없습니다.");

            return;
        }

        pieceSelectionService.selectPiece(player, uuid);
    }
}
