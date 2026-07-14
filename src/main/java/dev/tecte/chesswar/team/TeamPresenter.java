package dev.tecte.chesswar.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class TeamPresenter {
    public void showPlayerJoinFeedback(
            @NotNull final Player player,
            @NotNull final JoinResult result,
            @NotNull final TeamSide teamSide
    ) {
        switch (result) {
            case INVALID_PHASE -> player.sendMessage(Component.text(
                    "게임 중에는 팀에 참가할 수 없습니다.",
                    NamedTextColor.RED
            ));
            case TEAM_FULL -> player.sendMessage(Component.text(
                    "해당 팀의 정원이 가득 찼습니다.",
                    NamedTextColor.RED
            ));
            case ALREADY_IN_TEAM -> player.sendMessage(Component.text(
                    "이미 해당 팀에 소속되어 있습니다.",
                    NamedTextColor.RED
            ));
            case SUCCESS -> player.sendMessage(Component.text(
                    teamSide.teamName() + "에 참가했습니다.",
                    NamedTextColor.GREEN
            ));
        }
    }

    public void showPlayerLeaveFeedback(
            @NotNull final Player player,
            @NotNull final LeaveResult result,
            @NotNull final TeamSide teamSide
    ) {
        switch (result) {
            case SUCCESS -> player.sendMessage(Component.text("팀에서 탈퇴했습니다.", NamedTextColor.RED));
            case INVALID_PHASE -> player.sendMessage(Component.text(
                    "게임 중에는 팀에서 탈퇴할 수 없습니다.",
                    NamedTextColor.RED
            ));
            case NOT_IN_TEAM -> player.sendMessage(Component.text(
                    teamSide.teamName() + "에 소속되어 있지 않습니다.",
                    NamedTextColor.RED
            ));
        }
    }

    public void showAdminLeaveFeedback(
            @NotNull final CommandSender sender,
            @NotNull final Player target,
            @NotNull final TeamSide teamSide,
            @NotNull final LeaveResult result
    ) {
        if (sender.equals(target)) {
            showPlayerLeaveFeedback(target, result, teamSide);
            return;
        }

        switch (result) {
            case SUCCESS -> {
                sender.sendMessage(Component.text(
                        target.getName() + "님을 " + teamSide.teamName() + "에서 강제 탈퇴시켰습니다.",
                        NamedTextColor.GREEN
                ));
                target.sendMessage(Component.text(
                        "관리자에 의해 " + teamSide.teamName() + "에서 강제 탈퇴되었습니다.",
                        NamedTextColor.RED
                ));
            }
            case INVALID_PHASE -> sender.sendMessage(Component.text(
                    "게임 중에는 강제 탈퇴시킬 수 없습니다.",
                    NamedTextColor.RED
            ));
            case NOT_IN_TEAM -> sender.sendMessage(Component.text(
                    target.getName() + "님은 " + teamSide.teamName() + "에 소속되어 있지 않습니다.",
                    NamedTextColor.RED
            ));
        }
    }

    public void showAdminJoinFeedback(
            @NotNull final CommandSender sender,
            @NotNull final Player target,
            @NotNull final TeamSide teamSide,
            @NotNull final JoinResult result
    ) {
        if (sender.equals(target)) {
            showPlayerJoinFeedback(target, result, teamSide);
            return;
        }

        switch (result) {
            case SUCCESS -> {
                sender.sendMessage(Component.text(
                        target.getName() + "님을 " + teamSide.teamName() + "에 강제 배정했습니다.",
                        NamedTextColor.GREEN
                ));
                target.sendMessage(Component.text(
                        "관리자에 의해 " + teamSide.teamName() + "에 강제 배정되었습니다.",
                        NamedTextColor.GREEN
                ));
            }
            case INVALID_PHASE -> sender.sendMessage(Component.text(
                    "게임 중에는 팀에 강제 배정할 수 없습니다.",
                    NamedTextColor.RED
            ));
            case TEAM_FULL -> sender.sendMessage(Component.text(
                    "해당 팀의 정원이 가득 찼습니다.",
                    NamedTextColor.RED
            ));
            case ALREADY_IN_TEAM -> sender.sendMessage(Component.text(
                    "이미 해당 팀에 소속되어 있습니다.",
                    NamedTextColor.RED
            ));
        }
    }

    public void showTeamList(@NotNull final CommandSender target, @NotNull final UUID[][] rosters) {
        final Builder messageBuilder = Component.text()
                .append(Component.text("=== 현재 팀 참가자 목록 ===", NamedTextColor.GOLD));

        for (final TeamSide teamSide : TeamSide.values()) {
            final UUID[] roster = rosters[teamSide.ordinal()];
            final var nameBuilder = new StringBuilder();

            for (final UUID uuid : roster) {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                final String name = offlinePlayer.getName() == null ? "(알 수 없음)" : offlinePlayer.getName();

                if (!nameBuilder.isEmpty()) {
                    nameBuilder.append(", ");
                }

                nameBuilder.append(name);
            }

            messageBuilder.append(Component.newline())
                    .append(Component.text(teamSide.teamName(), teamSide.color()))
                    .append(Component.text(" [" + roster.length + "명]: ", NamedTextColor.WHITE))
                    .append(Component.text(nameBuilder.isEmpty() ? "-" : nameBuilder.toString(), NamedTextColor.GRAY));
        }

        target.sendMessage(messageBuilder.build());
    }
}
