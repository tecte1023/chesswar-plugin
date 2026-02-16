package dev.tecte.chessWar.common.event;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * 이벤트의 알림 대상 정보를 제공합니다.
 */
public interface NotifiableEvent {
    /**
     * 이벤트를 일으킨 행위자를 식별합니다.
     *
     * @return 행위자
     */
    @Nullable
    CommandSender sender();
}
