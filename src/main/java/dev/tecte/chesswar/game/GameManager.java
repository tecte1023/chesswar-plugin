package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.event.ChessTurnStartedEvent;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
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
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

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
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final List<UUID> turnOrder = new ArrayList<>();
    private final Set<UUID> spawnedEntities = new HashSet<>();
    private final Set<UUID> readyPlayers = new HashSet<>();
    private final Set<Location> barracksChests = new HashSet<>();

    @Setter
    private GamePhase phase = GamePhase.WAITING;
    private int currentTurnIndex = -1;

    public void addSpawnedEntity(UUID entityId) {
        spawnedEntities.add(entityId);
    }

    public void addBarracksChest(Location location) {
        barracksChests.add(location);
    }

    public boolean isBarracksChest(Location location) {
        return barracksChests.contains(location);
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

    public void join(Player player, Team team) {
        UUID playerId = player.getUniqueId();

        participants.put(playerId, Participant.of(playerId, team));
    }

    public void selectPiece(Player player, PieceType pieceType) {
        UUID playerId = player.getUniqueId();
        Participant participant = participants.get(playerId);

        if (participant == null) {
            return;
        }

        participants.put(playerId, Participant.of(playerId, participant.team(), pieceType));
        applyStats(player, pieceType);

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (PieceItemUtils.isPieceItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }

        player.getInventory().addItem(PieceItemUtils.createPieceItem(pieceType));
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

    public void win(Team winner) {
        phase = GamePhase.ENDED;

        Component winMessage = Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(winner.displayName(), winner.textColor(), TextDecoration.BOLD))
                .append(Component.text("가 승리했습니다!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .build();

        Bukkit.broadcast(winMessage);
    }

    public void reset() {
        phase = GamePhase.WAITING;
        boardPieces.clear();
        turnOrder.clear();
        currentTurnIndex = -1;
        readyPlayers.clear();
        clearSpawnedEntities();
        clearBarracksChests();

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
