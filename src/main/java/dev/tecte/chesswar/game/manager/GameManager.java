package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.GameResetEvent;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.PhaseTimerSettings;
import dev.tecte.chesswar.game.component.Statistics;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import dev.tecte.chesswar.game.state.GameState;
import dev.tecte.chesswar.game.state.WaitingState;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public class GameManager {
    private static final Component MSG_COUNTDOWN_SUBTITLE = Component.text("잠시 후 기물 선택이 시작됩니다.", NamedTextColor.YELLOW);
    private static final Component ERROR_INSPECT_OWN_TEAM_ONLY = Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED);
    private static final Component ERROR_PIECE_ALREADY_TAKEN = Component.text("해당 위치의 기물은 이미 팀원이 선택했습니다!", NamedTextColor.RED);
    private static final Component MSG_READY_COMPLETE = Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN);
    private static final Component MSG_ELIMINATED = Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED);
    private static final Component MSG_PROMOTED_TO_KING = Component.text("팀에 킹이 없어 당신이 국왕으로 추대되었습니다!", NamedTextColor.GOLD, TextDecoration.BOLD);
    private static final Component MSG_HOVER_TAKEN = Component.text("이미 팀원이 선택한 기물입니다.", NamedTextColor.RED);
    private static final Component MSG_HOVER_SELECT = Component.text("클릭하여 해당 기물로 참전합니다.", NamedTextColor.GREEN);

    private final ChessWar plugin;
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final PhaseTimerSettings timerSettings = PhaseTimerSettings.createDefault();

    private final NamespacedKey keyPieceType;
    private final NamespacedKey keyPieceTeam;
    private final NamespacedKey keyCoordX;
    private final NamespacedKey keyCoordY;

    private GameState currentState;
    private Participant[] turnOrder = new Participant[0];
    private int currentTurnIndex = -1;

    public GameManager(final ChessWar plugin) {
        this.plugin = plugin;
        this.currentState = new WaitingState(plugin);
        
        this.keyPieceType = new NamespacedKey(plugin, "barracks_piece_type");
        this.keyPieceTeam = new NamespacedKey(plugin, "barracks_piece_team");
        this.keyCoordX = new NamespacedKey(plugin, "barracks_piece_x");
        this.keyCoordY = new NamespacedKey(plugin, "barracks_piece_y");
    }

    // --- Properties & Queries ---

    public GamePhase phase() {
        return currentState.phase();
    }

    public Optional<Participant> findParticipant(final UUID playerId) {
        return Optional.ofNullable(participants.get(playerId));
    }

    public boolean isParticipant(final Player player) {
        return participants.containsKey(player.getUniqueId());
    }

    public int countTeam(final Team team) {
        int count = 0;
        for (final Participant participant : participants.values()) {
            if (participant.team() == team) {
                count++;
            }
        }
        return count;
    }

    public Statistics stats(final UUID playerId) {
        final Participant participant = participants.get(playerId);
        return participant != null ? participant.statistics() : new Statistics();
    }

    public boolean isReady(final UUID playerId) {
        final Participant participant = participants.get(playerId);
        return participant != null && participant.ready();
    }

    public boolean areAllParticipantsReady() {
        if (participants.isEmpty()) {
            return false;
        }
        for (final Participant participant : participants.values()) {
            if (!participant.ready()) {
                return false;
            }
        }
        return true;
    }

    public int countReady(final Team team) {
        int count = 0;
        for (final Participant participant : participants.values()) {
            if (participant.team() == team && participant.ready()) {
                count++;
            }
        }
        return count;
    }

    public boolean areAllPiecesSelected() {
        if (participants.isEmpty()) {
            return false;
        }
        for (final Participant participant : participants.values()) {
            if (!participant.hasPiece()) {
                return false;
            }
        }
        return true;
    }

    public Optional<UUID> currentTurnPlayer() {
        if (turnOrder.length == 0 || currentTurnIndex < 0 || currentTurnIndex >= turnOrder.length) {
            return Optional.empty();
        }
        final Participant participant = turnOrder[currentTurnIndex];
        return participant == null ? Optional.empty() : Optional.of(participant.playerId());
    }

    public Optional<Coordinate> findCommandTarget(final UUID commanderId) {
        final Participant participant = participants.get(commanderId);
        return participant == null ? Optional.empty() : Optional.ofNullable(participant.commanderTarget());
    }

    // --- State Transitions & Flow Control ---

    public void advancePhase() {
        changeState(currentState.nextState());
    }

    public void nextTurn() {
        currentState.nextTurn(this);
    }

    public void win(final Team winner) {
        final Component winMessage = Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(winner.teamName(), winner.color(), TextDecoration.BOLD))
                .append(Component.text("가 승리했습니다!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .build();

        Bukkit.broadcast(winMessage);
        advancePhase();
    }

    public void reset() {
        currentState = new WaitingState(plugin);
        turnOrder = new Participant[0];
        currentTurnIndex = -1;

        for (final Participant participant : participants.values()) {
            participant.initialCoordinate(null);
            participant.ready(false);
            participant.turnOrder(-1);
            participant.commanderTarget(null);

            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        Bukkit.getPluginManager().callEvent(new GameResetEvent());
    }

    public void eliminate(final UUID playerId) {
        final Participant participant = participants.get(playerId);
        if (participant == null) return;

        participant.statistics().addDeath();

        final Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage(MSG_ELIMINATED);
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    // --- Phase Context Preparation ---

    public void prepareSelectionContext() {
        plugin.boardManager().setupBarracks(plugin.pieceManager());

        for (final Participant participant : participants.values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                plugin.boardManager().teleportToBarracks(participant.team(), player);
            }
        }

        plugin.timerManager().startTimer(timerSettings.barracksSelectionTime());
    }

    public void prepareTurnOrderContext() {
        autoAssignMissingPieces();
        spawnAllPiecesOnMainBoard();

        final int whiteCount = countTeam(Team.WHITE);
        final int blackCount = countTeam(Team.BLACK);

        plugin.boardManager().setupTurnOrderChests(whiteCount, blackCount);
        plugin.timerManager().startTimer(timerSettings.turnOrderSelectionTime());
    }

    public void prepareBattleContext() {
        plugin.pieceManager().clearSpawnedEntities(true);
        plugin.boardManager().clearBarracksChests();

        final PlayerInventoryAdapter inventoryAdapter = new PlayerInventoryAdapter(plugin, BoardManager.TURN_ORDER_KEY);
        final Map<UUID, Integer> playerOrders = new HashMap<>();

        for (final Participant participant : participants.values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                inventoryAdapter.extractTurnOrder(player).ifPresent(order -> playerOrders.put(participant.playerId(), order));
            }
        }

        calculateTurnOrder(playerOrders);

        for (final Participant participant : participants.values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player == null) continue;

            inventoryAdapter.clearOrderItems(player);

            if (participant.initialCoordinate() != null) {
                plugin.boardManager().deployToBattlefield(participant.team(), participant.initialCoordinate(), player);
            }
        }

        currentTurnPlayer().ifPresent(playerId -> {
            final Player firstPlayer = Bukkit.getPlayer(playerId);
            if (firstPlayer != null) {
                Bukkit.getPluginManager().callEvent(new TurnStartedEvent(firstPlayer));
            }
        });
    }

    public void prepareEndContext() {
        plugin.timerManager().stopTimer();
        plugin.boardVisualManager().displayStatisticsHologram();
    }

    // --- User Actions & Interaction Logic ---

    public void join(final Player player, final Team team) {
        participants.put(player.getUniqueId(), Participant.of(player.getUniqueId(), team));
    }

    public void leave(final Player player) {
        final UUID playerId = player.getUniqueId();
        participants.remove(playerId);

        for (int i = 0; i < turnOrder.length; i++) {
            if (turnOrder[i] != null && playerId.equals(turnOrder[i].playerId())) {
                turnOrder[i] = null;
                break;
            }
        }
    }

    public void handleReadyUp(final Player player, final Location location) {
        currentState.handleReadyUp(this, player, location);
    }

    public void processReadyUp(final Player player) {
        toggleReady(player.getUniqueId(), true);
        player.sendMessage(MSG_READY_COMPLETE);

        findParticipant(player.getUniqueId()).ifPresent(participant -> {
            final Team team = participant.team();
            final Component teamMsg = Component.text(player.getName() + "님이 준비되었습니다! ", NamedTextColor.GRAY)
                    .append(Component.text("(" + countReady(team) + "/" + countTeam(team) + ")", NamedTextColor.AQUA));

            for (final Participant target : participants.values()) {
                if (target.team() == team) {
                    final Player onlinePlayer = Bukkit.getPlayer(target.playerId());
                    if (onlinePlayer != null) {
                        onlinePlayer.sendMessage(teamMsg);
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

    public void selectPiece(final Player player, final Coordinate coordinate) {
        currentState.selectPiece(this, player, coordinate);
    }

    public void processPieceSelection(final Player player, final Coordinate coordinate) {
        final UUID playerId = player.getUniqueId();
        final Participant participant = participants.get(playerId);

        if (participant == null) return;

        if (isCoordinateOccupiedByTeammate(playerId, participant.team(), coordinate)) {
            player.sendMessage(ERROR_PIECE_ALREADY_TAKEN);
            return;
        }

        final PieceType pieceType = ChessFormation.getInitialPieceType(coordinate);
        applyPieceAssignment(participant, player, coordinate, pieceType);

        player.sendMessage(Component.text(pieceType.displayName() + " 기물을 선택했습니다!", NamedTextColor.GOLD));
    }

    public void inspectBarracksPiece(final Player player, final Entity entity) {
        final PersistentDataContainer persistentData = entity.getPersistentDataContainer();
        if (!persistentData.has(keyPieceType, PersistentDataType.STRING)) return;

        final String typeName = persistentData.get(keyPieceType, PersistentDataType.STRING);
        final String teamName = persistentData.get(keyPieceTeam, PersistentDataType.STRING);
        final Integer coordX = persistentData.get(keyCoordX, PersistentDataType.INTEGER);
        final Integer coordY = persistentData.get(keyCoordY, PersistentDataType.INTEGER);

        if (typeName == null || teamName == null || coordX == null || coordY == null) return;

        final PieceType pieceType = PieceType.valueOf(typeName);
        final Team team = Team.valueOf(teamName);
        final Optional<Team> playerTeam = findParticipant(player.getUniqueId()).map(Participant::team);

        if (playerTeam.isEmpty() || playerTeam.get() != team) {
            player.sendMessage(ERROR_INSPECT_OWN_TEAM_ONLY);
            return;
        }

        displayPieceInfo(player, pieceType, team, Coordinate.of(coordX, coordY));
    }

    public void registerCommandTarget(final UUID commanderId, final Coordinate targetCoord) {
        final Participant participant = participants.get(commanderId);
        if (participant != null) {
            participant.commanderTarget(targetCoord);
        }
    }

    public void clearCommandTarget(final UUID commanderId) {
        final Participant participant = participants.get(commanderId);
        if (participant != null) {
            participant.commanderTarget(null);
        }
    }

    // --- System Logic ---

    public void calculateTurnOrder(final Map<UUID, Integer> playerOrders) {
        final List<Participant> whiteTeam = new ArrayList<>();
        final List<Participant> blackTeam = new ArrayList<>();

        for (final Participant participant : participants.values()) {
            final Integer order = playerOrders.get(participant.playerId());
            if (order != null) participant.turnOrder(order);

            if (participant.team() == Team.WHITE) {
                whiteTeam.add(participant);
            } else {
                blackTeam.add(participant);
            }
        }

        whiteTeam.sort(Comparator.comparingInt(Participant::turnOrder));
        blackTeam.sort(Comparator.comparingInt(Participant::turnOrder));
        turnOrder = new Participant[whiteTeam.size() + blackTeam.size()];

        final int maxSize = Math.max(whiteTeam.size(), blackTeam.size());
        int index = 0;
        for (int i = 0; i < maxSize; i++) {
            if (i < whiteTeam.size()) turnOrder[index++] = whiteTeam.get(i);
            if (i < blackTeam.size()) turnOrder[index++] = blackTeam.get(i);
        }
        currentTurnIndex = 0;
    }

    public void performNextTurnLogic() {
        if (turnOrder.length == 0) return;

        currentTurnPlayer().ifPresent(this::clearCommandTarget);

        int attempts = 0;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % turnOrder.length;
            attempts++;
        } while (turnOrder[currentTurnIndex] == null && attempts < turnOrder.length);

        currentTurnPlayer().ifPresent(nextId -> {
            final Player player = Bukkit.getPlayer(nextId);
            if (player != null) {
                Bukkit.getPluginManager().callEvent(new TurnStartedEvent(player));
            }
        });
    }

    public void spawnAllPiecesOnMainBoard() {
        if (!plugin.boardManager().hasBoard()) return;
        plugin.pieceManager().spawnInitialLayout(plugin.boardManager().currentBoard(), participants.values());
    }

    public void autoAssignMissingPieces() {
        enforceMandatoryKing();
        assignRandomRemainingPieces();
    }

    public void startPreSelectionCountdown(final int seconds) {
        plugin.timerManager().startTimer(seconds);
    }

    // --- Feedback & UI ---

    public void displayPieceInfo(final Player player, final PieceType pieceType, final Team team, final Coordinate coordinate) {
        final Component decorationLine = Component.text("━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH);
        final Component title = Component.text()
                .append(decorationLine)
                .appendSpace()
                .append(Component.text("[ " + pieceType.symbol() + " "))
                .append(Component.text(team.teamName() + " " + pieceType.displayName(), team.color(), TextDecoration.BOLD))
                .append(Component.text(" ]", NamedTextColor.WHITE))
                .appendSpace()
                .append(decorationLine)
                .build();

        final Component stats = Component.text()
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) pieceType.baseHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) pieceType.baseDamage(), NamedTextColor.RED))
                .build();

        final Component rangeInfo = Component.text()
                .append(Component.text("공격 및 이동 범위: ", NamedTextColor.GRAY))
                .append(Component.text(pieceType.rangeDescription(), NamedTextColor.AQUA))
                .build();

        final boolean isTaken = isCoordinateOccupiedByTeammate(null, team, coordinate);
        final Component buttonLine = Component.text()
                .append(Component.text("               "))
                .append(buildSelectionButton(isTaken, coordinate))
                .build();

        final Component infoPanel = Component.join(
                JoinConfiguration.newlines(),
                Component.empty(),
                title,
                Component.empty(),
                Component.text(pieceType.description(), NamedTextColor.WHITE),
                Component.empty(),
                stats,
                rangeInfo,
                Component.empty(),
                buttonLine,
                Component.empty()
        );

        player.sendMessage(infoPanel);
    }

    public void broadcastSelectionCountdown(final int seconds) {
        final Component mainTitle = Component.text(seconds, NamedTextColor.GOLD, TextDecoration.BOLD);

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(mainTitle, MSG_COUNTDOWN_SUBTITLE));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
        }
    }

    // --- Private Helpers ---

    private void changeState(final GameState nextState) {
        if (currentState != null) currentState.onExit();
        currentState = nextState;
        broadcastPhaseChange(currentState.phase());
        currentState.onEnter(plugin, this);
    }

    private void broadcastPhaseChange(final GamePhase nextPhase) {
        Bukkit.broadcast(Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("게임 단계가 변경되었습니다: ", NamedTextColor.YELLOW))
                .append(Component.text(nextPhase.displayName(), NamedTextColor.WHITE, TextDecoration.BOLD))
                .build());
    }

    private void toggleReady(final UUID playerId, final boolean ready) {
        final Participant participant = participants.get(playerId);
        if (participant != null) {
            participant.ready(ready);
        }
    }

    private void applyPieceAssignment(final Participant participant, final Player player, final Coordinate coordinate, final PieceType pieceType) {
        participant.initialCoordinate(coordinate);
        if (player == null) return;

        plugin.pieceManager().applyStats(player, pieceType);
        PieceItemUtils.replacePlayerPieceItem(player, pieceType);
    }

    private void enforceMandatoryKing() {
        final Set<Team> teamsWithKings = new HashSet<>();
        for (final Participant participant : participants.values()) {
            if (participant.initialCoordinate() != null && ChessFormation.getInitialPieceType(participant.initialCoordinate()) == PieceType.KING) {
                teamsWithKings.add(participant.team());
            }
        }

        for (final Team team : Team.values()) {
            if (teamsWithKings.contains(team)) continue;

            final List<Participant> teamMembers = participants.values().stream()
                    .filter(participant -> participant.team() == team)
                    .toList();

            if (teamMembers.isEmpty()) continue;

            final Participant luckyMember = teamMembers.get((int) (Math.random() * teamMembers.size()));
            final Coordinate kingCoord = ChessFormation.getKingCoordinate(team);
            final Player player = Bukkit.getPlayer(luckyMember.playerId());

            applyPieceAssignment(luckyMember, player, kingCoord, PieceType.KING);

            if (player != null) {
                player.sendMessage(MSG_PROMOTED_TO_KING);
            }
        }
    }

    private void assignRandomRemainingPieces() {
        for (final Team team : Team.values()) {
            final List<Participant> teamMembersWithoutPiece = participants.values().stream()
                    .filter(participant -> participant.team() == team && participant.initialCoordinate() == null)
                    .toList();

            if (teamMembersWithoutPiece.isEmpty()) continue;

            final Set<Coordinate> takenCoordinates = participants.values().stream()
                    .filter(participant -> participant.team() == team && participant.initialCoordinate() != null)
                    .map(Participant::initialCoordinate)
                    .collect(Collectors.toSet());

            final List<Coordinate> availableCoordinates = new ArrayList<>();
            final int backRank = (team == Team.WHITE) ? ChessFormation.WHITE_BACK_RANK : ChessFormation.BLACK_BACK_RANK;
            final int pawnRank = (team == Team.WHITE) ? ChessFormation.WHITE_PAWN_RANK : ChessFormation.BLACK_PAWN_RANK;

            for (int x = 0; x < ChessFormation.BOARD_SIZE; x++) {
                final Coordinate backCoord = Coordinate.of(x, backRank);
                if (!takenCoordinates.contains(backCoord)) availableCoordinates.add(backCoord);
                final Coordinate pawnCoord = Coordinate.of(x, pawnRank);
                if (!takenCoordinates.contains(pawnCoord)) availableCoordinates.add(pawnCoord);
            }

            Collections.shuffle(availableCoordinates);

            for (int i = 0; i < teamMembersWithoutPiece.size() && i < availableCoordinates.size(); i++) {
                final Participant participant = teamMembersWithoutPiece.get(i);
                final Coordinate randomCoord = availableCoordinates.get(i);
                final Player player = Bukkit.getPlayer(participant.playerId());
                final PieceType pieceType = ChessFormation.getInitialPieceType(randomCoord);

                applyPieceAssignment(participant, player, randomCoord, pieceType);

                if (player != null) {
                    player.sendMessage(Component.text("기물을 선택하지 않아 무작위 기물(" + pieceType.displayName() + ")이 배정되었습니다.", NamedTextColor.YELLOW));
                }
            }
        }
    }

    private boolean isCoordinateOccupiedByTeammate(final UUID requesterId, final Team team, final Coordinate coordinate) {
        return participants.values().stream()
                .filter(participant -> participant.team() == team)
                .filter(participant -> !participant.playerId().equals(requesterId))
                .anyMatch(participant -> coordinate.equals(participant.initialCoordinate()));
    }

    private Component buildSelectionButton(final boolean isTaken, final Coordinate coordinate) {
        final Component button = Component.text()
                .color(isTaken ? NamedTextColor.GRAY : NamedTextColor.GREEN)
                .append(Component.text("[ ⚔ "))
                .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
                .append(Component.text(" ]"))
                .build();

        if (isTaken) {
            return button.hoverEvent(HoverEvent.showText(MSG_HOVER_TAKEN));
        } else {
            return button.hoverEvent(HoverEvent.showText(MSG_HOVER_SELECT))
                    .clickEvent(ClickEvent.runCommand("/cw select " + coordinate.x() + " " + coordinate.y()));
        }
    }
}
