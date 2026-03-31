package dev.tecte.chessWar.port;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * 사용자에게 안내 및 알림 메시지를 전송합니다.
 */
public interface UserNotifier {
    /**
     * 안내 메시지를 전송합니다.
     *
     * @param recipientId 수신자 ID
     * @param message     메시지
     */
    void inform(@NonNull UUID recipientId, @NonNull Component message);

    /**
     * 안내 메시지를 전송합니다.
     *
     * @param recipient 수신자
     * @param message   메시지
     */
    void inform(@NonNull CommandSender recipient, @NonNull Component message);

    /**
     * 성공 메시지를 전송합니다.
     *
     * @param recipientId 수신자 ID
     * @param message     메시지
     */
    void informSuccess(@NonNull UUID recipientId, @NonNull Component message);

    /**
     * 성공 메시지를 전송합니다.
     *
     * @param recipient 수신자
     * @param message   메시지
     */
    void informSuccess(@NonNull CommandSender recipient, @NonNull Component message);

    /**
     * 오류 메시지를 전송합니다.
     *
     * @param recipientId 수신자 ID
     * @param message     메시지
     */
    void informError(@NonNull UUID recipientId, @NonNull Component message);

    /**
     * 오류 메시지를 전송합니다.
     *
     * @param recipient 수신자
     * @param message   메시지
     */
    void informError(@NonNull CommandSender recipient, @NonNull Component message);

    /**
     * 타이틀을 표시합니다.
     *
     * @param recipientId 수신자 ID
     * @param title       메인 타이틀
     */
    void displayTitle(@NonNull UUID recipientId, @NonNull Component title);

    /**
     * 타이틀을 표시합니다.
     *
     * @param recipient 수신자
     * @param title     메인 타이틀
     */
    void displayTitle(@NonNull CommandSender recipient, @NonNull Component title);

    /**
     * 타이틀과 서브 타이틀을 함께 표시합니다.
     *
     * @param recipientId 수신자 ID
     * @param title       메인 타이틀
     * @param subtitle    서브 타이틀
     */
    void displayTitle(
            @NonNull UUID recipientId,
            @NonNull Component title,
            @NonNull Component subtitle
    );

    /**
     * 타이틀과 서브 타이틀을 함께 표시합니다.
     *
     * @param recipient 수신자
     * @param title     메인 타이틀
     * @param subtitle  서브 타이틀
     */
    void displayTitle(
            @NonNull CommandSender recipient,
            @NonNull Component title,
            @NonNull Component subtitle
    );

    /**
     * 액션바 메시지를 표시합니다.
     *
     * @param recipientId 수신자 ID
     * @param message     메시지
     */
    void displayActionBar(@NonNull UUID recipientId, @NonNull Component message);

    /**
     * 액션바 메시지를 표시합니다.
     *
     * @param recipient 수신자
     * @param message   메시지
     */
    void displayActionBar(@NonNull CommandSender recipient, @NonNull Component message);
}
