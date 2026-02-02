package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/**
 * 체스판 관련 알림 및 안내 메시지를 전송합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardNotifier {
    private final SenderNotifier senderNotifier;

    /**
     * 체스판 생성 성공을 알립니다.
     *
     * @param recipient 알림을 받을 대상
     */
    public void notifyBoardCreate(@NonNull CommandSender recipient) {
        senderNotifier.notifySuccess(recipient, Component.text("체스판이 생성되었습니다.", NamedTextColor.WHITE));
    }
}
