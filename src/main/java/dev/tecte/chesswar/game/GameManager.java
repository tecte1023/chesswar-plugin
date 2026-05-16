package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class GameManager {
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final List<UUID> turnOrder = new ArrayList<>();

    @Setter
    private GamePhase phase = GamePhase.WAITING;
    private int currentTurnIndex = -1;

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
    }

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
    }

    public boolean isParticipant(Player player) {
        return participants.containsKey(player.getUniqueId());
    }

    public Optional<Participant> findParticipant(Player player) {
        return Optional.ofNullable(participants.get(player.getUniqueId()));
    }

    public void prepareTurnOrder() {
        turnOrder.clear();

        List<UUID> whiteTeam = new ArrayList<>();
        List<UUID> blackTeam = new ArrayList<>();

        for (Participant p : participants.values()) {
            if (p.team() == Team.WHITE) {
                whiteTeam.add(p.playerId());
            } else {
                blackTeam.add(p.playerId());
            }
        }

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
    }

    private void applyStats(Player player, PieceType type) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(type.baseHp());
            player.setHealth(type.baseHp());
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(type.baseDamage());
        }
    }
}
