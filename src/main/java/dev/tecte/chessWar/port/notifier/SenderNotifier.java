package dev.tecte.chessWar.port.notifier;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/**
 * 메시지를 전송합니다.
 */
public interface SenderNotifier {
    /**
     * 성공 메시지를 전송합니다.
     *
     * @param sender  알림을 받을 대상
     * @param message 메시지
     */
    void notifySuccess(@NonNull CommandSender sender, @NonNull Component message);

    /**
     * 성공 메시지를 전송합니다.
     *
     * @param sender  알림을 받을 대상
     * @param message 메시지
     */
    void notifySuccess(@NonNull CommandSender sender, @NonNull String message);

    /**
     * 오류 메시지를 전송합니다.
     *
     * @param sender  알림을 받을 대상
     * @param message 메시지
     */
    void notifyError(@NonNull CommandSender sender, @NonNull Component message);

    /**
     * 오류 메시지를 전송합니다.
     *
     * @param sender  알림을 받을 대상
     * @param message 메시지
     */
    void notifyError(@NonNull CommandSender sender, @NonNull String message);

    /**
     * 타이틀 메시지를 전송합니다.
     *
     * @param sender   알림을 받을 대상
     * @param title    메인 타이틀
     * @param subtitle 서브 타이틀
     */
    void sendTitle(@NonNull CommandSender sender, @NonNull Component title, @NonNull Component subtitle);

    /**
     * 타이틀 메시지를 전송합니다.
     *
     * @param sender 알림을 받을 대상
     * @param title  메인 타이틀
     */
    void sendTitle(@NonNull CommandSender sender, @NonNull Component title);

    /**
     * 액션바 메시지를 전송합니다.
     *
     * @param sender  알림을 받을 대상
     * @param message 메시지
     */
    void sendActionBar(@NonNull CommandSender sender, @NonNull Component message);
}
