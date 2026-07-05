/*
package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.BoardEnvironmentPresenter;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardVisualManager;
import dev.tecte.chesswar.piece.PieceAbility;
import dev.tecte.chesswar.piece.PieceLayout;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.economy.EconomyManager;
import dev.tecte.chesswar.economy.ShopController;
import dev.tecte.chesswar.piece.ConsumableItemUtils;
import dev.tecte.chesswar.piece.EffectType;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceEffect;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PiecePdcMapper;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@lombok.extern.slf4j.Slf4j(topic = "ChessWar")
public class GameManager implements Listener {
    private static final Component ERROR_INSPECT_OWN_TEAM_ONLY = Component.text("자신의 진영 기물만 살펴볼 수 있습니다!", NamedTextColor.RED);
    private static final Component ERROR_PIECE_ALREADY_TAKEN = Component.text("해당 위치의 기물은 이미 팀원이 선택했습니다!", NamedTextColor.RED);

    private static final Component ERROR_NOT_READY_PHASE = Component.text("지금은 준비를 완료할 수 있는 시간이 아닙니다!", NamedTextColor.RED);
    private static final Component ERROR_NOT_SELECTION_PHASE = Component.text("지금은 기물을 선택할 수 있는 시간이 아닙니다!", NamedTextColor.RED);

    private static final Component ERROR_NO_BOARD = Component.text("체스판이 존재하지 않습니다. 체스판을 먼저 생성해 주세요.", NamedTextColor.RED);
    private static final Component ERROR_NO_PARTICIPANTS = Component.text("참가자가 없습니다.", NamedTextColor.RED);

    private static final int SELECTION_COUNTDOWN_START = 3;
    private static final int PIECE_SELECTION_AUTO_ADVANCE_TIME = 10;
    private static final int DEFAULT_CELL_SIZE = 3;
    private static final int SELECTION_UI_COUNTDOWN_THRESHOLD = 5;

    private final JavaPlugin plugin;
    private final GameContext context;
    private final BoardComponent boardComponent;
    private final BoardEnvironmentPresenter boardEnvPresenter;
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
    private final EconomyManager economyManager;
    private final ShopController shopController;
    private final PlayerInventoryAdapter inventoryAdapter;
    private final GameAnnouncer announcer;

    private org.bukkit.scheduler.BukkitTask heartbeatTask;
    private int lastDisplayedSecond = -1;
    private long tickCount = 0;
    private boolean lastWeaponHeldState = false;

    public GameManager(
            final JavaPlugin plugin,
            final GameContext context,
            final BoardComponent boardComponent,
            final BoardEnvironmentPresenter boardEnvPresenter,
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
            final EconomyManager economyManager,
            final ShopController shopController,
            final PlayerInventoryAdapter inventoryAdapter,
            final GameAnnouncer announcer
    ) {
        this.plugin = plugin;
        this.context = context;
        this.boardComponent = boardComponent;
        this.boardEnvPresenter = boardEnvPresenter;
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
        this.economyManager = economyManager;
        this.shopController = shopController;
        this.inventoryAdapter = inventoryAdapter;
        this.announcer = announcer;

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
            announcer.sendTurnOrderActionBarGuidance(inventoryAdapter);
        }

        // BATTLE 단계인 경우 현재 턴 플레이어의 무기 소지 상태를 1틱마다 폴링 (상태 캐싱 적용)
        if (context.currentPhase() == GamePhase.BATTLE) {
            final UUID playerId = currentTurnPlayer();
            if (playerId != null) {
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
            }
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

    @Nullable
    public Participant findParticipant(final UUID playerId) {
        return context.participant(playerId);
    }

    public boolean isParticipant(final Player player) {
        return context.isParticipant(player.getUniqueId());
    }

    public boolean isHungerProtected(final Player player) {
        return isParticipant(player);
    }

    public int countTeam(final Team team) {
        return context.countTeam(team);
    }

    public boolean areAllParticipantsReady() {
        return context.areAllParticipantsReady();
    }

    public boolean areAllPiecesSelected() {
        if (context.isParticipantsEmpty()) {
            return false;
        }

        for (final Participant p : context.participantsValues()) {
            if (p.getPiece(pieceState) == null) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    public UUID currentTurnPlayer() {
        return context.currentTurnPlayerId();
    }

    @Nullable
    public Coordinate findCommandTarget(final UUID commanderId) {
        final Participant participant = context.participant(commanderId);
        return participant != null ? participant.commanderTarget() : null;
    }

    public void startGame(final Player player) {
        if (!boardComponent.hasBoard()) {
            announcer.announceCombatError(player, ERROR_NO_BOARD);
            return;
        }

        if (context.isParticipantsEmpty()) {
            announcer.announceCombatError(player, ERROR_NO_PARTICIPANTS);
            return;
        }

        for (final Participant participant : context.participantsValues()) {
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
            announcer.announceAdminMessage(admin, Component.text("대기 단계에서만 체스판을 설정할 수 있습니다!", NamedTextColor.RED));
            return;
        }

        final BlockFace forward = getCardinalDirection(admin.getLocation());
        final ChessBoard board = ChessBoard.of(admin.getLocation(), forward, DEFAULT_CELL_SIZE);

        boardManager.updateBoard(board);
        environmentManager.configure(board.origin().getWorld());

        announcer.announceAdminMessage(admin, GameAnnouncer.MSG_BOARD_SETUP_COMPLETE);
        announcer.announceAdminMessage(admin, Component.text("기준점: " + formatLocation(board.origin()), NamedTextColor.GRAY));
        announcer.announceAdminMessage(admin, Component.text(
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

        meta.setTitle(GameAnnouncer.RECORD_BOOK_TITLE);
        meta.setAuthor(GameAnnouncer.RECORD_BOOK_AUTHOR);

        Component content = GameAnnouncer.RECORD_BOOK_HEADER;

        for (final Participant p : context.participantsValues()) {
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
        announcer.announceAdminMessage(admin, GameAnnouncer.MSG_RECORD_BOOK_GIVEN);
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
        handleExitPhase(context.currentPhase());

        switch (context.advancePhase()) {
            case PIECE_SELECTION -> handleTransitionToPieceSelection();
            case TURN_ORDER -> handleTransitionToTurnOrder();
            case BATTLE -> handleTransitionToBattle();
            case ENDED -> handleTransitionToEnded();
            case WAITING -> handleTransitionToWaiting();
        }

        scoreboardManager.updateAll();
    }

    private void handleExitPhase(final GamePhase phase) {
        switch (phase) {
            case PIECE_SELECTION -> handleExitPieceSelection();
            case TURN_ORDER -> handleExitTurnOrder();
        }
    }

    private void handleExitPieceSelection() {
        if (context.isSelectionStarted()) {
            finalizePieceSelection();
        }

        for (final Participant participant : context.participantsValues()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                PieceItemUtils.removeSelectionItems(player);
            }
        }
    }

    private void handleExitTurnOrder() {
        combatManager.calculateTurnOrder(inventoryAdapter);
        boardEnvPresenter.clearBarracksChests();
    }

    private void handleTransitionToPieceSelection() {
        announcer.broadcast(GameAnnouncer.MSG_COUNTDOWN_SUBTITLE);
        pieceManager.clearSpawnedEntities(false);
        pieceManager.spawnBunker(boardComponent.board());

        lastDisplayedSecond = SELECTION_COUNTDOWN_START;
        announcer.broadcastSelectionCountdown(SELECTION_COUNTDOWN_START);
        timerManager.startTimer(SELECTION_COUNTDOWN_START, TimerPolicy.IMMEDIATE);
    }

    private void handleTransitionToTurnOrder() {
        timerManager.startTimer(context.timerSettings().turnOrderSelectionTime());
        boardEnvPresenter.setupTurnOrderChests(countTeam(Team.WHITE), countTeam(Team.BLACK));
        announcer.broadcast(GameAnnouncer.MSG_TURN_ORDER_DECISION);
        announcer.sendTurnOrderActionBarGuidance(inventoryAdapter);
    }

    private void handleTransitionToBattle() {
        pieceManager.deployBunkerToBattlefield(boardComponent.board());

        for (final Participant participant : context.participantsValues()) {
            final Player player = Bukkit.getPlayer(participant.playerId());

            if (player == null) {
                continue;
            }

            final Piece piece = participant.getPiece(pieceState);

            if (piece != null && piece.type().isLongRange()) {
                player.getInventory().addItem(ConsumableItemUtils.createLeapItem());
            }

            final Coordinate initialCoord = pieceState.coordinate(participant.playerId());

            if (initialCoord == null) {
                continue;
            }

            boardEnvPresenter.deployToBattlefield(participant.team(), initialCoord, player);
        }

        nextTurn();
    }

    private void handleTransitionToEnded() {
        scoreboardManager.clearTurnLine();
        timerManager.stopTimer();
        announcer.displayStatisticsHologram();
    }

    private void handleTransitionToWaiting() {
        reset();
    }

    private void startBarracksSelection() {
        pieceManager.deployBunkerToBarracks();

        for (final Participant participant : context.participantsValues()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                player.clearTitle();
                boardEnvPresenter.teleportToBarracks(participant.team(), player);
            }
        }

        timerManager.startTimer(context.timerSettings().barracksSelectionTime());
    }

    @EventHandler
    public void onPiecePromotion(final PiecePromotionEvent event) {
        promotePiece(event.player(), event.piece(), event.coordinate());
    }

    private void promotePiece(final Player player, final Piece piece, final Coordinate coordinate) {
        final PieceType upgradeType = PieceType.QUEEN;

        final LivingEntity oldEntity = piece.isPlayer() ? Bukkit.getPlayer(piece.id()) : (Bukkit.getEntity(piece.id()) instanceof LivingEntity e ? e : null);
        if (oldEntity != null && !piece.isPlayer()) {
            oldEntity.remove();
        }

        pieceManager.removePiece(coordinate);

        UUID newPieceId = piece.isPlayer() ? piece.id() : null;
        if (!piece.isPlayer()) {
            final Location loc = boardComponent.board().toCenterLocation(coordinate);
            final Vector direction = boardComponent.board().calculateDirection(piece.team());
            final LivingEntity newEntity = pieceManager.spawnPiece(upgradeType, piece.team(), coordinate, loc, direction, false);
            if (newEntity != null) {
                newPieceId = newEntity.getUniqueId();
            }
        }

        if (newPieceId != null) {
            final Piece upgradedPiece = piece.isPlayer()
                    ? Piece.ofPlayer(newPieceId, piece.team(), upgradeType)
                    : Piece.of(newPieceId, piece.team(), upgradeType);
            pieceManager.attachAbilities(upgradedPiece);
            pieceManager.placePiece(coordinate, upgradedPiece);
        }

        Bukkit.broadcast(Component.text()
                .append(Component.text(player.getName(), piece.team().color()))
                .append(Component.text("의 폰이 ", NamedTextColor.GRAY))
                .append(Component.text(upgradeType.displayName(), NamedTextColor.GOLD))
                .append(Component.text("(으)로 승급했습니다!", NamedTextColor.GRAY))
                .build());
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

        for (final Participant participant : context.participantsValues()) {
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
        boardEnvPresenter.clearBarracksChests();

        if (boardComponent.hasBoard()) {
            environmentManager.configure(boardComponent.board().origin().getWorld());
        }

        combatManager.resetAllStats();
        economyManager.reset();
        scoreboardManager.reset();

        Bukkit.broadcast(GameAnnouncer.MSG_RESET_COMPLETE);
    }

    public void clearOrderItems(final Player player) {
        inventoryAdapter.clearOrderItems(player);
    }

    public void eliminate(final UUID playerId) {
        final Participant participant = context.participant(playerId);

        if (participant == null) {
            return;
        }

        participant.statistics().addDeath();

        final Player player = Bukkit.getPlayer(playerId);

        if (player != null) {
            player.sendMessage(GameAnnouncer.MSG_ELIMINATED);
            player.setGameMode(GameMode.SPECTATOR);
            PieceItemUtils.removePlayerPieceItems(player);
        }

        checkVictoryConditions();
    }

    @EventHandler
    public void onPlayerSwapHandItems(final PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();
        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);
            return;
        }

        if (phase() != GamePhase.BATTLE) {
            return;
        }

        event.setCancelled(true);
        plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);
        openShop(player);
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
            return;
        }

        final ClickType click = event.getClick();
        if (click == ClickType.SWAP_OFFHAND || click == ClickType.DROP || click == ClickType.CONTROL_DROP || click == ClickType.NUMBER_KEY) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        useConsumableItem(player, item);
    }

    public void useConsumableItem(final Player player, final ItemStack item) {
        if (item == null) {
            return;
        }

        final String consumableId = ConsumableItemUtils.getConsumableId(item);

        if (consumableId == null) {
            return;
        }

        final UUID turnId = context.currentTurnPlayerId();
        if (phase() != GamePhase.BATTLE || turnId == null || !turnId.equals(player.getUniqueId())) {
            player.sendMessage(Component.text("자신의 전투 턴에만 아이템을 사용할 수 있습니다!", NamedTextColor.RED));
            return;
        }

        final Participant participant = context.participant(player.getUniqueId());
        if (participant == null) {
            return;
        }

        final Piece piece = participant.getPiece(pieceState);
        if (piece == null) {
            return;
        }

        if (ConsumableItemUtils.ID_LEAP.equals(consumableId)) {
            if (piece.hasEffect("도약")) {
                announcer.announceAlreadyLeaping(player);
                return;
            }

            item.setAmount(item.getAmount() - 1);
            piece.addEffect(PieceEffect.of("도약", EffectType.BUFF, 1));

            announcer.announceLeapUsage(player);

            updateVisualGuide(player);
            plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    public void openShop(final Player player) {
        if (phase() != GamePhase.BATTLE) {
            player.sendMessage(Component.text("전투 단계에서만 상점을 이용할 수 있습니다!", NamedTextColor.RED));
            return;
        }

        shopController.openMainShop(player);
    }

    public void join(final Player player, final Team team) {
        context.addParticipant(player.getUniqueId(), Participant.of(player.getUniqueId(), player.getName(), team, null));
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
        final CombatManager.AttackResult result = combatManager.handleAttack(attacker, victim);

        if (result == CombatManager.AttackResult.ACTION_DONE) {
            updateVisualGuide(attacker);
            nextTurn();
        } else if (result == CombatManager.AttackResult.COMMAND_CHANGED) {
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

        final PieceType type = pdcMapper.readType(entity);
        final Team team = pdcMapper.readTeam(entity);
        final Coordinate coord = pdcMapper.readCoordinate(entity);

        if (type == null || team == null || coord == null) {
            return false;
        }

        final Participant participant = context.participant(player.getUniqueId());
        if (participant == null || participant.team() != team) {
            player.sendMessage(ERROR_INSPECT_OWN_TEAM_ONLY);
            return true;
        }

        announcer.sendSelectionConfirmation(player, type, coord, false); // TODO: isTaken 파라미터 처리
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
                Bukkit.broadcast(GameAnnouncer.MSG_WAITING_PREPARATION);
            } else {
                Bukkit.broadcast(Component.text("모든 플레이어가 기물을 선택했습니다!", NamedTextColor.AQUA, TextDecoration.BOLD));
            }
        }

        announcer.sendSelectionActionBarGuidance(areAllPiecesSelected());
    }

    public void processPieceSelection(final Player player, final Coordinate coordinate) {
        final UUID playerId = player.getUniqueId();
        final Participant participant = context.participant(playerId);

        if (participant == null) {
            return;
        }

        if (isCoordinateOccupiedByTeammate(playerId, participant.team(), coordinate)) {
            player.sendMessage(ERROR_PIECE_ALREADY_TAKEN);

            return;
        }

        final PieceType pieceType = PieceLayout.getInitialPieceType(coordinate);

        applyPieceAssignment(participant, player, coordinate, pieceType);
        player.sendMessage(Component.text(pieceType.displayName() + " 기물을 선택했습니다!", NamedTextColor.GOLD));
    }

    public void onTurnStart(final Player player) {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        final Participant participant = findParticipant(player.getUniqueId());
        if (participant == null) {
            return;
        }

        final Piece piece = participant.getPiece(pieceState);
        if (piece != null) {
            piece.tickEffects();
        }
        final Team team = participant.team();

        // 턴 시작 시 월급 지급 (팀 전원)
        economyManager.distributeSalary(team);

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
        combatManager.onTurnStart(player);

        clearVisualGuide(player);
        updateVisualGuide(player);
    }

    public void onTimerTick(final int remaining) {
        switch (context.currentPhase()) {
            case PIECE_SELECTION -> {
                if (!context.isSelectionStarted()) {
                    if (remaining > 0 && remaining != lastDisplayedSecond) {
                        announcer.broadcastSelectionCountdown(remaining);
                        lastDisplayedSecond = remaining;
                    }
                } else if (remaining != lastDisplayedSecond) {
                    announcer.sendSelectionActionBarGuidance(areAllPiecesSelected());

                    if (remaining <= SELECTION_UI_COUNTDOWN_THRESHOLD && remaining > 0) {
                        announcer.playCountdownTickSound();
                    }

                    lastDisplayedSecond = remaining;
                }
            }
            case TURN_ORDER -> {
                announcer.sendTurnOrderActionBarGuidance(inventoryAdapter);

                if (remaining != lastDisplayedSecond) {
                    if (remaining <= SELECTION_UI_COUNTDOWN_THRESHOLD && remaining > 0) {
                        announcer.playCountdownTickSound();
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
                        announcer.playCountdownTickSound();
                    }
                    lastDisplayedSecond = remaining;
                }
            }
            default -> {
            }
        }
    }

    public void onTimerExpire() {
        switch (context.currentPhase()) {
            case PIECE_SELECTION -> {
                if (!context.isSelectionStarted()) {
                    context.isSelectionStarted(true);
                    startBarracksSelection();
                } else {
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
            announcer.announceCombatError(player, ERROR_NOT_READY_PHASE);
            return;
        }

        final Participant participant = context.participant(player.getUniqueId());
        if (participant == null) {
            return;
        }

        // 자신이 속한 팀의 상자인지 검증
        if (!boardManager.isTeamChest(location, participant.team())) {
            announcer.announceCombatError(player, Component.text("자신의 팀 상자에서만 준비를 완료할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        registerReady(participant.team());
    }

    public void registerReady(final Team team) {
        final List<Participant> teamMembers = new ArrayList<>();
        for (final Participant p : context.participantsValues()) {
            if (p.team() == team) {
                teamMembers.add(p);
            }
        }

        boolean isAnyReady = false;
        for (final Participant p : teamMembers) {
            if (p.ready()) {
                isAnyReady = true;
                break;
            }
        }

        if (isAnyReady) {
            return;
        }

        boardEnvPresenter.disableReadyButton(team);

        for (final Participant p : teamMembers) {
            p.ready(true);
            final Player pObj = Bukkit.getPlayer(p.playerId());
            if (pObj != null) {
                announcer.announcePieceSelection(pObj, GameAnnouncer.MSG_READY_COMPLETE);
                pObj.playSound(pObj, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }

    public void processWoolBreakLeave(final Player player, final Material material) {
        if (context.currentPhase() != GamePhase.WAITING) {
            return;
        }

        final Participant participant = context.participant(player.getUniqueId());
        if (participant == null) {
            return;
        }

        final boolean isCorrectWool = (participant.team() == Team.WHITE && material == Material.WHITE_WOOL)
                                      || (participant.team() == Team.BLACK && material == Material.BLACK_WOOL);

        if (isCorrectWool) {
            context.removeParticipant(player.getUniqueId());
            scoreboardManager.remove(player);
            scoreboardManager.updateAll();
            announcer.announcePieceSelection(player, Component.text(participant.team().teamName()).append(GameAnnouncer.MSG_LEAVE_TEAM));
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

        final Coordinate from = pieceManager.findCoordinate(player);
        final Coordinate commandTarget = findCommandTarget(player.getUniqueId());
        final Coordinate finalFrom = commandTarget != null ? commandTarget : from;

        if (finalFrom == null) {
            return;
        }

        final Participant participant = context.participant(player.getUniqueId());
        if (participant == null) {
            return;
        }

        final Piece myPiece = pieceState.piece(finalFrom);
        if (myPiece == null) {
            return;
        }

        final Map<Coordinate, dev.tecte.chesswar.board.GuideType> validMoves = new HashMap<>();

        for (int x = 0; x < Coordinate.BOARD_SIZE; x++) {
            for (int y = 0; y < Coordinate.BOARD_SIZE; y++) {
                final Coordinate to = Coordinate.of(x, y);

                if (moveValidator.canMove(pieceState, finalFrom, to)) {
                    validMoves.put(to, pieceState.hasPiece(to) ? dev.tecte.chesswar.board.GuideType.CAPTURE : dev.tecte.chesswar.board.GuideType.MOVE);
                } else if (moveValidator.canReach(pieceState, finalFrom, to) || finalFrom.equals(to)) {
                    final Piece targetPiece = pieceState.piece(to);
                    if (targetPiece != null && targetPiece.team() == participant.team()) {
                        final LivingEntity targetEntity = targetPiece.isPlayer() ? Bukkit.getPlayer(targetPiece.id()) : (Bukkit.getEntity(targetPiece.id()) instanceof LivingEntity e ? e : null);
                        boolean interactable = false;
                        for (final PieceAbility ability : myPiece.ability().abilities()) {
                            if (ability.canInteract(player, myPiece, targetPiece, targetEntity)) {
                                interactable = true;
                                break;
                            }
                        }
                        if (interactable) {
                            validMoves.put(to, dev.tecte.chesswar.board.GuideType.INTERACT);
                        }
                    }
                }
            }
        }

        boardVisualManager.showGuide(player, validMoves);
    }

    public void setStartButtonBindingMode(final Player admin) {
        context.bindingAdmin(admin.getUniqueId());
        admin.sendMessage(GameAnnouncer.MSG_START_BIND_PROMPT);
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
        admin.sendMessage(GameAnnouncer.MSG_START_BIND_SUCCESS);
    }

    public void removeStartButton(final Player admin) {
        context.startButtonLocation(null);
        updateStartButtonHologram();
        saveConfig();
        admin.sendMessage(GameAnnouncer.MSG_START_REMOVE_SUCCESS);
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
            display.text(GameAnnouncer.MSG_START_BUTTON_HOLOGRAM);
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

        final UUID prevId = currentTurnPlayer();
        if (prevId != null) {
            final Player player = Bukkit.getPlayer(prevId);
            if (player != null) {
                combatManager.clearCommanderVisuals(player);
            }
        }

        context.advanceTurnIndex();

        final UUID nextId = currentTurnPlayer();
        if (nextId != null) {
            final Player player = Bukkit.getPlayer(nextId);

            if (player != null) {
                onTurnStart(player);
            }
        }

        scoreboardManager.updateAll();
    }

    private void finalizePieceSelection() {
        for (final Team team : Team.values()) {
            final List<Participant> teamMembers = new ArrayList<>();
            for (final Participant p : context.participantsValues()) {
                if (p.team() == team) {
                    teamMembers.add(p);
                }
            }

            if (teamMembers.isEmpty()) {
                continue;
            }

            boolean hasKing = false;
            for (final Participant p : teamMembers) {
                final Piece piece = p.getPiece(pieceState);
                if (piece != null && piece.type() == PieceType.KING) {
                    hasKing = true;
                    break;
                }
            }

            if (context.kingRequired() && !hasKing) {
                final List<Participant> unselected = new ArrayList<>();
                for (final Participant p : teamMembers) {
                    if (p.getPiece(pieceState) == null) {
                        unselected.add(p);
                    }
                }

                final Participant target = unselected.isEmpty()
                        ? teamMembers.get((int) (Math.random() * teamMembers.size()))
                        : unselected.get((int) (Math.random() * unselected.size()));

                forceAssignPiece(target, PieceType.KING);
            }
        }

        for (final Participant p : context.participantsValues()) {
            if (p.getPiece(pieceState) == null) {
                final PieceType randomType = PieceType.values()[(int) (Math.random() * (PieceType.values().length - 1)) + 1];
                forceAssignPiece(p, randomType);
            }
        }
    }

    private void forceAssignPiece(final Participant participant, final PieceType type) {
        final Player player = Bukkit.getPlayer(participant.playerId());
        if (player == null) {
            return;
        }

        Coordinate targetCoord = null;
        for (final Map.Entry<Coordinate, PieceType> entry : PieceLayout.getFullInitialLayout().entrySet()) {
            if (entry.getValue() == type && PieceLayout.getTeamAt(entry.getKey()) == participant.team()) {
                targetCoord = entry.getKey();
                break;
            }
        }

        if (targetCoord == null) {
            targetCoord = Coordinate.of(3, teamPieceRow(participant.team()));
        }

        applyPieceAssignment(participant, player, targetCoord, type);
    }

    private int teamPieceRow(final Team team) {
        return team == Team.WHITE ? 0 : 7;
    }

    public void checkVictoryConditions() {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        final Set<Team> aliveTeams = new HashSet<>();
        for (final Team team : Team.values()) {
            if (hasKing(team)) {
                aliveTeams.add(team);
            }
        }

        if (aliveTeams.size() == 1) {
            win(aliveTeams.iterator().next());
        } else if (aliveTeams.isEmpty()) {
            advancePhase();
        }
    }

    public void processPieceDeath(final Entity entity) {
        if (entity instanceof Player player) {
            if (isParticipant(player)) {
                final Coordinate coord = pieceManager.findCoordinate(player);
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
        final Piece[][] pieces = pieceState.boardPieces();

        for (int x = 0; x < Coordinate.BOARD_SIZE; x++) {
            for (int y = 0; y < Coordinate.BOARD_SIZE; y++) {
                final Piece p = pieces[x][y];

                if (p != null && p.team() == team && p.type() == PieceType.KING) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isCoordinateOccupiedByTeammate(final UUID playerId, final Team team, final Coordinate coord) {
        final Piece piece = pieceState.piece(coord);
        return piece != null && piece.isPlayer() && piece.team() == team && !piece.id().equals(playerId);
    }

    private void applyPieceAssignment(final Participant participant, final Player player, final Coordinate coord, final PieceType type) {
        pieceManager.registerPlayerPiece(player, participant.team(), type, coord);
        combatManager.applyStats(player, type, participant.team());
        PieceItemUtils.replacePlayerPieceItem(player, type);
        scoreboardManager.updateAll();
    }
}
*/
