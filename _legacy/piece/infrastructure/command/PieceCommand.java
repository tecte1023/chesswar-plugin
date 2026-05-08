package dev.tecte.chessWar.piece.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.application.PieceSelectionCoordinator;
import dev.tecte.chessWar.infrastructure.command.CommandRouting;
import dev.tecte.chessWar.piece.domain.exception.PieceSystemException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 기물 관련 명령어를 처리합니다.
 */
@Singleton
@CommandAlias(CommandRouting.ROOT_ALIAS)
@Subcommand(PieceCommandRouting.PIECE)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class PieceCommand extends BaseCommand {
    private final PieceSelectionCoordinator pieceSelectionCoordinator;

    /**
     * 플레이어가 대상 기물이 되어 전장에 참전합니다.
     *
     * @param player  행위자
     * @param pieceId 참전할 기물 ID
     */
    @Subcommand(PieceCommandRouting.SELECT)
    @Private
    @HandleException
    public void select(@NonNull Player player, @NonNull String pieceId) {
        UUID uuid;

        try {
            uuid = UUID.fromString(pieceId);
        } catch (IllegalArgumentException e) {
            throw PieceSystemException.invalidId(pieceId, e);
        }

        pieceSelectionCoordinator.selectPiece(player, uuid);
    }
}
