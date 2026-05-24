package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.ChessBoard;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.event.ChessTurnStartedEvent;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
public class GameManager {
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<UUID, Statistics> statistics = new HashMap<>();
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final List<UUID> turnOrder = new ArrayList<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();
    private final Set<Location> barracksChests = new HashSet<>();
    private final Map<Location, Team> chestTeamOwnership = new HashMap<>();
    private final Set<UUID> readyPlayers = new HashSet<>();

    @Setter
    private GamePhase phase = GamePhase.WAITING;
    private int currentTurnIndex = -1;

    public void addSpawnedEntity(UUID entityId) {
        spawnedEntities.add(entityId);
    }

    public void addBarracksChest(Location location, Team team) {
        Location blockLoc = location.getBlock().getLocation();
        barracksChests.add(blockLoc);
        chestTeamOwnership.put(blockLoc, team);
    }

    public boolean isBarracksChest(Location location) {
        return barracksChests.contains(location.getBlock().getLocation());
    }

    public boolean isTeamChest(Location location, Team team) {
        return chestTeamOwnership.get(location.getBlock().getLocation()) == team;
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

    public void advancePhase(Plugin plugin, BoardManager boardManager, TimerManager timerManager) {
        GamePhase nextPhase = switch (phase) {
            case WAITING -> GamePhase.PIECE_SELECTION;
            case PIECE_SELECTION -> GamePhase.TURN_ORDER;
            case TURN_ORDER -> GamePhase.BATTLE;
            case BATTLE -> GamePhase.ENDED;
            case ENDED -> GamePhase.WAITING;
        };

        phase = nextPhase;
        Bukkit.broadcast(Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("게임 단계가 변경되었습니다: ", NamedTextColor.YELLOW))
                .append(Component.text(nextPhase.displayName(), NamedTextColor.WHITE, TextDecoration.BOLD))
                .build());

        switch (nextPhase) {
            case PIECE_SELECTION -> {
                setupBarracks(plugin, boardManager);
                teleportToBarracks(boardManager);
                timerManager.startTurnTimer(300);
            }
            case TURN_ORDER -> {
                enforceMandatoryKing();
                assignRandomRemainingPieces();
                setupTurnOrderChests(plugin, boardManager);
                timerManager.startTurnTimer(180);
            }
            case BATTLE -> {
                clearBarracksChests();
                calculateTurnOrder(plugin);
                deployToBattlefield(boardManager);
                timerManager.startTurnTimer(30);
                currentTurnPlayer().ifPresent(uuid -> {
                    Player firstPlayer = Bukkit.getPlayer(uuid);

                    if (firstPlayer != null) {
                        Bukkit.getPluginManager().callEvent(new ChessTurnStartedEvent(firstPlayer));
                    }
                });
            }
            case ENDED -> {
                timerManager.stopTimer();
                displayStatisticsHologram(boardManager);
            }
            case WAITING -> {
                timerManager.reset();
                reset();
            }
        }
    }

