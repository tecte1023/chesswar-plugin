package dev.tecte.chessWar.port.notifier;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/**
 * {@link CommandSender}에게 메시지를 보내는 역할을 담당하는 포트 인터페이스입니다.
 * 이 인터페이스는 애플리케이션 계층과 인프라스트럭처 계층 사이의 의존성을 분리합니다.
 */
public interface SenderNotifier {
    /**
     * 성공 메시지를 실행 주체에게 알립니다.
     *
     * @param sender  알림을 받을 실행 주체
     * @param message 보낼 메시지 컴포넌트
     */
    void notifySuccess(@NonNull CommandSender sender, @NonNull Component message);

    /**
     * 성공 메시지를 실행 주체에게 알립니다.
     *
     * @param sender  알림을 받을 실행 주체
     * @param message 보낼 메시지 문자열
     */
    void notifySuccess(@NonNull CommandSender sender, @NonNull String message);

    /**
     * 오류 메시지를 실행 주체에게 알립니다.
     *
     * @param sender  알림을 받을 실행 주체
     * @param message 보낼 메시지 컴포넌트
     */
    void notifyError(@NonNull CommandSender sender, @NonNull Component message);

    /**
     * 오류 메시지를 실행 주체에게 알립니다.
     *
     * @param sender  알림을 받을 실행 주체
     * @param message 보낼 메시지 문자열
     */
    void notifyError(@NonNull CommandSender sender, @NonNull String message);
}
