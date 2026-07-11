package dev.tecte.chesswar.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TeamPresenter {
    public void sendJoinMessage(@NotNull final Player player, @NotNull final TeamSide teamSide) {
        player.sendMessage(Component.textOfChildren(
                Component.text(teamSide.teamName(), teamSide.color()),
                Component.text("에 참가했습니다.")
        ));
    }

    public void sendLeaveMessage(@NotNull final Player player) {
        player.sendMessage(Component.text("팀에서 탈퇴했습니다.", NamedTextColor.RED));
    }
}
