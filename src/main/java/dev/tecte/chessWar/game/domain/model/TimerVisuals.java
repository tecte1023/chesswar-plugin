package dev.tecte.chessWar.game.domain.model;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Duration;

/**
 * 타이머의 시각적 연출 규칙을 정의합니다.
 */
public record TimerVisuals() {
    /**
     * 남은 시간을 렌더링합니다.
     *
     * @param phaseName 단계 이름
     * @param remaining 남은 시간
     * @return 렌더링된 제목
     */
    @NonNull
    public Component renderTitle(@NonNull String phaseName, @NonNull Duration remaining) {
        return Component.text()
                .append(Component.text("[ ", NamedTextColor.AQUA))
                .append(Component.text(phaseName, NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" ]", NamedTextColor.AQUA))
                .append(Component.space())
                .append(Component.text("시간이 ", NamedTextColor.WHITE))
                .append(Component.text(format(remaining), NamedTextColor.GOLD))
                .append(Component.text(" 남았습니다.", NamedTextColor.WHITE))
                .build();
    }

    @NonNull
    private String format(@NonNull Duration duration) {
        return "%02d분 %02d초".formatted(duration.toMinutes(), duration.toSecondsPart());
    }
}
