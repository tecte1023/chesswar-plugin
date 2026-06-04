package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardVisualManager;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.PhaseTimerSettings;
import dev.tecte.chesswar.game.component.Statistics;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceState;
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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@lombok.extern.slf4j.Slf4j(topic = "ChessWar")
public class GameManager {
    private static final String KEY_NAME_PIECE_TYPE = "barracks_piece_type";
    private static final String KEY_NAME_PIECE_TEAM = "barracks_piece_team";
    private static final String KEY_NAME_COORD_X = "barracks_piece_x";
    private static final String KEY_NAME_COORD_Y = "barracks_piece_y";

    // Static Feedback Constants
    private static final Component MSG_COUNTDOWN_SUBTITLE = Component.text("잠시 후 기물 선택이 시작됩니다.", NamedTextColor.YELLOW);
    private static final Component ERROR_INSPECT_OWN_TEAM_ONLY = Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED);
    private static final Component ERROR_PIECE_ALREADY_TAKEN = Component.text("해당 위치의 기물은 이미 팀원이 선택했습니다!", NamedTextColor.RED);
    private static final Component MSG_READY_COMPLETE = Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN);
    private static final Component MSG_RESET_COMPLETE = Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN);
    private static final Component MSG_ELIMINATED = Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED);
    private static final Component MSG_PROMOTED_TO_KING = Component.text("팀에 킹이 없어 당신이 국왕으로 추대되었습니다!", NamedTextColor.GOLD, TextDecoration.BOLD);
    private static final Component MSG_HOVER_TAKEN = Component.text("이미 팀원이 선택한 기물입니다.", NamedTextColor.RED);
    private static final Component MSG_HOVER_SELECT = Component.text("클릭하여 해당 기물로 참전합니다.", NamedTextColor.GREEN);

    private static final Component ERROR_NOT_READY_PHASE = Component.text("지금은 준비를 완료할 수 있는 시간이 아닙니다!", NamedTextColor.RED);
    private static final Component ERROR_NOT_SELECTION_PHASE = Component.text("지금은 기물을 선택할 수 있는 시간이 아닙니다!", NamedTextColor.RED);
    private static final Component ERROR_ALREADY_READY = Component.text("이미 준비 완료 상태입니다.", NamedTextColor.YELLOW);
    private static final Component ERROR_NOT_TEAM_CHEST = Component.text("자신의 팀 막사에 있는 상자에서만 준비를 완료할 수 있습니다!", NamedTextColor.RED);

    private static final Component ERROR_NO_BOARD = Component.text("체스판이 존재하지 않습니다. 체스판을 먼저 생성해 주세요.", NamedTextColor.RED);
    private static final Component ERROR_NO_PARTICIPANTS = Component.text("참가자가 없습니다.", NamedTextColor.RED);
    private static final Component MSG_BOARD_SETUP_COMPLETE = Component.text("체스판이 설정되었습니다! (3x3 배율)", NamedTextColor.GREEN);
    private static final Component MSG_RECORD_BOOK_GIVEN = Component.text("전투 기록 일지를 지급했습니다.", NamedTextColor.GREEN);
    private static final Component MSG_TURN_ORDER_DECISION = Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("팀 전술 회의 및 순서 결정 단계입니다. 상자를 열어 순서를 정하세요!", NamedTextColor.AQUA, TextDecoration.BOLD));
    private static final Component MSG_WAITING_PREPARATION = Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("모든 플레이어가 기물을 선택했습니다! 10초 후 준비 단계로 넘어갑니다.", NamedTextColor.AQUA, TextDecoration.BOLD));

    private static final Component UI_TITLE_CONFIRMATION = Component.text(" [ 기물 확인 ] ", NamedTextColor.GOLD, TextDecoration.BOLD);
    private static final Component UI_LABEL_TYPE = Component.text("유형: ", NamedTextColor.GRAY);
    private static final Component UI_LABEL_HEALTH = Component.text("체력: ", NamedTextColor.GRAY);
    private static final Component UI_LABEL_DAMAGE = Component.text("공격력: ", NamedTextColor.GRAY);
    private static final Component UI_BTN_TAKEN = Component.text("[ 이미 선택됨 ]", NamedTextColor.RED);
    private static final Component UI_BTN_SELECT = Component.text("[ 이 기물로 결정하기 ]", NamedTextColor.GREEN, TextDecoration.BOLD);
    private static final Component UI_STATS_TITLE = Component.text(" [ 전투 결과 통계 ] ", NamedTextColor.GOLD, TextDecoration.BOLD);
    
    private static final String RECORD_BOOK_TITLE = "전투 기록 일지";
    private static final String RECORD_BOOK_AUTHOR = "ChessWar System";
    private static final Component RECORD_BOOK_HEADER = Component.text("=== 전투 기록 리포트 ===\n\n", NamedTextColor.GOLD, TextDecoration.BOLD);

    private static final int SELECTION_COUNTDOWN_START = 10;
    private static final int PIECE_SELECTION_AUTO_ADVANCE_TIME = 10;
    private static final int DEFAULT_CELL_SIZE = 3;
    private static final int SELECTION_UI_COUNTDOWN_THRESHOLD = 5;
    private static final float SOUND_VOLUME_DEFAULT = 1.0f;
    private static final float SOUND_PITCH_DEFAULT = 1.0f;

    private final JavaPlugin plugin;
    private final GameContext context;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final PieceState pieceState;
    private final TimerManager timerManager;
    private final EnvironmentManager environmentManager;
    private final BoardVisualManager boardVisualManager;
    private final MoveValidator moveValidator;
    private final CombatManager combatManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerInventoryAdapter inventoryAdapter;

    private org.bukkit.scheduler.BukkitTask heartbeatTask;

    private final NamespacedKey keyPieceType;
    private final NamespacedKey keyPieceTeam;
    private final NamespacedKey keyCoordX;
    private final NamespacedKey keyCoordY;

    public GameManager(
            final JavaPlugin plugin,
            final GameContext context,
            final BoardManager boardManager,
            final PieceManager pieceManager,
            final PieceState pieceState,
            final TimerManager timerManager,
            final EnvironmentManager environmentManager,
            final BoardVisualManager boardVisualManager,
            final MoveValidator moveValidator,
            final CombatManager combatManager,
            final ScoreboardManager scoreboardManager
    ) {
        this.plugin = plugin;
        this.context = context;
        this.boardManager = boardManager;
        this.pieceManager = pieceManager;
        this.pieceState = pieceState;
        this.timerManager = timerManager;
        this.environmentManager = environmentManager;
        this.boardVisualManager = boardVisualManager;
        this.moveValidator = moveValidator;
        this.combatManager = combatManager;
        this.scoreboardManager = scoreboardManager;
        this.inventoryAdapter = new PlayerInventoryAdapter(plugin, BoardManager.TURN_ORDER_KEY);

        context.currentPhase(GamePhase.WAITING);

        keyPieceType = new NamespacedKey(plugin, KEY_NAME_PIECE_TYPE);
        keyPieceTeam = new NamespacedKey(plugin, KEY_NAME_PIECE_TEAM);
        keyCoordX = new NamespacedKey(plugin, KEY_NAME_COORD_X);
        keyCoordY = new NamespacedKey(plugin, KEY_NAME_COORD_Y);
    }

    public void startHeartbeat() {
        if (heartbeatTask != null) {
            return;
        }

        heartbeatTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                try {
                    gameTick();
                } catch (final Exception e) {
                    log.error("Game heartbeat error: [{}] {}", e.getClass().getSimpleName(), e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    private void gameTick() {
        final boolean wasExpired = timerManager.isExpired();
        timerManager.tick();

        final int remaining = context.remainingSeconds();
        if (context.timerRunning()) {
            onTimerTick(remaining);
        }

        if (!wasExpired && timerManager.isExpired()) {
            onTimerExpire();
        }

        scoreboardManager.tick();
    }

    public GamePhase phase() {
        return context.currentPhase();
    }

    public Map<UUID, Participant> participants() {
        return context.participants();
    }

    public PhaseTimerSettings timerSettings() {
        return context.timerSettings();
    }

    public Optional<Participant> findParticipant(final UUID playerId) {
        return Optional.ofNullable(context.participants().get(playerId));
    }

    public boolean isParticipant(final Player player) {
        return context.participants().containsKey(player.getUniqueId());
    }

    public boolean isHungerProtected(final Player player) {
        return isParticipant(player);
    }

    public int countTeam(final Team team) {
        return context.countTeam(team);
    }

    public Statistics stats(final UUID playerId) {
        final Participant participant = context.participants().get(playerId);

        return participant != null ? participant.statistics() : new Statistics();
    }

    public boolean isReady(final UUID playerId) {
        final Participant participant = context.participants().get(playerId);

        return participant != null && participant.ready();
    }

    public boolean areAllParticipantsReady() {
        return context.areAllParticipantsReady();
    }

    public int countReady(final Team team) {
        return context.countReady(team);
    }

    public boolean areAllPiecesSelected() {
        return context.areAllPiecesSelected();
    }

    public Optional<UUID> currentTurnPlayer() {
        return Optional.ofNullable(context.currentTurnPlayerId());
    }

    public Optional<Coordinate> findCommandTarget(final UUID commanderId) {
        final Participant participant = context.participants().get(commanderId);

        return participant == null ? Optional.empty() : Optional.ofNullable(participant.commanderTarget());
    }

    public void startGame(final Player player) {
        if (!boardManager.hasBoard()) {
            player.sendMessage(ERROR_NO_BOARD);
            return;
        }

        if (context.participants().isEmpty()) {
            player.sendMessage(ERROR_NO_PARTICIPANTS);
            return;
        }

        advancePhase();
    }

    public void setupBoard(final Player admin) {
        if (context.currentPhase() != GamePhase.WAITING) {
            admin.sendMessage(Component.text("대기 단계에서만 체스판을 설정할 수 있습니다!", NamedTextColor.RED));
            return;
        }

        final BlockFace forward = getCardinalDirection(admin.getLocation());
        final ChessBoard board = new ChessBoard(admin.getLocation(), forward, DEFAULT_CELL_SIZE);

        boardManager.updateBoard(board);
        environmentManager.configure(board.origin().getWorld());

        admin.sendMessage(MSG_BOARD_SETUP_COMPLETE);
        admin.sendMessage(Component.text("기준점: " + formatLocation(board.origin()), NamedTextColor.GRAY));
        admin.sendMessage(Component.text(
                "방향: %s | 칸 크기: %s".formatted(board.forward(), board.cellSize()),
                NamedTextColor.GRAY
        ));
        boardVisualManager.visualizeBoardOutline(admin, board);
    }

    public void provideRecordBook(final Player admin) {
        final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        final BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null) {
            return;
        }

        meta.setTitle(RECORD_BOOK_TITLE);
        meta.setAuthor(RECORD_BOOK_AUTHOR);

        Component content = RECORD_BOOK_HEADER;

        for (final Participant p : context.participants().values()) {
            final Statistics s = p.statistics();
            final Player participantPlayer = Bukkit.getPlayer(p.playerId());
            final String name = (participantPlayer != null) ? participantPlayer.getName() : "오프라인";

            content = content.append(Component.text()
                    .append(Component.text("-" + name + " (" + p.team().teamName() + ")\n", p.team().color()))
                    .append(Component.text("  가한 피해: " + (int) s.getDamageDealt() + "\n", NamedTextColor.DARK_GRAY))
                    .append(Component.text("  받은 피해: " + (int) s.getDamageTaken() + "\n", NamedTextColor.DARK_GRAY))
                    .append(Component.text("  킬/데스: " + s.getKills() + "/" + s.getDeaths() + "\n\n", NamedTextColor.DARK_GRAY))
                    .build());
        }

        meta.addPages(content);
        book.setItemMeta(meta);
        admin.getInventory().addItem(book);
        admin.sendMessage(MSG_RECORD_BOOK_GIVEN);
    }

    private BlockFace getCardinalDirection(final Location location) {
        final float yaw = (location.getYaw() + 360) % 360;

        if (yaw <= 45 || yaw > 315) {
            return BlockFace.SOUTH;
        } else if (yaw > 45 && yaw <= 135) {
            return BlockFace.WEST;
        } else if (yaw > 135 && yaw <= 225) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }

    private String formatLocation(final Location location) {
        return String.format("%.0f, %.0f, %.0f", location.getX(), location.getY(), location.getZ());
    }

    public void advancePhase() {
        final GamePhase nextPhase = switch (context.currentPhase()) {
            case WAITING -> GamePhase.PIECE_SELECTION;
            case PIECE_SELECTION -> GamePhase.TURN_ORDER;
            case TURN_ORDER -> GamePhase.BATTLE;
            case BATTLE -> GamePhase.ENDED;
            case ENDED -> GamePhase.WAITING;
        };

        context.currentPhase(nextPhase);
        scoreboardManager.handlePhaseChange(nextPhase);

        switch (nextPhase) {
            case PIECE_SELECTION -> {
                broadcast(MSG_COUNTDOWN_SUBTITLE);
                timerManager.startTimer(SELECTION_COUNTDOWN_START);
            }
            case TURN_ORDER -> {
                timerManager.startTimer(context.timerSettings().turnOrderSelectionTime());
                boardManager.setupTurnOrderChests(countTeam(Team.WHITE), countTeam(Team.BLACK));
                broadcast(MSG_TURN_ORDER_DECISION);
            }
            case BATTLE -> prepareBattleContext();
            case ENDED -> prepareEndContext();
            case WAITING -> reset();
        }
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
        context.reset();
        context.currentPhase(GamePhase.WAITING);

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());

            if (player != null) {
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                player.setFoodLevel(20);
                player.setSaturation(5.0f);
            }
        }

        pieceManager.processFullGameReset();
        boardVisualManager.clearAllGuides();
        boardManager.clearBarracksChests();

        if (boardManager.hasBoard()) {
            environmentManager.configure(boardManager.currentBoard().origin().getWorld());
        }

        combatManager.resetAllStats();
        scoreboardManager.reset();

        Bukkit.broadcast(MSG_RESET_COMPLETE);
    }

    public void eliminate(final UUID playerId) {
        final Participant participant = context.participants().get(playerId);

        if (participant == null) {
            return;
        }

        participant.statistics().addDeath();

        final Player player = Bukkit.getPlayer(playerId);

        if (player != null) {
            player.sendMessage(MSG_ELIMINATED);
            player.setGameMode(GameMode.SPECTATOR);
            PieceItemUtils.removePlayerPieceItems(player);
        }

        checkVictoryConditions();
    }

    public void join(final Player player, final Team team) {
        context.participants().put(player.getUniqueId(), Participant.of(player.getUniqueId(), team));
        player.sendMessage(Component.text("[Admin] " + team.teamName() + "에 강제 참가했습니다!", team.color()));
    }

    public void movePiece(final Player player) {
        if (combatManager.handleMove(player)) {
            updateVisualGuide(player);
            nextTurn();
        }
    }

    public void attackPiece(final Player attacker, final LivingEntity victim) {
        if (combatManager.handleAttack(attacker, victim)) {
            updateVisualGuide(attacker);
            nextTurn();
        }
    }

    public boolean canInspectBarracksPiece(final Player player) {
        return phase() == GamePhase.PIECE_SELECTION && isParticipant(player);
    }

    public boolean inspectBarracksPiece(final Player player, final Entity entity) {
        if (!canInspectBarracksPiece(player)) {
            return false;
        }

        if (entity.getPersistentDataContainer().isEmpty()) {
            return false;
        }

        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(keyPieceType, PersistentDataType.STRING)) {
            return false;
        }

        final String typeStr = pdc.get(keyPieceType, PersistentDataType.STRING);
        final String teamStr = pdc.get(keyPieceTeam, PersistentDataType.STRING);
        final int x = pdc.getOrDefault(keyCoordX, PersistentDataType.INTEGER, -1);
        final int y = pdc.getOrDefault(keyCoordY, PersistentDataType.INTEGER, -1);

        if (typeStr == null || teamStr == null || x == -1 || y == -1) {
            return false;
        }

        final PieceType type = PieceType.valueOf(typeStr);
        final Team team = Team.valueOf(teamStr);
        final Coordinate coord = Coordinate.of(x, y);

        final Participant participant = context.participants().get(player.getUniqueId());
        if (participant == null || participant.team() != team) {
            player.sendMessage(ERROR_INSPECT_OWN_TEAM_ONLY);
            return true;
        }

        sendSelectionConfirmation(player, type, coord);
        return true;
    }

    public void selectPiece(final Player player, final Coordinate coordinate) {
        if (context.currentPhase() != GamePhase.PIECE_SELECTION) {
            player.sendMessage(ERROR_NOT_SELECTION_PHASE);
            return;
        }

        processPieceSelection(player, coordinate);

        if (areAllPiecesSelected()) {
            timerManager.accelerateTimerTo(PIECE_SELECTION_AUTO_ADVANCE_TIME);
            Bukkit.broadcast(MSG_WAITING_PREPARATION);
        }
    }

    public void processPieceSelection(final Player player, final Coordinate coordinate) {
        final UUID playerId = player.getUniqueId();
        final Participant participant = context.participants().get(playerId);

        if (participant == null) {
            return;
        }

        if (isCoordinateOccupiedByTeammate(playerId, participant.team(), coordinate)) {
            player.sendMessage(ERROR_PIECE_ALREADY_TAKEN);

            return;
        }

        final PieceType pieceType = ChessFormation.getInitialPieceType(coordinate);

        applyPieceAssignment(participant, player, coordinate, pieceType);
        player.sendMessage(Component.text(pieceType.displayName() + " 기물을 선택했습니다!", NamedTextColor.GOLD));
    }

    public void onTurnStart(final Player player) {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        final Optional<Participant> participant = findParticipant(player.getUniqueId());

        if (participant.isEmpty()) {
            return;
        }

        final Team team = participant.get().team();
        final int teamTime = context.getTeamTime(team);
        final int minTime = context.timerSettings().battleTurnTime();

        timerManager.startTimer(Math.max(teamTime, minTime));

        scoreboardManager.updateTurnLine(player);
        combatManager.updateInvulnerability(player);

        clearVisualGuide(player);
        updateVisualGuide(player);
    }

    public void onTimerTick(final int remaining) {
        if (remaining <= SELECTION_UI_COUNTDOWN_THRESHOLD && remaining > 0) {
            for (final UUID playerId : context.participants().keySet()) {
                final Player player = Bukkit.getPlayer(playerId);

                if (player != null) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
                }
            }
        }

        switch (context.currentPhase()) {
            case PIECE_SELECTION -> {
                if (!context.isSelectionStarted()) {
                    broadcastSelectionCountdown(remaining + 1);
                }
            }
            case TURN_ORDER -> {
                if (areAllParticipantsReady()) {
                    timerManager.accelerateTimerTo(context.timerSettings().readyAccelerateTime());
                }
            }
            case BATTLE -> currentTurnPlayer()
                    .flatMap(this::findParticipant)
                    .ifPresent(participant -> {
                        final Team team = participant.team();
                        final int threshold = context.timerSettings().battleTurnTime();

                        if (context.getTeamTime(team) > threshold) {
                            timerManager.updateTeamTime(team, remaining);
                        }
                    });
            default -> {
            }
        }
    }

    public void onTimerExpire() {
        switch (context.currentPhase()) {
            case PIECE_SELECTION -> {
                if (!context.isSelectionStarted()) {
                    context.isSelectionStarted(true);
                    prepareSelectionContext();
                } else {
                    advancePhase();
                }
            }
            case BATTLE -> nextTurn();
            default -> advancePhase();
        }
    }

    public void handleReadyUp(final Player player, final Location location) {
        registerReady(player);
    }

    public void registerReady(final Player player) {
        if (context.currentPhase() != GamePhase.TURN_ORDER) {
            player.sendMessage(ERROR_NOT_READY_PHASE);
            return;
        }

        final Participant participant = context.participants().get(player.getUniqueId());
        if (participant == null) {
            return;
        }

        if (participant.ready()) {
            player.sendMessage(ERROR_ALREADY_READY);
            return;
        }

        if (!boardManager.isTeamChest(player.getLocation(), participant.team())) {
            player.sendMessage(ERROR_NOT_TEAM_CHEST);
            return;
        }

        participant.ready(true);
        player.sendMessage(MSG_READY_COMPLETE);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
    }

    public boolean handleWoolBreakLeave(final Player player) {
        return context.participants().containsKey(player.getUniqueId());
    }

    public void clearAllVisualGuides() {
        boardVisualManager.clearAllGuides();
    }

    public void clearVisualGuide(final Player player) {
        boardVisualManager.clearGuide(player);
    }

    public void updateVisualGuide(final Player player) {
        final Coordinate from = pieceState.entityToCoordinate().get(player.getUniqueId());
        final Optional<Coordinate> commandTarget = findCommandTarget(player.getUniqueId());
        final Coordinate finalFrom = commandTarget.orElse(from);

        if (finalFrom == null) {
            return;
        }

        final Map<Coordinate, Boolean> validMoves = new HashMap<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                final Coordinate to = Coordinate.of(x, y);
                if (moveValidator.canMove(pieceState, finalFrom, to)) {
                    validMoves.put(to, pieceState.boardPieces().containsKey(to));
                }
            }
        }

        boardVisualManager.showGuide(player, validMoves);
    }

    public void registerCommandTarget(final UUID playerId, final Coordinate target) {
        final Participant participant = context.participants().get(playerId);

        if (participant != null) {
            participant.commanderTarget(target);
        }
    }

    public void clearCommandTarget(final UUID playerId) {
        final Participant participant = context.participants().get(playerId);

        if (participant != null) {
            participant.commanderTarget(null);
        }
    }

    public void nextTurn() {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        context.advanceTurnIndex();

        currentTurnPlayer().ifPresent(playerId -> {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                onTurnStart(player);
            }
        });
    }

    private void prepareSelectionContext() {
        pieceManager.clearSpawnedEntities(false);

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                boardManager.teleportToBarracks(participant.team(), player);
            }
        }

        timerManager.startTimer(context.timerSettings().barracksSelectionTime());
    }

    public void prepareEndContext() {
        timerManager.stopTimer();
        this.displayStatisticsHologram();
    }

    private void prepareBattleContext() {
        final Map<UUID, Integer> playerOrders = new HashMap<>();

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());

            if (player != null) {
                inventoryAdapter.extractTurnOrder(player).ifPresent(order -> {
                    playerOrders.put(participant.playerId(), order);
                });
            }
        }

        calculateTurnOrder(playerOrders);

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());

            if (player == null) {
                continue;
            }

            inventoryAdapter.clearOrderItems(player);

            if (participant.initialCoordinate() != null) {
                boardManager.deployToBattlefield(participant.team(), participant.initialCoordinate(), player);
            }
        }

        pieceManager.spawnInitialLayout(boardManager.currentBoard(), context.participants().values());
        boardManager.setupBarracks(pieceManager);

        currentTurnPlayer().ifPresent(playerId -> {
            final Player firstPlayer = Bukkit.getPlayer(playerId);

            if (firstPlayer != null) {
                onTurnStart(firstPlayer);
            }
        });
    }

    private void calculateTurnOrder(final Map<UUID, Integer> orders) {
        final List<Participant> sorted = new ArrayList<>(context.participants().values());
        Collections.shuffle(sorted);

        sorted.sort(Comparator.comparingInt(p -> {
            return orders.getOrDefault(p.playerId(), 99);
        }));

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).turnOrder(i);
        }

        context.turnOrder(sorted.toArray(new Participant[0]));
        context.currentTurnIndex(0);
    }

    public void checkVictoryConditions() {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        final Set<Team> aliveTeams = context.participants().values().stream()
                .filter(p -> {
                    final Player player = Bukkit.getPlayer(p.playerId());
                    return player != null && player.getGameMode() != GameMode.SPECTATOR;
                })
                .map(Participant::team)
                .collect(Collectors.toSet());

        if (aliveTeams.size() == 1) {
            win(aliveTeams.iterator().next());
        } else if (aliveTeams.isEmpty()) {
            advancePhase();
        } else {
            ensureTeamHasKing();
        }
    }

    public void handlePieceDisappearance(final Entity entity) {
        if (entity instanceof Player player) {

            if (isParticipant(player)) {
                final Coordinate coord = pieceState.entityToCoordinate().get(player.getUniqueId());

                if (coord != null) {
                    pieceManager.removePiece(coord);
                }

                boardVisualManager.clearGuide(player);
                eliminate(player.getUniqueId());
            }
            return;
        }

        if (pieceManager.handlePieceDisappearance(entity)) {
            checkVictoryConditions();
        }
    }

    private void ensureTeamHasKing() {
        for (final Team team : Team.values()) {
            if (hasKing(team)) {
                continue;
            }

            final List<Participant> teamMembers = context.participants().values().stream()
                    .filter(p -> {
                        return p.team() == team;
                    })
                    .filter(p -> {
                        final Player player = Bukkit.getPlayer(p.playerId());
                        return player != null && player.getGameMode() != GameMode.SPECTATOR;
                    })
                    .toList();

            if (teamMembers.isEmpty()) {
                continue;
            }

            final Participant newKing = teamMembers.get(0);
            promoteToKing(newKing);
        }
    }

    private boolean hasKing(final Team team) {
        return pieceState.boardPieces().values().stream()
                .anyMatch(p -> {
                    return p.team() == team && p.type() == PieceType.KING;
                });
    }

    private void promoteToKing(final Participant participant) {
        final Coordinate coord = participant.initialCoordinate();
        if (coord == null) {
            return;
        }

        final Piece newKingPiece = Piece.of(participant.playerId(), participant.team(), PieceType.KING);
        pieceManager.placePiece(coord, newKingPiece);

        final Player player = Bukkit.getPlayer(participant.playerId());
        if (player != null) {
            player.sendMessage(MSG_PROMOTED_TO_KING);
            combatManager.applyStats(player, PieceType.KING);
        }
    }

    private boolean isCoordinateOccupiedByTeammate(final UUID playerId, final Team team, final Coordinate coord) {
        return context.participants().values().stream()
                .filter(p -> {
                    return p.team() == team && !p.playerId().equals(playerId);
                })
                .anyMatch(p -> {
                    return coord.equals(p.initialCoordinate());
                });
    }

    private void applyPieceAssignment(final Participant participant, final Player player, final Coordinate coord, final PieceType type) {
        participant.initialCoordinate(coord);
        combatManager.applyStats(player, type);
    }

    private void sendSelectionConfirmation(final Player player, final PieceType type, final Coordinate coord) {
        final boolean isTaken = context.participants().values().stream()
                .anyMatch(p -> {
                    return coord.equals(p.initialCoordinate());
                });

        final Component message = Component.text()
                .append(UI_TITLE_CONFIRMATION)
                .append(Component.newline())
                .append(UI_LABEL_TYPE)
                .append(Component.text(type.displayName(), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(UI_LABEL_HEALTH)
                .append(Component.text(type.baseHealth() + " HP", NamedTextColor.RED))
                .append(Component.newline())
                .append(UI_LABEL_DAMAGE)
                .append(Component.text(type.baseDamage() + " ATK", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.newline())
                .append(isTaken ?
                        UI_BTN_TAKEN
                                .hoverEvent(HoverEvent.showText(MSG_HOVER_TAKEN)) :
                        UI_BTN_SELECT
                                .clickEvent(ClickEvent.runCommand("/cw selection " + coord.x() + " " + coord.y()))
                                .hoverEvent(HoverEvent.showText(MSG_HOVER_SELECT))
                )
                .build();

        player.sendMessage(message);
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
    }

    private void broadcastSelectionCountdown(final int seconds) {
        final Title title = Title.title(
                Component.text(seconds, NamedTextColor.RED, TextDecoration.BOLD),
                MSG_COUNTDOWN_SUBTITLE
        );

        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                player.showTitle(title);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
            }
        }
    }

    private void broadcast(final Component message) {
        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private void displayStatisticsHologram() {
        final List<Component> lines = new ArrayList<>();
        lines.add(UI_STATS_TITLE);
        lines.add(Component.empty());

        participants().values().forEach(p -> {
            final Statistics s = stats(p.playerId());
            final Player player = Bukkit.getPlayer(p.playerId());
            final String name = (player != null) ? player.getName() : "오프라인";

            lines.add(Component.text()
                    .append(Component.text(name, p.team().color()))
                    .append(Component.text(" | ", NamedTextColor.GRAY))
                    .append(Component.text("⚔" + (int) s.getDamageDealt(), NamedTextColor.RED))
                    .append(Component.text(" 🛡" + (int) s.getDamageTaken(), NamedTextColor.BLUE))
                    .append(Component.text(" ➕" + (int) s.getHealingDone(), NamedTextColor.GREEN))
                    .append(Component.text(" ☠" + s.getKills() + "/" + s.getDeaths(), NamedTextColor.DARK_RED))
                    .build());
        });

        Bukkit.broadcast(Component.join(JoinConfiguration.newlines(), lines));
    }
}
