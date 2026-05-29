package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.*;
import dev.tecte.chesswar.game.state.BattleState;
import dev.tecte.chesswar.game.state.EndedState;
import dev.tecte.chesswar.game.state.GameState;
import dev.tecte.chesswar.game.state.SelectionState;
import dev.tecte.chesswar.game.state.TurnOrderState;
import dev.tecte.chesswar.game.state.WaitingState;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class GameManager {
    private final ChessWar plugin;

    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<UUID, Statistics> statistics = new HashMap<>();
    private final List<UUID> turnOrder = new ArrayList<>();
    private final Set<UUID> readyPlayers = new HashSet<>();
    private final Map<UUID, Coordinate> commanderCommands = new HashMap<>();
    private final PhaseTimerSettings timerSettings = PhaseTimerSettings.createDefault();

    private GamePhase phase = GamePhase.WAITING;
    private GameState currentState = new WaitingState();
    private int currentTurnIndex = -1;

    public void commandTarget(UUID commanderId, Coordinate targetCoord) {
        commanderCommands.put(commanderId, targetCoord);
    }

    public void clearCommandTarget(UUID commanderId) {
        commanderCommands.remove(commanderId);
    }

    public Optional<Coordinate> commandTarget(UUID commanderId) {
        return Optional.ofNullable(commanderCommands.get(commanderId));
    }

    public void toggleReady(UUID playerId, boolean ready) {
        if (ready) {
            readyPlayers.add(playerId);
        } else {
            readyPlayers.remove(playerId);
        }
    }

    public boolean isReady(UUID playerId) {
        return readyPlayers.contains(playerId);
    }

    public boolean areAllParticipantsReady() {
        if (participants.isEmpty()) {
            return false;
        }
        return readyPlayers.containsAll(participants.keySet());
    }

    public boolean areAllPiecesSelected() {
        if (participants.isEmpty()) {
            return false;
        }
        return participants.values().stream().allMatch(p -> p.initialCoordinate() != null);
    }

    public void advancePhase() {
        if (phase == GamePhase.WAITING) {
            startStartSequence();
            return;
        }

        changeState(currentState.nextState());
    }

    private void changeState(GameState nextState) {
        if (currentState != null) {
            currentState.onExit();
        }

        currentState = nextState;
        phase = switch (currentState) {
            case SelectionState ignored -> GamePhase.PIECE_SELECTION;
            case TurnOrderState ignored -> GamePhase.TURN_ORDER;
            case BattleState ignored -> GamePhase.BATTLE;
            case EndedState ignored -> GamePhase.ENDED;
            default -> GamePhase.WAITING;
        };
        broadcastPhaseChange(phase);
        currentState.onEnter(plugin, this);
    }

    private void broadcastPhaseChange(GamePhase nextPhase) {
        Bukkit.broadcast(Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("게임 단계가 변경되었습니다: ", NamedTextColor.YELLOW))
                .append(Component.text(nextPhase.displayName(), NamedTextColor.WHITE, TextDecoration.BOLD))
                .build());
    }

    private void startStartSequence() {
        int[] countHolder = {3};
        plugin.timerManager().startCountdown(3,
                () -> {
                    Component mainTitle = Component.text(countHolder[0], NamedTextColor.GOLD, TextDecoration.BOLD);
                    Component subTitle = Component.text("초 후 기물 선택이 시작됩니다.", NamedTextColor.YELLOW);

                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.showTitle(Title.title(mainTitle, subTitle));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                    });
                    countHolder[0]--;
                },
                () -> {
                    changeState(new SelectionState(plugin));

                    plugin.timerManager().startHeartbeat();
                    plugin.scoreboardManager().startHeartbeat();
                    plugin.timerManager().startTurnTimer(timerSettings.barracksSelectionTime());
                }
        );
    }

    public void spawnAllPiecesOnMainBoard() {
        if (!plugin.boardManager().hasBoard()) return;
        ChessBoard mainBoard = plugin.boardManager().currentBoard();

        for (int y : new int[]{
                ChessFormation.WHITE_BACK_RANK, ChessFormation.WHITE_PAWN_RANK,
                ChessFormation.BLACK_PAWN_RANK, ChessFormation.BLACK_BACK_RANK
        }) {
            Team team = (y < 4) ? Team.WHITE : Team.BLACK;
            Vector direction = (team == Team.WHITE) ? mainBoard.forward().getDirection() : mainBoard.forward().getDirection().multiply(-1);

            for (int x = 0; x < ChessFormation.BOARD_SIZE; x++) {
                Coordinate coord = Coordinate.of(x, y);

                Optional<Participant> participant = participants.values().stream()
                        .filter(p -> coord.equals(p.initialCoordinate()))
                        .findFirst();

                PieceType type = ChessFormation.getInitialPieceType(coord);
                Piece piece;

                if (participant.isPresent()) {
                    piece = Piece.of(participant.get().playerId(), team, type);
                    plugin.pieceManager().placePiece(coord, piece);
                } else {
                    piece = Piece.of(null, team, type);
                    plugin.pieceManager().placePiece(coord, piece);
                }

                plugin.pieceManager().spawnPiece(
                        mainBoard.toCenterLocation(coord),
                        type,
                        team,
                        coord,
                        direction,
                        false
                );
            }
        }
    }

    public void join(Player player, Team team) {
        UUID playerId = player.getUniqueId();
        participants.put(playerId, Participant.of(playerId, team));
    }

    public void selectPiece(Player participant, Coordinate coordinate) {
        UUID participantId = participant.getUniqueId();
        Participant currentParticipant = participants.get(participantId);

        if (currentParticipant == null) {
            return;
        }

        boolean isAlreadyTaken = participants.values().stream()
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

        participants.put(participantId, Participant.of(participantId, currentParticipant.team(), coordinate));
        PieceType pieceType = ChessFormation.getInitialPieceType(coordinate);
        plugin.pieceManager().applyStats(participant, pieceType);

        PieceItemUtils.replacePlayerPieceItem(participant, pieceType);
        participant.sendMessage(Component.text(
                pieceType.displayName() + " 기물을 선택했습니다!",
                NamedTextColor.GOLD
        ));
    }

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
    }

    public boolean isParticipant(Player player) {
        return participants.containsKey(player.getUniqueId());
    }

    public Optional<Participant> findParticipant(UUID playerId) {
        return Optional.ofNullable(participants.get(playerId));
    }

    public void calculateTurnOrder() {
        turnOrder.clear();

        NamespacedKey orderKey = new NamespacedKey(plugin, BoardManager.TURN_ORDER_KEY);
        List<UUID> whiteTeam = new ArrayList<>();
        List<UUID> blackTeam = new ArrayList<>();
        Map<UUID, Integer> playerOrders = new HashMap<>();

        for (Participant participant : participants.values()) {
            Player player = Bukkit.getPlayer(participant.playerId());
            int bestOrder = Integer.MAX_VALUE;

            if (player != null) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.hasItemMeta()) {
                        Integer order = item.getItemMeta()
                                .getPersistentDataContainer()
                                .get(orderKey, PersistentDataType.INTEGER);

                        if (order != null && order < bestOrder) {
                            bestOrder = order;
                        }
                    }
                }
            }

            if (bestOrder != Integer.MAX_VALUE) {
                playerOrders.put(participant.playerId(), bestOrder);
            }

            if (participant.team() == Team.WHITE) {
                whiteTeam.add(participant.playerId());
            } else {
                blackTeam.add(participant.playerId());
            }
        }

        whiteTeam.sort(Comparator.comparingInt(id -> playerOrders.getOrDefault(id, 999)));
        blackTeam.sort(Comparator.comparingInt(id -> playerOrders.getOrDefault(id, 999)));

        int maxSize = Math.max(whiteTeam.size(), blackTeam.size());

        for (int i = 0; i < maxSize; i++) {
            if (i < whiteTeam.size()) {
                turnOrder.add(whiteTeam.get(i));
            }

            if (i < blackTeam.size()) {
                turnOrder.add(blackTeam.get(i));
            }
        }

        for (UUID playerId : participants.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.hasItemMeta()) {
                        if (item.getItemMeta().getPersistentDataContainer().has(orderKey, PersistentDataType.INTEGER)) {
                            player.getInventory().setItem(i, null);
                        }
                    }
                }
            }
        }

        currentTurnIndex = 0;
    }

    public Optional<UUID> currentTurnPlayer() {
        if (currentTurnIndex < 0 || currentTurnIndex >= turnOrder.size()) {
            return Optional.empty();
        }

        return Optional.of(turnOrder.get(currentTurnIndex));
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) {
            return;
        }

        currentTurnPlayer().ifPresent(this::clearCommandTarget);

        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        currentTurnPlayer().ifPresent(uuid -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                updateInvulnerability(player);
                Bukkit.getPluginManager().callEvent(new TurnStartedEvent(player));
            }
        });
    }

    public void updateInvulnerability(Player currentPlayer) {
        Participant participant = participants.get(currentPlayer.getUniqueId());
        if (participant == null) return;

        Team myTeam = participant.team();
        boolean isKing = false;

        for (Piece p : plugin.pieceManager().boardPieces().values()) {
            if (currentPlayer.getUniqueId().equals(p.ownerId()) && p.type() == PieceType.KING) {
                isKing = true;
                break;
            }
        }

        for (Map.Entry<Coordinate, LivingEntity> entry : plugin.pieceManager().pieceEntities().entrySet()) {
            Coordinate coord = entry.getKey();
            LivingEntity living = entry.getValue();

            if (living == null || !living.isValid()) continue;

            Piece piece = plugin.pieceManager().boardPieces().get(coord);
            if (piece == null) continue;

            boolean shouldBeVulnerable = false;

            if (piece.team() != myTeam) {
                shouldBeVulnerable = true;
            } else if (isKing && !piece.isPlayerPiece()) {
                shouldBeVulnerable = true;
            }

            living.setInvulnerable(!shouldBeVulnerable);
        }
    }

    public void win(Team winner) {
        Component winMessage = Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(winner.displayName(), winner.textColor(), TextDecoration.BOLD))
                .append(Component.text("가 승리했습니다!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .build();

        Bukkit.broadcast(winMessage);
        advancePhase();
    }

    public Statistics stats(UUID playerId) {
        return statistics.computeIfAbsent(playerId, id -> new Statistics());
    }

    public void handleReadyUp(Player player, Location location) {
        if (phase != GamePhase.TURN_ORDER) {
            player.sendMessage(Component.text("지금은 전투 준비를 완료할 수 있는 시간이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (isReady(player.getUniqueId())) {
            player.sendMessage(Component.text("이미 준비 완료 상태입니다.", NamedTextColor.YELLOW));
            return;
        }

        if (location != null) {
            boolean isMyChest = findParticipant(player.getUniqueId())
                    .map(p -> plugin.boardManager().isTeamChest(location, p.team()))
                    .orElse(false);

            if (!isMyChest) {
                player.sendMessage(Component.text("자신의 팀 막사에 있는 상자에서만 준비를 완료할 수 있습니다!", NamedTextColor.RED));
                return;
            }
        }

        toggleReady(player.getUniqueId(), true);
        player.sendMessage(Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN));

        Optional<Participant> participant = findParticipant(player.getUniqueId());

        participant.ifPresent(p -> {
            Team team = p.team();
            Component msg = Component.text(player.getName() + "님이 준비되었습니다! ", NamedTextColor.GRAY)
                    .append(Component.text("(" + countReady(team) + "/" + countTeam(team) + ")", NamedTextColor.AQUA));

            for (Participant other : participants.values()) {
                if (other.team() == team) {
                    Player online = player.getServer().getPlayer(other.playerId());

                    if (online != null) {
                        online.sendMessage(msg);
                    }
                }
            }
        });

        if (areAllParticipantsReady()) {
            Bukkit.broadcast(Component.text()
                    .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("모든 플레이어가 준비를 마쳤습니다! " + timerSettings.readyAccelerateTime() + "초 후 전투가 시작됩니다.", NamedTextColor.GREEN, TextDecoration.BOLD))
                    .build());
        }
    }

    private int countReady(Team team) {
        return (int) participants.values().stream()
                .filter(p -> p.team() == team && isReady(p.playerId()))
                .count();
    }

    private int countTeam(Team team) {
        return (int) participants.values().stream()
                .filter(p -> p.team() == team)
                .count();
    }

    public void reset() {
        Bukkit.getPluginManager().callEvent(new GameResetEvent());
        phase = GamePhase.WAITING;
        currentState = new WaitingState();
        turnOrder.clear();
        currentTurnIndex = -1;
        readyPlayers.clear();
        statistics.clear();
        commanderCommands.clear();
        plugin.pieceManager().clearSpawnedEntities(false);
        plugin.boardManager().clearBarracksChests();
        plugin.pieceManager().reset();

        participants.keySet().forEach(id -> participants.computeIfPresent(id, (k, p) -> Participant.of(id, p.team(), null)));

        for (Participant participant : participants.values()) {
            Player player = Bukkit.getPlayer(participant.playerId());

            if (player != null) {
                plugin.pieceManager().resetStats(player);
                player.setGameMode(GameMode.SURVIVAL);

                PieceItemUtils.removePlayerPieceItems(player);
            }
        }
    }
}
