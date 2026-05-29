package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.PieceType;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

@RequiredArgsConstructor
public class SelectionState implements GameState {
    private final ChessWar plugin;
    private GameManager gameManager;

    @Override
    public void onEnter(Plugin plugin, GameManager gameManager) {
        this.gameManager = gameManager;
        GameState.super.onEnter(plugin, gameManager);
        
        // 물리적 공간 세팅 및 플레이어 텔레포트를 상태 진입 시점에 캡슐화하여 일괄 처리
        this.plugin.boardManager().setupBarracks(this.plugin.pieceManager());
        
        gameManager.participants().values().forEach(p -> {
            Player onlinePlayer = org.bukkit.Bukkit.getPlayer(p.playerId());
            if (onlinePlayer != null) {
                this.plugin.boardManager().teleportToBarracks(p.team(), onlinePlayer);
            }
        });
    }

    @Override
    public GameState nextState() {
        return new TurnOrderState(plugin);
    }

    @Override
    public String displayName() {
        return "기물 선택";
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

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
