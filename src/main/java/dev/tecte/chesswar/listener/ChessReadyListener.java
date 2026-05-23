package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.Participant;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

@RequiredArgsConstructor
public class ChessReadyListener implements Listener {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;

    @EventHandler
    public void onReadyClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ItemStack hotbar = null;
        Player player = (Player) event.getWhoClicked();

        if (event.getHotbarButton() != -1) {
            hotbar = player.getInventory().getItem(event.getHotbarButton());
        }

        if (isReadyButton(item) || isReadyButton(cursor) || isReadyButton(hotbar)) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, player::updateInventory);

            if (isReadyButton(item)) {
                handleReadyUp(player);
            }
        }
    }

    private boolean isReadyButton(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        NamespacedKey readyKey = new NamespacedKey(plugin, "ready_button");

        return item.getItemMeta().getPersistentDataContainer().has(readyKey, PersistentDataType.BYTE);
    }

    private void handleReadyUp(Player player) {
        if (gameManager.isReady(player.getUniqueId())) {
            player.sendMessage(Component.text("이미 준비 완료 상태입니다!", NamedTextColor.YELLOW));
            return;
        }

        gameManager.toggleReady(player.getUniqueId(), true);
        player.sendMessage(Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN));

        Optional<Participant> participant = gameManager.findParticipant(player.getUniqueId());

        participant.ifPresent(p -> {
            Team team = p.team();
            Component msg = Component.text(player.getName() + "님이 준비되었습니다! ", NamedTextColor.GRAY)
                    .append(Component.text("(" + countReady(team) + "/" + countTeam(team) + ")", NamedTextColor.AQUA));

            for (Participant other : gameManager.participants().values()) {
                if (other.team() == team) {
                    Player online = player.getServer().getPlayer(other.playerId());

                    if (online != null) {
                        online.sendMessage(msg);
                    }
                }
            }
        });

        if (gameManager.areAllParticipantsReady()) {
            timerManager.accelerateTo(10);
            Bukkit.broadcast(Component.text(
                    " 모든 플레이어가 준비를 마쳤습니다! 10초 후 전투가 시작됩니다.",
                    NamedTextColor.GREEN,
                    TextDecoration.BOLD
            ));
        }
    }

    private int countReady(Team team) {
        return (int) gameManager.participants().values().stream()
                .filter(p -> p.team() == team && gameManager.isReady(p.playerId()))
                .count();
    }

    private int countTeam(Team team) {
        return (int) gameManager.participants().values().stream()
                .filter(p -> p.team() == team)
                .count();
    }

    private void startBattle(Player sender) {
        gameManager.calculateTurnOrder(plugin);
        sender.performCommand("cw start");
    }
}
