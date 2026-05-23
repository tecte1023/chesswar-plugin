package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.Participant;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

@RequiredArgsConstructor
public class ChessPieceSelectionListener implements Listener {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (gameManager.phase() != GamePhase.WAITING && gameManager.phase() != GamePhase.PIECE_SELECTION) {
            return;
        }

        if (!gameManager.isParticipant(player)) {
            return;
        }

        Entity clickedEntity = event.getRightClicked();
        NamespacedKey typeKey = new NamespacedKey(plugin, "barracks_piece_type");
        NamespacedKey teamKey = new NamespacedKey(plugin, "barracks_piece_team");

        if (!clickedEntity.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);

        String typeStr = clickedEntity.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String teamStr = clickedEntity.getPersistentDataContainer().get(teamKey, PersistentDataType.STRING);

        if (typeStr == null || teamStr == null) {
            return;
        }

        PieceType pieceType = PieceType.valueOf(typeStr);
        Team pieceTeam = Team.valueOf(teamStr);
        Optional<Team> playerTeam = gameManager.findParticipant(player.getUniqueId()).map(Participant::team);

        if (playerTeam.isEmpty() || playerTeam.get() != pieceTeam) {
            player.sendMessage(Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED));
            return;
        }

        renderInfo(player, pieceType, pieceTeam);

        if (gameManager.areAllPiecesSelected()) {
            timerManager.accelerateTo(10);
            Bukkit.broadcast(Component.text(
                    " 모든 플레이어가 기물을 선택했습니다! 10초 후 준비 단계로 넘어갑니다.",
                    NamedTextColor.AQUA,
                    TextDecoration.BOLD
            ));
        }
    }

    private void renderInfo(Player player, PieceType type, Team team) {
        Component decorationLine = Component.text(
                "━━━━━━━━━━━━━━━",
                NamedTextColor.DARK_GRAY,
                TextDecoration.STRIKETHROUGH
        );
        Component title = Component.text()
                .append(decorationLine)
                .appendSpace()
                .append(Component.text("[ " + type.symbol() + " "))
                .append(Component.text(
                        team.displayName() + " " + type.displayName(),
                        team.textColor(),
                        TextDecoration.BOLD
                ))
                .append(Component.text(" ]", NamedTextColor.WHITE))
                .appendSpace()
                .append(decorationLine)
                .build();
        Component stats = Component.text()
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) type.baseHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) type.baseDamage(), NamedTextColor.RED))
                .build();
        Component rangeInfo = Component.text()
                .append(Component.text("공격 및 이동 범위: ", NamedTextColor.GRAY))
                .append(Component.text(type.rangeDescription(), NamedTextColor.AQUA))
                .build();
        Component button = Component.text()
                .color(NamedTextColor.GREEN)
                .append(Component.text("[ ⚔ "))
                .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                .append(Component.text(" ]"))
                .hoverEvent(HoverEvent.showText(Component.text(
                        "클릭하여 해당 기물로 참전합니다.",
                        NamedTextColor.GREEN
                )))
                .clickEvent(ClickEvent.runCommand("/cw select " + type.name()))
                .build();
        Component buttonLine = Component.text()
                .append(Component.text("               "))
                .append(button)
                .build();
        Component infoPanel = Component.join(
                JoinConfiguration.newlines(),
                Component.empty(),
                title,
                Component.empty(),
                Component.text(type.description(), NamedTextColor.WHITE),
                Component.empty(),
                stats,
                rangeInfo,
                Component.empty(),
                buttonLine,
                Component.empty()
        );

        player.sendMessage(infoPanel);
    }
}
