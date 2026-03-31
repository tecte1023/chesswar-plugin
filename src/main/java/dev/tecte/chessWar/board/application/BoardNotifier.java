package dev.tecte.chessWar.board.application;

import dev.tecte.chessWar.port.UserNotifier;
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
    private final UserNotifier userNotifier;

    /**
     * 체스판 생성 성공을 알립니다.
     *
     * @param recipient 수신자
     */
    public void informCreated(@NonNull CommandSender recipient) {
        userNotifier.informSuccess(recipient, Component.text("체스판이 생성되었습니다.", NamedTextColor.WHITE));
    }
}