    private void setupBarracks(Plugin plugin, BoardManager boardManager) {
        if (!boardManager.hasBoard()) {
            return;
        }

        ChessBoard mainBoard = boardManager.currentBoard();
        int cellSize = mainBoard.cellSize();
        int offsetDistance = 5 + (8 * cellSize);
        Location whiteOrigin = mainBoard.origin().clone()
                .subtract(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard whiteBarracks = new ChessBoard(whiteOrigin, mainBoard.forward(), cellSize);
        Location blackOrigin = mainBoard.origin().clone()
                .add(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard blackBarracks = new ChessBoard(blackOrigin, mainBoard.forward(), cellSize);

        PieceType[] backRow = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        NamespacedKey typeKey = new NamespacedKey(plugin, "barracks_piece_type");
        NamespacedKey teamKey = new NamespacedKey(plugin, "barracks_piece_team");
        MobManager mobManager = MythicProvider.get().getMobManager();

        for (int x = 0; x < 8; x++) {
            PieceType type = backRow[x];

            spawnBarracksPiece(
                    whiteBarracks.toCenterLocation(Coordinate.of(x, 0)),
                    type,
                    Team.WHITE,
                    mainBoard.forward().getDirection(),
                    typeKey,
                    teamKey,
                    mobManager
            );
            spawnBarracksPiece(
                    blackBarracks.toCenterLocation(Coordinate.of(x, 7)),
                    type,
                    Team.BLACK,
                    mainBoard.forward().getDirection().multiply(-1),
                    typeKey,
                    teamKey,
                    mobManager
            );
        }
    }

    private void setupTurnOrderChests(Plugin plugin, BoardManager boardManager) {
        if (!boardManager.hasBoard()) {
            return;
        }

        ChessBoard mainBoard = boardManager.currentBoard();
        int cellSize = mainBoard.cellSize();
        int offsetDistance = 5 + (8 * cellSize);

        Location whiteBarracksLoc = mainBoard.origin().clone()
                .subtract(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard whiteBarracks = new ChessBoard(whiteBarracksLoc, mainBoard.forward(), cellSize);

        Location blackBarracksLoc = mainBoard.origin().clone()
                .add(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard blackBarracks = new ChessBoard(blackBarracksLoc, mainBoard.forward(), cellSize);

        setupReadyChest(plugin, whiteBarracks, Team.WHITE, 4);
        setupReadyChest(plugin, blackBarracks, Team.BLACK, 3);
    }

    private void setupReadyChest(Plugin plugin, ChessBoard barracks, Team team, int row) {
        Location origin = barracks.origin();
        BlockFace forward = barracks.forward();
        BlockFace right = barracks.right();
        int cellSize = barracks.cellSize();

        int dFileRight = 11;
        int eFileLeft = 12;
        int rankCenter = row * cellSize + 1;

        Location b1 = origin.clone()
                .add(right.getDirection().multiply(dFileRight))
                .add(forward.getDirection().multiply(rankCenter));
        Location b2 = origin.clone()
                .add(right.getDirection().multiply(eFileLeft))
                .add(forward.getDirection().multiply(rankCenter));

        BlockFace chestFacing;
        Chest.Type b1Type;
        Chest.Type b2Type;

        if (team == Team.WHITE) {
            chestFacing = forward.getOppositeFace();
            b1Type = Chest.Type.RIGHT;
            b2Type = Chest.Type.LEFT;
        } else {
            chestFacing = forward;
            b1Type = Chest.Type.LEFT;
            b2Type = Chest.Type.RIGHT;
        }

        b1.getBlock().setType(Material.CHEST, false);
        b2.getBlock().setType(Material.CHEST, false);

        BlockState state1 = b1.getBlock().getState();
        BlockState state2 = b2.getBlock().getState();

        Chest data1 = (Chest) state1.getBlockData();
        Chest data2 = (Chest) state2.getBlockData();

        data1.setFacing(chestFacing);
        data1.setType(b1Type);
        data2.setFacing(chestFacing);
        data2.setType(b2Type);

        state1.setBlockData(data1);
        state2.setBlockData(data2);

        state1.update(true, false);
        state2.update(true, false);

        addBarracksChest(b1, team);
        addBarracksChest(b2, team);

        boolean b1IsTop = b1.getBlockX() < b2.getBlockX() || (b1.getBlockX() == b2.getBlockX() && b1.getBlockZ() < b2.getBlockZ());
        Inventory chestInv = ((InventoryHolder) (b1IsTop ? b1 : b2).getBlock().getState()).getInventory();

        chestInv.clear();

        int participantCount = (int) participants.values().stream()
                .filter(p -> p.team() == team)
                .count();

        if (participantCount > 0) {
            NamespacedKey orderKey = new NamespacedKey(plugin, "turn_order");

            for (int i = 1; i <= participantCount; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(Component.text(i + "번 순서", NamedTextColor.GOLD, TextDecoration.BOLD));
                meta.getPersistentDataContainer().set(orderKey, PersistentDataType.INTEGER, i);
                item.setItemMeta(meta);
                chestInv.addItem(item);
            }
        }

        NamespacedKey readyKey = new NamespacedKey(plugin, "ready_button");
        ItemStack readyBtn = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta readyMeta = readyBtn.getItemMeta();

        readyMeta.displayName(Component.text("[ 준비 완료 ]", NamedTextColor.GREEN, TextDecoration.BOLD));
        readyMeta.getPersistentDataContainer().set(readyKey, PersistentDataType.BYTE, (byte) 1);
        readyBtn.setItemMeta(readyMeta);

        // 큰 상자(54칸)면 49번, 작은 상자(27칸)면 22번 슬롯 사용 (맨 하단 중앙)
        int readySlot = (chestInv.getSize() == 54) ? 49 : 22;
        chestInv.setItem(readySlot, readyBtn);
    }

    private void spawnBarracksPiece(
            Location location,
            PieceType type,
            Team team,
            Vector direction,
            NamespacedKey typeKey,
            NamespacedKey teamKey,
            MobManager mobManager
    ) {
        String mobId = toPascalCase(team.name()) + toPascalCase(type.name());

        mobManager.getMythicMob(mobId).ifPresent(mythicMob -> {
            location.setDirection(direction);
            ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(location), 1);

            if (activeMob == null) {
                return;
            }

            Entity entity = activeMob.getEntity().getBukkitEntity();
            entity.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
            entity.getPersistentDataContainer().set(teamKey, PersistentDataType.STRING, team.name());
            addSpawnedEntity(entity.getUniqueId());
        });
    }

    private String toPascalCase(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        return source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase();
    }

    private void enforceMandatoryKing() {
        Set<Team> teamsWithKings = new HashSet<>();
        participants.values().forEach(p -> {
            if (p.initialCoordinate() != null && getPieceTypeAt(p.initialCoordinate()) == PieceType.KING) {
                teamsWithKings.add(p.team());
            }
        });

        for (Team team : Team.values()) {
            if (teamsWithKings.contains(team)) continue;

            List<Participant> teamMembers = participants.values().stream()
                    .filter(p -> p.team() == team)
                    .toList();

            if (teamMembers.isEmpty()) continue;

            Participant luckyMember = teamMembers.get((int) (Math.random() * teamMembers.size()));
            Coordinate kingCoord = (team == Team.WHITE) ? Coordinate.of(4, 0) : Coordinate.of(4, 7);

            participants.put(luckyMember.playerId(), Participant.of(luckyMember.playerId(), team, kingCoord));

            Player player = Bukkit.getPlayer(luckyMember.playerId());
            if (player != null) {
                player.sendMessage(Component.text("팀에 킹이 없어 당신이 국왕으로 추대되었습니다!", NamedTextColor.GOLD, TextDecoration.BOLD));
                applyStats(player, PieceType.KING);

                // 기존 기물 무기 제거
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (PieceItemUtils.isPieceItem(item)) {
                        player.getInventory().setItem(i, null);
                    }
                }

                player.getInventory().addItem(PieceItemUtils.createPieceItem(PieceType.KING));
            }
        }
    }

    private void assignRandomRemainingPieces() {
        participants.values().forEach(p -> {
            if (p.initialCoordinate() == null) {
                // TODO: 남은 기물 중 랜덤 배정 로직 고도화
                Coordinate randomPawn = (p.team() == Team.WHITE) ? Coordinate.of(0, 1) : Coordinate.of(0, 6);
                participants.put(p.playerId(), Participant.of(p.playerId(), p.team(), randomPawn));
            }
        });
    }

    private void teleportToBarracks(BoardManager boardManager) {
        if (!boardManager.hasBoard()) {
            return;
        }

        ChessBoard mainBoard = boardManager.currentBoard();
        int cellSize = mainBoard.cellSize();
        int offsetDistance = 5 + (8 * cellSize);

        Location whiteBarracksLoc = mainBoard.origin().clone()
                .subtract(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard whiteBarracks = new ChessBoard(whiteBarracksLoc, mainBoard.forward(), cellSize);
        Location whiteTeleportPos = whiteBarracks.toCenterLocation(Coordinate.of(3, 6))
                .add(whiteBarracks.right().getDirection().multiply(cellSize / 2.0));
        whiteTeleportPos.setDirection(mainBoard.forward().getDirection().multiply(-1));

        Location blackBarracksLoc = mainBoard.origin().clone()
                .add(mainBoard.forward().getDirection().multiply(offsetDistance));
        ChessBoard blackBarracks = new ChessBoard(blackBarracksLoc, mainBoard.forward(), cellSize);
        Location blackTeleportPos = blackBarracks.toCenterLocation(Coordinate.of(3, 1))
                .add(blackBarracks.right().getDirection().multiply(cellSize / 2.0));
        blackTeleportPos.setDirection(mainBoard.forward().getDirection());

        participants.values().forEach(p -> {
            Player onlinePlayer = Bukkit.getPlayer(p.playerId());

            if (onlinePlayer != null) {
                onlinePlayer.teleport(p.team() == Team.WHITE ? whiteTeleportPos : blackTeleportPos);
            }
        });
    }

    private void deployToBattlefield(BoardManager boardManager) {
        if (!boardManager.hasBoard()) {
            return;
        }

        ChessBoard mainBoard = boardManager.currentBoard();

        participants.values().forEach(p -> {
            Player onlinePlayer = Bukkit.getPlayer(p.playerId());

            if (onlinePlayer == null || p.initialCoordinate() == null) {
                return;
            }

            Coordinate startCoordinate = p.initialCoordinate();
            Piece piece = Piece.of(p.playerId(), p.team(), getPieceTypeAt(startCoordinate));
            Location spawnLocation = mainBoard.toCenterLocation(startCoordinate).add(0, 1, 0);

            placePiece(startCoordinate, piece);
            onlinePlayer.teleport(spawnLocation);
        });
    }

    private PieceType getPieceTypeAt(Coordinate coordinate) {
        int y = coordinate.y();
        int x = coordinate.x();

        if (y == 1 || y == 6) {
            return PieceType.PAWN;
        }

        return switch (x) {
            case 0, 7 -> PieceType.ROOK;
            case 1, 6 -> PieceType.KNIGHT;
            case 2, 5 -> PieceType.BISHOP;
            case 3 -> PieceType.QUEEN;
            case 4 -> PieceType.KING;
            default -> PieceType.PAWN;
        };
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
        PieceType pieceType = getPieceTypeAt(coordinate);
        applyStats(participant, pieceType);

        for (int i = 0; i < participant.getInventory().getSize(); i++) {
            ItemStack item = participant.getInventory().getItem(i);

            if (PieceItemUtils.isPieceItem(item)) {
                participant.getInventory().setItem(i, null);
            }
        }

        participant.getInventory().addItem(PieceItemUtils.createPieceItem(pieceType));
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

    public void calculateTurnOrder(Plugin plugin) {
        turnOrder.clear();

        NamespacedKey orderKey = new NamespacedKey(plugin, "turn_order");
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

        // 순서 아이템 제거
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

        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        currentTurnPlayer().ifPresent(uuid -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Bukkit.getPluginManager().callEvent(new ChessTurnStartedEvent(player));
            }
        });
    }

    public void finishTurn() {
        nextTurn();
    }

    public void placePiece(Coordinate coordinate, Piece piece) {
        boardPieces.put(coordinate, piece);
    }

    public void removePiece(Coordinate coordinate) {
        boardPieces.remove(coordinate);
    }

    public Optional<Piece> findPieceAt(Coordinate coordinate) {
        return Optional.ofNullable(boardPieces.get(coordinate));
    }

    public void win(Plugin plugin, BoardManager boardManager, TimerManager timerManager, Team winner) {
        Component winMessage = Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(winner.displayName(), winner.textColor(), TextDecoration.BOLD))
                .append(Component.text("가 승리했습니다!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .build();

        Bukkit.broadcast(winMessage);
        advancePhase(plugin, boardManager, timerManager);
    }

    public Statistics getStats(UUID playerId) {
        return statistics.computeIfAbsent(playerId, id -> new Statistics());
    }

    private void displayStatisticsHologram(BoardManager boardManager) {
        if (!boardManager.hasBoard()) return;

        Location center = boardManager.currentBoard().toCenterLocation(Coordinate.of(3, 3))
                .add(boardManager.currentBoard().right().getDirection().multiply(boardManager.currentBoard().cellSize() / 2.0))
                .add(0, 2, 0);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(" [ 전투 결과 통계 ] ", NamedTextColor.GOLD, TextDecoration.BOLD));
        lines.add(Component.empty());

        participants.values().forEach(p -> {
            Statistics s = getStats(p.playerId());
            Player player = Bukkit.getPlayer(p.playerId());
            String name = (player != null) ? player.getName() : "오프라인";

            lines.add(Component.text()
                    .append(Component.text(name, p.team().textColor()))
                    .append(Component.text(" | ", NamedTextColor.GRAY))
                    .append(Component.text("⚔" + (int) s.getDamageDealt(), NamedTextColor.RED))
                    .append(Component.text(" 🛡" + (int) s.getDamageTaken(), NamedTextColor.BLUE))
                    .append(Component.text(" ➕" + (int) s.getHealingDone(), NamedTextColor.GREEN))
                    .append(Component.text(" ☠" + s.getKills() + "/" + s.getDeaths(), NamedTextColor.DARK_RED))
                    .build());
        });

        for (int i = 0; i < lines.size(); i++) {
            final int index = i;
            Location lineLoc = center.clone().subtract(0, i * 0.3, 0);
            lineLoc.getWorld().spawn(lineLoc, org.bukkit.entity.ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setCustomNameVisible(true);
                as.customName(lines.get(index));
                as.setMarker(true);
                addSpawnedEntity(as.getUniqueId());
            });
        }
    }

    public void reset() {
        Bukkit.getPluginManager().callEvent(new dev.tecte.chesswar.event.ChessGameResetEvent());
        phase = GamePhase.WAITING;
        boardPieces.clear();
        turnOrder.clear();
        currentTurnIndex = -1;
        readyPlayers.clear();
        statistics.clear();
        clearSpawnedEntities();
        clearBarracksChests();

        participants.keySet().forEach(id -> {
            Participant p = participants.get(id);
            participants.put(id, Participant.of(id, p.team(), null));
        });

        for (Participant participant : participants.values()) {
            Player player = Bukkit.getPlayer(participant.playerId());

            if (player != null) {
                resetStats(player);

                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);

                    if (PieceItemUtils.isPieceItem(item)) {
                        player.getInventory().setItem(i, null);
                    }
                }
            }
        }
    }

    private void clearBarracksChests() {
        for (Location loc : barracksChests) {
            BlockState state = loc.getBlock().getState();

            if (state instanceof InventoryHolder holder) {
                holder.getInventory().clear();
            }

            loc.getBlock().setType(Material.AIR);
        }

        barracksChests.clear();
        chestTeamOwnership.clear();
    }

    private void clearSpawnedEntities() {
        for (UUID entityId : spawnedEntities) {
            Entity entity = Bukkit.getEntity(entityId);

            if (entity != null) {
                entity.remove();
            }
        }

        spawnedEntities.clear();
    }

    private void applyStats(Player player, PieceType type) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(type.baseHealth());
            player.setHealth(type.baseHealth());
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(type.baseDamage());
        }
    }

    private void resetStats(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0);
            player.setHealth(20.0);
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(1.0);
        }
    }
}
