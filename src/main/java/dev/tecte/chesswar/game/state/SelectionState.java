package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
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
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        this.gameManager = gameManager;
        GameState.super.onEnter(plugin, gameManager);

        final int[] countHolder = {3};
        plugin.timerManager().startCountdown(3,
                () -> {
                    final Component mainTitle = Component.text(countHolder[0], NamedTextColor.GOLD, TextDecoration.BOLD);
                    final Component subTitle = Component.text("초 후 기물 선택이 시작됩니다.", NamedTextColor.YELLOW);

                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.showTitle(Title.title(mainTitle, subTitle));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                    });
                    countHolder[0]--;
                },
                () -> {
                    plugin.boardManager().setupBarracks(plugin.pieceManager());

                    gameManager.participants().values().forEach(p -> {
                        final Player onlinePlayer = Bukkit.getPlayer(p.playerId());
                        if (onlinePlayer != null) {
                            plugin.boardManager().teleportToBarracks(p.team(), onlinePlayer);
                        }
                    });

                    plugin.timerManager().startHeartbeat();
                    plugin.scoreboardManager().startHeartbeat();
                    plugin.timerManager().startTurnTimer(gameManager.timerSettings().barracksSelectionTime());
                }
        );
    }

    @Override
    public GameState nextState() {
        return new TurnOrderState(plugin);
    }

    @Override
    public GamePhase phase() {
        return GamePhase.PIECE_SELECTION;
    }

    @Override
    public String displayName() {
        return "기물 선택";
    }

    @Override
    public void selectPiece(final GameManager gameManager, final Player participant, final Coordinate coordinate) {
        final java.util.UUID participantId = participant.getUniqueId();
        final Participant currentParticipant = gameManager.participants().get(participantId);

        if (currentParticipant == null) {
            return;
        }

        final boolean isAlreadyTaken = gameManager.participants().values().stream()
                .filter(p -> p.team() == currentParticipant.team())
                .filter(p -> !p.playerId().equals(participantId))
                .anyMatch(p -> coordinate.equals(p.initialCoordinate()));

        if (isAlreadyTaken) {
            participant.sendMessage(Component.text(
                    "해당 위치의 기물은 이미 팀원이 선택했습니다!",
                    NamedTextColor.RED
            ));
            return;
        }

        currentParticipant.initialCoordinate(coordinate);
        final dev.tecte.chesswar.piece.PieceType pieceType = dev.tecte.chesswar.board.ChessFormation.getInitialPieceType(coordinate);
        plugin.pieceManager().applyStats(participant, pieceType);

        dev.tecte.chesswar.piece.PieceItemUtils.replacePlayerPieceItem(participant, pieceType);
        participant.sendMessage(Component.text(
                pieceType.displayName() + " 기물을 선택했습니다!",
                NamedTextColor.GOLD
        ));
    }

    @EventHandler
    public void onEntityInteract(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();

        if (!gameManager.isParticipant(player)) {
            return;
        }

        final Entity clickedEntity = event.getRightClicked();
        final NamespacedKey typeKey = new NamespacedKey(plugin, "barracks_piece_type");
        final NamespacedKey teamKey = new NamespacedKey(plugin, "barracks_piece_team");
        final NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
        final NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");

        if (!clickedEntity.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);

        final String typeStr = clickedEntity.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        final String teamStr = clickedEntity.getPersistentDataContainer().get(teamKey, PersistentDataType.STRING);
        final Integer coordX = clickedEntity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
        final Integer coordY = clickedEntity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

        if (typeStr == null || teamStr == null || coordX == null || coordY == null) {
            return;
        }

        final Coordinate clickedCoordinate = Coordinate.of(coordX, coordY);
        final PieceType pieceType = PieceType.valueOf(typeStr);
        final Team pieceTeam = Team.valueOf(teamStr);
        final Optional<Team> playerTeam = gameManager.findParticipant(player.getUniqueId()).map(Participant::team);

        if (playerTeam.isEmpty() || playerTeam.get() != pieceTeam) {
            player.sendMessage(Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED));
            return;
        }

        renderInfo(player, pieceType, pieceTeam, clickedCoordinate);
    }

    private void renderInfo(final Player player, final PieceType type, final Team team, final Coordinate coordinate) {
        final Component decorationLine = Component.text(
                "━━━━━━━━━━━━━━━",
                NamedTextColor.DARK_GRAY,
                TextDecoration.STRIKETHROUGH
        );
        final Component title = Component.text()
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
        final Component stats = Component.text()
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) type.baseHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) type.baseDamage(), NamedTextColor.RED))
                .build();
        final Component rangeInfo = Component.text()
                .append(Component.text("공격 및 이동 범위: ", NamedTextColor.GRAY))
                .append(Component.text(type.rangeDescription(), NamedTextColor.AQUA))
                .build();

        final boolean isAlreadySelected = gameManager.participants().values().stream()
                .filter(p -> p.team() == team)
                .anyMatch(p -> coordinate.equals(p.initialCoordinate()));

        final Component button;
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
        final Component buttonLine = Component.text()
                .append(Component.text("               "))
                .append(button)
                .build();
        final Component infoPanel = Component.join(
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
