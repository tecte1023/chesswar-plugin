package dev.tecte.chesswar.game;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
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

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
    }

    public boolean isParticipant(Player player) {
        return participants.containsKey(player.getUniqueId());
    }
}
