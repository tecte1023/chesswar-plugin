package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class GameManager {
    @Setter
    private GamePhase phase = GamePhase.WAITING;

    private final Map<UUID, Participant> participants = new HashMap<>();

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
