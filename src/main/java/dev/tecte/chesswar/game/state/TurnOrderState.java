package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class TurnOrderState implements GameState {
    private static final Component ERROR_ALREADY_READY = Component.text(
            "이미 준비 완료 상태입니다.",
            NamedTextColor.YELLOW
    );
    private static final Component ERROR_NOT_TEAM_CHEST = Component.text(
            "자신의 팀 막사에 있는 상자에서만 준비를 완료할 수 있습니다!",
            NamedTextColor.RED
    );

    private final ChessWar plugin;

    @Override
    public GamePhase phase() {
        return GamePhase.TURN_ORDER;
    }

    @Override
    public String displayName() {
        return phase().displayName();
    }

    @Override
    public GameState nextState() {
        return new BattleState(plugin);
    }

    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        gameManager.prepareTurnOrderContext();
    }

    @Override
    public void handleReadyUp(final GameManager gameManager, final Player player, final Location location) {
        if (gameManager.isReady(player.getUniqueId())) {
            player.sendMessage(ERROR_ALREADY_READY);
            return;
        }

        if (location != null) {
            final boolean isMyChest = gameManager.findParticipant(player.getUniqueId())
                    .map(participant -> plugin.boardManager().isTeamChest(location, participant.team()))
                    .orElse(false);

            if (!isMyChest) {
                player.sendMessage(ERROR_NOT_TEAM_CHEST);
                return;
            }
        }

        gameManager.processReadyUp(player);
    }

    @Override
    public void onTimerTick(final GameManager gameManager, final TimerManager timerManager) {
        if (gameManager.areAllParticipantsReady()) {
            timerManager.accelerateTo(gameManager.timerSettings().readyAccelerateTime());
        }
    }
}
