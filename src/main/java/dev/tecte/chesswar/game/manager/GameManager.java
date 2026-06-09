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
import dev.tecte.chesswar.game.component.TimerPolicy;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PiecePdcMapper;
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
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import dev.tecte.chesswar.game.event.KingDeathEvent;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
public class GameManager implements Listener {
    // Static Feedback Constants
    private static final Component MSG_COUNTDOWN_SUBTITLE = Component.text("잠시 후 기물 선택이 시작됩니다.", NamedTextColor.YELLOW);
    private static final Component ERROR_INSPECT_OWN_TEAM_ONLY = Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED);
    private static final Component ERROR_PIECE_ALREADY_TAKEN = Component.text("해당 위치의 기물은 이미 팀원이 선택했습니다!", NamedTextColor.RED);
    private static final Component MSG_READY_COMPLETE = Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN);
    private static final Component MSG_RESET_COMPLETE = Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN);
    private static final Component MSG_ELIMINATED = Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED);
    private static final Component MSG_HOVER_TAKEN = Component.text("이미 참전 중인 기물입니다.", NamedTextColor.RED);
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

    private static final Component UI_DECORATION_LINE = Component.text("━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH);
    private static final Component UI_TITLE_CONFIRMATION = Component.text(" [ 기물 확인 ] ", NamedTextColor.GOLD, TextDecoration.BOLD);
    
    private static final Component UI_BTN_BASE = Component.text("[ ⚔ ")
            .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
            .append(Component.text(" ]"));
    
    private static final Component UI_BTN_TAKEN = UI_BTN_BASE.color(NamedTextColor.GRAY);
    private static final Component UI_BTN_SELECT = UI_BTN_BASE.color(NamedTextColor.GREEN);
    
    private static final Component UI_STATS_TITLE = Component.text(" [ 전투 결과 통계 ] ", NamedTextColor.GOLD, TextDecoration.BOLD);
    
    private static final String RECORD_BOOK_TITLE = "전투 기록 일지";
    private static final String RECORD_BOOK_AUTHOR = "ChessWar System";
    private static final Component RECORD_BOOK_HEADER = Component.text("=== 전투 기록 리포트 ===\n\n", NamedTextColor.GOLD, TextDecoration.BOLD);

    private static final int SELECTION_COUNTDOWN_START = 3;
    private static final int PIECE_SELECTION_AUTO_ADVANCE_TIME = 10;
    private static final int DEFAULT_CELL_SIZE = 3;
    private static final int SELECTION_UI_COUNTDOWN_THRESHOLD = 5;
    private static final float SOUND_VOLUME_DEFAULT = 1.0f;
    private static final float SOUND_PITCH_DEFAULT = 1.0f;

    private static final Component MSG_LEAVE_TEAM = Component.text(" 팀에서 퇴장했습니다.", NamedTextColor.YELLOW);
    private static final Component MSG_SELECTION_GUIDE = Component.text("기물을 우클릭하여 참전할 기물을 선택해주세요.", NamedTextColor.RED);
    private static final Component MSG_SELECTION_WAITING = Component.text("기물 선택 완료! 다른 플레이어를 기다리는 중...", NamedTextColor.GREEN);
    private static final Component MSG_SELECTION_COMPLETED = Component.text("기물 선택이 모두 완료되었습니다!", NamedTextColor.AQUA);
    private static final Component MSG_START_BIND_PROMPT = Component.text("설정할 버튼을 좌클릭(타격) 하세요.", NamedTextColor.YELLOW);
    private static final Component MSG_START_BIND_SUCCESS = Component.text("게임 시작 버튼이 성공적으로 등록되었습니다!", NamedTextColor.GREEN);
    private static final Component MSG_START_REMOVE_SUCCESS = Component.text("게임 시작 버튼이 제거되었습니다.", NamedTextColor.YELLOW);
    private static final Component MSG_START_BUTTON_HOLOGRAM = Component.text("게임 시작", NamedTextColor.GOLD, TextDecoration.BOLD);
    
    private static final Component MSG_TURN_ORDER_GUIDE = Component.text("상자에서 순서 아이템을 가져가 턴 순서를 정하세요!", NamedTextColor.YELLOW);
    private static final Component MSG_TURN_ORDER_APPLIED = Component.text("순서 확정: ", NamedTextColor.GREEN);

    private final JavaPlugin plugin;
    private final GameContext context;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final PieceState pieceState;
    private final PiecePdcMapper pdcMapper;
    private final TimerManager timerManager;
    private final BossBarManager bossBarManager;
    private final EnvironmentManager environmentManager;
    private final BoardVisualManager boardVisualManager;
    private final MoveValidator moveValidator;
    private final CombatManager combatManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerInventoryAdapter inventoryAdapter;

    private org.bukkit.scheduler.BukkitTask heartbeatTask;
    private int lastDisplayedSecond = -1;
    private long tickCount = 0;
    private boolean lastWeaponHeldState = false;

    public GameManager(
            final JavaPlugin plugin,
            final GameContext context,
            final BoardManager boardManager,
            final PieceManager pieceManager,
            final PieceState pieceState,
            final PiecePdcMapper pdcMapper,
            final TimerManager timerManager,
            final EnvironmentManager environmentManager,
            final BoardVisualManager boardVisualManager,
            final MoveValidator moveValidator,
            final CombatManager combatManager,
            final ScoreboardManager scoreboardManager,
            final PlayerInventoryAdapter inventoryAdapter
    ) {
        this.plugin = plugin;
        this.context = context;
        this.boardManager = boardManager;
        this.pieceManager = pieceManager;
        this.pieceState = pieceState;
        this.pdcMapper = pdcMapper;
        this.timerManager = timerManager;
        this.bossBarManager = new BossBarManager(context);
        this.environmentManager = environmentManager;
        this.boardVisualManager = boardVisualManager;
        this.moveValidator = moveValidator;
        this.combatManager = combatManager;
        this.scoreboardManager = scoreboardManager;
        this.inventoryAdapter = inventoryAdapter;

        context.currentPhase(GamePhase.WAITING);
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
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }

        if (context.startButtonHologram() != null) {
            context.startButtonHologram().remove();
            context.startButtonHologram(null);
        }
    }

    private void gameTick() {
        tickCount++;
        final boolean wasExpired = timerManager.isExpired();

        final int remaining = context.remainingSeconds();
        if (context.timerRunning()) {
            onTimerTick(remaining);
        }

        // TURN_ORDER 단계인 경우 2틱마다 인벤토리 상태를 스캔하여 UI(스코어보드, 액션바) 갱신
        if (context.currentPhase() == GamePhase.TURN_ORDER && tickCount % 2 == 0) {
            scoreboardManager.updateAll();
            sendTurnOrderActionBarGuidance();
        }

        // BATTLE 단계인 경우 현재 턴 플레이어의 무기 소지 상태를 1틱마다 폴링 (상태 캐싱 적용)
        if (context.currentPhase() == GamePhase.BATTLE) {
            currentTurnPlayer().ifPresent(playerId -> {
                final Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    final boolean isHolding = PieceItemUtils.isPieceItem(player.getInventory().getItemInMainHand());
                    if (isHolding != lastWeaponHeldState) {
                        if (isHolding) {
                            updateVisualGuide(player);
                        } else {
                            clearVisualGuide(player);
                        }
                        lastWeaponHeldState = isHolding;
                    }
                }
            });
        }

        timerManager.tick();
        bossBarManager.tick();

        if (!wasExpired && timerManager.isExpired()) {
            onTimerExpire();
        }
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

        // 게임 시작 시점의 플레이어 상태(모드, 체력, 공격력)를 저장
        for (final Participant participant : context.participants().values()) {
            final Player p = Bukkit.getPlayer(participant.playerId());
            if (p != null) {
                participant.originalGameMode(p.getGameMode());
                
                final org.bukkit.attribute.AttributeInstance maxHealth = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    participant.originalHealth(maxHealth.getBaseValue());
                }
                
                final org.bukkit.attribute.AttributeInstance attackDamage = p.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
                if (attackDamage != null) {
                    participant.originalAttackDamage(attackDamage.getBaseValue());
                }
            }
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
        scoreboardManager.updateAll();

        switch (nextPhase) {
            case PIECE_SELECTION -> {
                broadcast(MSG_COUNTDOWN_SUBTITLE);
                pieceManager.clearSpawnedEntities(false);
                pieceManager.spawnBunker(boardManager.currentBoard());

                lastDisplayedSecond = SELECTION_COUNTDOWN_START;
                broadcastSelectionCountdown(SELECTION_COUNTDOWN_START);
                timerManager.startTimer(SELECTION_COUNTDOWN_START, TimerPolicy.IMMEDIATE);
            }
            case TURN_ORDER -> {
                timerManager.startTimer(context.timerSettings().turnOrderSelectionTime());
                boardManager.setupTurnOrderChests(countTeam(Team.WHITE), countTeam(Team.BLACK));
                broadcast(MSG_TURN_ORDER_DECISION);
                sendTurnOrderActionBarGuidance();
            }
            case BATTLE -> prepareBattleContext();
            case ENDED -> prepareEndContext();
            case WAITING -> reset();
        }
    }

    @EventHandler
    public void onKingDeath(final KingDeathEvent event) {
        win(event.getWinnerTeam());
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
        lastDisplayedSecond = -1;

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());

            if (player != null) {
                inventoryAdapter.clearOrderItems(player);
                if (participant.originalGameMode() != null) {
                    player.setGameMode(participant.originalGameMode());
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                }

                if (participant.originalHealth() != null && participant.originalAttackDamage() != null) {
                    combatManager.restoreStats(player, participant.originalHealth(), participant.originalAttackDamage());
                } else {
                    combatManager.resetStats(player);
                }
                
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

    public void clearOrderItems(final Player player) {
        inventoryAdapter.clearOrderItems(player);
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
        context.participants().put(player.getUniqueId(), Participant.of(player.getUniqueId(), player.getName(), team, null));
        scoreboardManager.updateAll();
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
        } else {
            // 킹의 지휘 대상 변경 등 턴이 넘어가지 않는 상호작용 후에도 가이드 즉각 갱신
            updateVisualGuide(attacker);
        }
    }

    public boolean canInspectBarracksPiece(final Player player) {
        return phase() == GamePhase.PIECE_SELECTION && isParticipant(player);
    }

    public boolean inspectBarracksPiece(final Player player, final Entity entity) {
        if (!canInspectBarracksPiece(player)) {
            return false;
        }

        final Optional<PieceType> typeOpt = pdcMapper.readType(entity);
        final Optional<Team> teamOpt = pdcMapper.readTeam(entity);
        final Optional<Coordinate> coordOpt = pdcMapper.readCoordinate(entity);

        if (typeOpt.isEmpty() || teamOpt.isEmpty() || coordOpt.isEmpty()) {
            return false;
        }

        final PieceType type = typeOpt.get();
        final Team team = teamOpt.get();
        final Coordinate coord = coordOpt.get();

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
            if (context.remainingSeconds() > PIECE_SELECTION_AUTO_ADVANCE_TIME) {
                timerManager.accelerateTimerTo(PIECE_SELECTION_AUTO_ADVANCE_TIME);
                Bukkit.broadcast(MSG_WAITING_PREPARATION);
            } else {
                Bukkit.broadcast(Component.text("모든 플레이어가 기물을 선택했습니다!", NamedTextColor.AQUA, TextDecoration.BOLD));
            }
        }

        sendSelectionActionBarGuidance();
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

        final Optional<Participant> participantOpt = findParticipant(player.getUniqueId());
        if (participantOpt.isEmpty()) {
            return;
        }

        final Participant participant = participantOpt.get();
        final Team team = participant.team();

        // 턴 시작 시 월급 지급 (100G)
        participant.gold(participant.gold() + 100);
        player.sendMessage(Component.text(" [!] ", NamedTextColor.GOLD)
                .append(Component.text("턴 시작 월급 100G를 지급받았습니다.", NamedTextColor.YELLOW)));

        final int teamTime = context.getTeamTime(team);
        final int overtimeTime = context.timerSettings().battleTurnTime();

        // 공유 시간이 남아있으면 공유 시간 사용, 아니면 초읽기(30초) 사용
        if (teamTime > 0) {
            timerManager.startTimer(teamTime);
            context.setTeamOvertime(team, false);
        } else {
            timerManager.startTimer(overtimeTime);
            context.setTeamOvertime(team, true);
        }

        scoreboardManager.updateTurnLine(player);
        scoreboardManager.updateAll();
        combatManager.updateInvulnerability(player);

        clearVisualGuide(player);
        updateVisualGuide(player);
    }

    public void onTimerTick(final int remaining) {
        switch (context.currentPhase()) {
            case PIECE_SELECTION -> {
                if (!context.isSelectionStarted()) {
                    if (remaining > 0 && remaining != lastDisplayedSecond) {
                        broadcastSelectionCountdown(remaining);
                        lastDisplayedSecond = remaining;
                    }
                } else if (remaining != lastDisplayedSecond) {
                    sendSelectionActionBarGuidance();
                    
                    if (remaining <= SELECTION_UI_COUNTDOWN_THRESHOLD && remaining > 0) {
                        playCountdownTickSound();
                    }
                    
                    lastDisplayedSecond = remaining;
                }
            }
            case TURN_ORDER -> {
                sendTurnOrderActionBarGuidance();

                if (remaining != lastDisplayedSecond) {
                    if (remaining <= SELECTION_UI_COUNTDOWN_THRESHOLD && remaining > 0) {
                        playCountdownTickSound();
                    }
                    lastDisplayedSecond = remaining;
                }
                
                if (areAllParticipantsReady()) {
                    timerManager.accelerateTimerTo(context.timerSettings().readyAccelerateTime());
                }
            }
            case BATTLE -> {
                if (remaining != lastDisplayedSecond) {
                    scoreboardManager.updateTimer(remaining);
                    if (remaining <= SELECTION_UI_COUNTDOWN_THRESHOLD && remaining > 0) {
                        playCountdownTickSound();
                    }
                    lastDisplayedSecond = remaining;
                }
            }
            default -> {
            }
        }
    }

    private void playCountdownTickSound() {
        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
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
                    finalizePieceSelection();
                    advancePhase();
                }
            }
            case TURN_ORDER -> {
                advancePhase();
            }
            case BATTLE -> nextTurn();
            default -> advancePhase();
        }
    }

    public void handleReadyUp(final Player player, final Location location) {
        if (context.currentPhase() != GamePhase.TURN_ORDER) {
            player.sendMessage(ERROR_NOT_READY_PHASE);
            return;
        }

        final Participant participant = context.participants().get(player.getUniqueId());
        if (participant == null) {
            return;
        }

        // 자신이 속한 팀의 상자인지 검증
        if (!boardManager.isTeamChest(location, participant.team())) {
            player.sendMessage(Component.text("자신의 팀 상자에서만 준비를 완료할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        registerReady(participant.team());
    }

    public void registerReady(final Team team) {
        final List<Participant> teamMembers = context.participants().values().stream()
                .filter(p -> p.team() == team)
                .toList();

        final boolean isAnyReady = teamMembers.stream().anyMatch(Participant::ready);
        if (isAnyReady) {
            return;
        }

        boardManager.disableReadyButton(team);

        for (final Participant p : teamMembers) {
            p.ready(true);
            final Player pObj = Bukkit.getPlayer(p.playerId());
            if (pObj != null) {
                pObj.sendMessage(MSG_READY_COMPLETE);
                pObj.playSound(pObj, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
            }
        }
    }

    public void processWoolBreakLeave(final Player player, final Material material) {
        if (context.currentPhase() != GamePhase.WAITING) {
            return;
        }

        final Participant participant = context.participants().get(player.getUniqueId());
        if (participant == null) {
            return;
        }

        final boolean isCorrectWool = (participant.team() == Team.WHITE && material == Material.WHITE_WOOL)
                || (participant.team() == Team.BLACK && material == Material.BLACK_WOOL);

        if (isCorrectWool) {
            context.participants().remove(player.getUniqueId());
            scoreboardManager.remove(player);
            scoreboardManager.updateAll();
            player.sendMessage(Component.text(participant.team().teamName()).append(MSG_LEAVE_TEAM));
        }
    }

    public void clearAllVisualGuides() {
        boardVisualManager.clearAllGuides();
    }

    public void clearVisualGuide(final Player player) {
        boardVisualManager.clearGuide(player);
    }

    public void updateVisualGuide(final Player player) {
        // 자신의 턴이 아니거나 전용 무기를 들고 있지 않으면 가이드를 표시하지 않음
        final UUID turnPlayerId = context.currentTurnPlayerId();
        if (turnPlayerId == null || !turnPlayerId.equals(player.getUniqueId())) {
            return;
        }

        if (!PieceItemUtils.isPieceItem(player.getInventory().getItemInMainHand())) {
            boardVisualManager.clearGuide(player);
            return;
        }

        final Coordinate from = pieceManager.findCoordinate(player).orElse(null);
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

    public void setStartButtonBindingMode(final Player admin) {
        context.bindingAdmin(admin.getUniqueId());
        admin.sendMessage(MSG_START_BIND_PROMPT);
    }

    public boolean isBindingAdmin(final UUID uuid) {
        return context.bindingAdmin() != null && context.bindingAdmin().equals(uuid);
    }

    public void loadConfig() {
        final File file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            return;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        final Location loc = config.getLocation("start_button_location");
        if (loc != null) {
            context.startButtonLocation(loc);
            updateStartButtonHologram();
        }
    }

    public void saveConfig() {
        final File file = new File(plugin.getDataFolder(), "data.yml");
        final FileConfiguration config = new YamlConfiguration();

        if (context.startButtonLocation() != null) {
            config.set("start_button_location", context.startButtonLocation());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Failed to save data.yml", e);
        }
    }

    public void bindStartButton(final Player admin, final Location location) {
        context.startButtonLocation(location);
        context.bindingAdmin(null);
        updateStartButtonHologram();
        saveConfig();
        admin.sendMessage(MSG_START_BIND_SUCCESS);
    }

    public void removeStartButton(final Player admin) {
        context.startButtonLocation(null);
        updateStartButtonHologram();
        saveConfig();
        admin.sendMessage(MSG_START_REMOVE_SUCCESS);
    }

    public void updateStartButtonHologram() {
        if (context.startButtonHologram() != null) {
            context.startButtonHologram().remove();
            context.startButtonHologram(null);
        }

        final Location loc = context.startButtonLocation();
        if (loc == null || loc.getWorld() == null) {
            return;
        }

        final Location hologramLoc = loc.clone().add(0.5, 1.2, 0.5);
        final TextDisplay textDisplay = loc.getWorld().spawn(hologramLoc, TextDisplay.class, display -> {
            display.text(MSG_START_BUTTON_HOLOGRAM);
            display.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            display.setShadowed(true);
        });

        context.startButtonHologram(textDisplay);
    }

    public boolean isStartButton(final Location location) {
        if (context.startButtonLocation() == null || location == null) {
            return false;
        }

        return context.startButtonLocation().getBlock().getLocation().equals(location.getBlock().getLocation());
    }

    public void nextTurn() {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        currentTurnPlayer().ifPresent(playerId -> {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                combatManager.clearCommanderVisuals(player);
            }
        });

        context.advanceTurnIndex();

        currentTurnPlayer().ifPresent(playerId -> {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                onTurnStart(player);
            }
        });

        scoreboardManager.updateAll();
    }

    private void finalizePieceSelection() {
        for (final Team team : Team.values()) {
            final List<Participant> teamMembers = context.participants().values().stream()
                    .filter(p -> p.team() == team)
                    .toList();

            if (teamMembers.isEmpty()) continue;

            final boolean hasKing = teamMembers.stream()
                    .anyMatch(p -> p.selectedType() == PieceType.KING);

            if (!hasKing) {
                final List<Participant> unselected = teamMembers.stream()
                        .filter(p -> p.selectedType() == null)
                        .toList();

                final Participant target = unselected.isEmpty()
                        ? teamMembers.get((int) (Math.random() * teamMembers.size()))
                        : unselected.get((int) (Math.random() * unselected.size()));

                forceAssignPiece(target, PieceType.KING);
            }
        }

        // 남은 미선택자들 랜덤 배정
        for (final Participant p : context.participants().values()) {
            if (p.selectedType() == null) {
                final PieceType randomType = PieceType.values()[(int) (Math.random() * (PieceType.values().length - 1)) + 1]; // KING 제외 랜덤
                forceAssignPiece(p, randomType);
            }
        }
    }

    private void forceAssignPiece(final Participant participant, final PieceType type) {
        final Player player = Bukkit.getPlayer(participant.playerId());
        if (player == null) return;

        // 해당 팀의 기물 타입에 맞는 초기 레이아웃 좌표를 검색하여 할당
        final Coordinate targetCoord = ChessFormation.getFullInitialLayout().entrySet().stream()
                .filter(entry -> entry.getValue() == type && ChessFormation.getTeamAt(entry.getKey()) == participant.team())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(Coordinate.of(3, teamPieceRow(participant.team())));

        applyPieceAssignment(participant, player, targetCoord, type);
    }

    private int teamPieceRow(final Team team) {
        return team == Team.WHITE ? 0 : 7;
    }

    private void prepareSelectionContext() {
        // 막사 이동 시점에 벙커 기물을 막사로 배치
        pieceManager.deployBunkerToBarracks();

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                player.clearTitle();
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
        // 전투 단계 진입 시 벙커 기물을 전장으로 배치
        pieceManager.deployBunkerToBattlefield(boardManager.currentBoard(), context.participants().values());

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

        // 전투 단계 진입 전 막사 상자 정리
        boardManager.clearBarracksChests();

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

        final Set<Team> aliveTeams = java.util.Arrays.stream(Team.values())
                .filter(this::hasKing)
                .collect(Collectors.toSet());

        if (aliveTeams.size() == 1) {
            win(aliveTeams.iterator().next());
        } else if (aliveTeams.isEmpty()) {
            advancePhase();
        }
    }

    public void processPieceDeath(final Entity entity) {
        if (entity instanceof Player player) {
            if (isParticipant(player)) {
                final Coordinate coord = pieceManager.findCoordinate(player).orElse(null);
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

    public void processPieceUnload(final Entity entity) {
        if (entity instanceof Player) {
            return;
        }

        pieceManager.handlePieceDisappearance(entity);
    }

    private boolean hasKing(final Team team) {
        return pieceState.boardPieces().values().stream()
                .anyMatch(p -> {
                    return p.team() == team && p.type() == PieceType.KING;
                });
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
        participant.selectedType(type);
        combatManager.applyStats(player, type);
        PieceItemUtils.replacePlayerPieceItem(player, type);
        scoreboardManager.updateAll();
    }

    private void sendSelectionConfirmation(final Player player, final PieceType type, final Coordinate coord) {
        final boolean isTaken = context.participants().values().stream()
                .anyMatch(p -> coord.equals(p.initialCoordinate()));

        final Component titlePanel = Component.text()
                .append(UI_DECORATION_LINE)
                .appendSpace()
                .append(Component.text("[ " + type.symbol() + " " + type.displayName() + " ]", NamedTextColor.GOLD, TextDecoration.BOLD))
                .appendSpace()
                .append(UI_DECORATION_LINE)
                .build();

        final Component statPanel = Component.text()
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) type.baseHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) type.baseDamage(), NamedTextColor.RED))
                .build();

        final Component rangePanel = Component.text()
                .append(Component.text("이동 및 공격 범위: ", NamedTextColor.GRAY))
                .append(Component.text(type.rangeDescription(), NamedTextColor.AQUA))
                .build();

        final Component buttonPanel = isTaken ?
                UI_BTN_TAKEN.hoverEvent(HoverEvent.showText(MSG_HOVER_TAKEN)) :
                UI_BTN_SELECT
                        .clickEvent(ClickEvent.runCommand("/cw select " + coord.x() + " " + coord.y()))
                        .hoverEvent(HoverEvent.showText(MSG_HOVER_SELECT));

        final Component message = Component.join(
                net.kyori.adventure.text.JoinConfiguration.newlines(),
                Component.empty(),
                titlePanel,
                Component.empty(),
                Component.text(type.description(), NamedTextColor.WHITE),
                Component.empty(),
                statPanel,
                rangePanel,
                Component.empty(),
                Component.text().append(Component.text("               ")).append(buttonPanel).build(),
                Component.empty()
        );

        player.sendMessage(message);
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
    }

    private void broadcastSelectionCountdown(final int seconds) {
        final Title title = Title.title(
                Component.text(seconds, NamedTextColor.RED, TextDecoration.BOLD),
                MSG_COUNTDOWN_SUBTITLE,
                Title.Times.times(java.time.Duration.ZERO, java.time.Duration.ofSeconds(1), java.time.Duration.ofMillis(250))
        );

        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                player.showTitle(title);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
            }
        }
    }

    private void sendSelectionActionBarGuidance() {
        final boolean allSelected = areAllPiecesSelected();

        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                final Component guide = allSelected ? MSG_SELECTION_COMPLETED :
                        (participant.hasPiece() ? MSG_SELECTION_WAITING : MSG_SELECTION_GUIDE);
                player.sendActionBar(guide);
            }
        }
    }

    private void sendTurnOrderActionBarGuidance() {
        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player == null) continue;

            final Optional<Integer> order = inventoryAdapter.extractTurnOrder(player);
            if (order.isPresent()) {
                player.sendActionBar(MSG_TURN_ORDER_APPLIED.append(Component.text(order.get() + "번", NamedTextColor.WHITE, TextDecoration.BOLD)));
            } else {
                player.sendActionBar(MSG_TURN_ORDER_GUIDE);
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
