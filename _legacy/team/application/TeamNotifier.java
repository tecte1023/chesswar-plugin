package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.port.UserNotifier;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/**
 * 팀 관련 알림 및 안내 메시지를 전송합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamNotifier {
    private final UserNotifier userNotifier;

    /**
     * 팀 참가 성공을 알립니다.
     *
     * @param recipient 수신자
     * @param teamColor 참가한 팀
     */
    public void informJoin(@NonNull CommandSender recipient, @NonNull TeamColor teamColor) {
        userNotifier.informSuccess(
                recipient,
                Component.text(teamColor.displayName(), teamColor.textColor())
                        .append(Component.text("에 참가했습니다.", NamedTextColor.AQUA))
        );
    }

    /**
     * 팀 나가기 성공을 알립니다.
     *
     * @param recipient 수신자
     */
    public void informLeave(@NonNull CommandSender recipient) {
        userNotifier.informSuccess(recipient, Component.text("팀에서 나갔습니다.", NamedTextColor.YELLOW));
    }
}
