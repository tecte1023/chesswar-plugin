package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.PlayerInventoryAdapter;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BattleState implements GameState {
    private final ChessWar plugin;

    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);

        plugin.pieceManager().clearSpawnedEntities(true);

        plugin.boardManager().clearBarracksChests();

        final PlayerInventoryAdapter inventoryAdapter = new PlayerInventoryAdapter(plugin, BoardManager.TURN_ORDER_KEY);
        final Map<UUID, Integer> playerOrders = new HashMap<>();

        for (final Participant participant : gameManager.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());

            inventoryAdapter.extractTurnOrder(player).ifPresent(order -> playerOrders.put(participant.playerId(), order));
        }

        gameManager.calculateTurnOrder(playerOrders);

        gameManager.participants().keySet().forEach(id -> {
            final Player p = Bukkit.getPlayer(id);

            if (p != null) {
                inventoryAdapter.clearOrderItems(p);
            }
        });

        gameManager.participants().values().forEach(p -> {
            final Player onlinePlayer = Bukkit.getPlayer(p.playerId());

            if (onlinePlayer != null && p.initialCoordinate() != null) {
                plugin.boardManager().deployToBattlefield(p.team(), p.initialCoordinate(), onlinePlayer);
            }
        });

        gameManager.currentTurnPlayer().ifPresent(uuid -> {
            final Player firstPlayer = Bukkit.getPlayer(uuid);

            if (firstPlayer != null) {
                Bukkit.getPluginManager().callEvent(new TurnStartedEvent(firstPlayer));
            }
        });
    }

    @Override
    public GameState nextState() {
        return new EndedState(plugin);
    }

    @Override
    public GamePhase phase() {
        return GamePhase.BATTLE;
    }

    @Override
    public void onTurnStart(final GameManager gameManager, final Player player) {
        final Optional<Participant> participant = gameManager.findParticipant(player.getUniqueId());

        if (participant.isEmpty()) {
            return;
        }

        final Team team = participant.get().team();
        final int teamTime = plugin.timerManager().teamTime(team);

        plugin.timerManager().startTimer(Math.max(teamTime, 30));
    }

    @Override
    public void onTimerTick(final GameManager gameManager, final dev.tecte.chesswar.game.manager.TimerManager timerManager) {
        final Optional<UUID> currentUuid = gameManager.currentTurnPlayer();

        if (currentUuid.isEmpty()) {
            return;
        }

        final Participant participant = gameManager.findParticipant(currentUuid.get()).orElse(null);

        if (participant == null) {
            return;
        }

        final Team team = participant.team();

        if (timerManager.teamTime(team) > 30) {
            timerManager.teamTime(team, timerManager.remainingSeconds());
        }
    }

    @Override
    public void onTimerExpire(final GameManager gameManager) {
        gameManager.nextTurn();
    }

    @Override
    public String displayName() {
        return "전투";
    }

    @Override
    public void nextTurn(final GameManager gameManager) {
        gameManager.performNextTurnLogic();
    }
}
