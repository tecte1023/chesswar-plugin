package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

@RequiredArgsConstructor
public class PieceSelectionListener implements Listener {
    private final ChessWar plugin;
    private final GameManager gameManager;

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (gameManager.phase() != GamePhase.PIECE_SELECTION) {
            return;
        }

        if (!gameManager.isParticipant(player)) {
            return;
        }

        Entity clickedEntity = event.getRightClicked();
        NamespacedKey typeKey = new NamespacedKey(plugin, "barracks_piece_type");
        NamespacedKey teamKey = new NamespacedKey(plugin, "barracks_piece_team");
        NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
        NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");

        if (!clickedEntity.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);

        String typeStr = clickedEntity.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String teamStr = clickedEntity.getPersistentDataContainer().get(teamKey, PersistentDataType.STRING);
        Integer coordX = clickedEntity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
        Integer coordY = clickedEntity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

        if (typeStr == null || teamStr == null || coordX == null || coordY == null) {
            return;
        }

        Coordinate clickedCoordinate = Coordinate.of(coordX, coordY);

        PieceType pieceType = PieceType.valueOf(typeStr);
        Team pieceTeam = Team.valueOf(teamStr);
        Optional<Team> playerTeam = gameManager.findParticipant(player.getUniqueId()).map(Participant::team);

        if (playerTeam.isEmpty() || playerTeam.get() != pieceTeam) {
            player.sendMessage(Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED));
            return;
        }

        renderInfo(player, pieceType, pieceTeam, clickedCoordinate);
    }

    private void renderInfo(Player player, PieceType type, Team team, Coordinate coordinate) {
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
                        team.teamName() + " " + type.displayName(),
                        team.color(),
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

        boolean isAlreadySelected = gameManager.participants().values().stream()
                .filter(p -> p.team() == team)
                .anyMatch(p -> coordinate.equals(p.initialCoordinate()));

        Component button;
        if (isAlreadySelected) {
            button = Component.text()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("[ ⚔ "))
                    .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                    .append(Component.text(" ]"))
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "이미 팀원이 선택한 기물입니다.",
                            NamedTextColor.RED
                    )))
                    .build();
        } else {
            button = Component.text()
                    .color(NamedTextColor.GREEN)
                    .append(Component.text("[ ⚔ "))
                    .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                    .append(Component.text(" ]"))
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "클릭하여 해당 기물로 참전합니다.",
                            NamedTextColor.GREEN
                    )))
                    .clickEvent(ClickEvent.runCommand("/cw select " + coordinate.x() + " " + coordinate.y()))
                    .build();
        }
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
