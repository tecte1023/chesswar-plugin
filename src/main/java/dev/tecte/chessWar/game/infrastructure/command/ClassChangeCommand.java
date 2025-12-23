package dev.tecte.chessWar.game.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.game.application.PieceClassService;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 게임 내 플레이어의 전직(Class Change) 명령어를 처리합니다.
 */
@CommandAlias(CommandConstants.ROOT)
@Subcommand("class")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class ClassChangeCommand extends BaseCommand {
    private final PieceClassService pieceClassService;
    private final SenderNotifier senderNotifier;

    /**
     * 자기 자신을 지정된 기물 클래스로 전직시킵니다.
     *
     * @param player  명령어를 실행한 플레이어
     * @param pieceId 대상 기물의 엔티티 UUID 문자열
     */
    @Subcommand("select")
    @Description("자신을 지정된 기물로 전직시킵니다.")
    @CommandCompletion("@nothing")
    public void changeClass(@NonNull Player player, @NonNull String pieceId) {
        UUID uuid;

        try {
            uuid = UUID.fromString(pieceId);
        } catch (IllegalArgumentException e) {
            senderNotifier.notifyError(player, "해당 기물을 찾을 수 없습니다.");

            return;
        }

        pieceClassService.changeClass(player, uuid);
    }
}
