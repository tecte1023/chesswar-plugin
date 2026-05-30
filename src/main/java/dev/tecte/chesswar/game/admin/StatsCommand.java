package dev.tecte.chesswar.game.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.Statistics;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

@CommandAlias("chesswar|cw")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public class StatsCommand extends BaseCommand {
    private final GameManager gameManager;

    @Subcommand("record")
    public void onRecord(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null) return;

        meta.setTitle("전투 기록 일지");
        meta.setAuthor("ChessWar System");

        Component content = Component.text()
                .append(Component.text("=== 전투 기록 리포트 ===\n\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .build();

        for (Participant p : gameManager.participants().values()) {
            Statistics s = gameManager.stats(p.playerId());
            Player participantPlayer = Bukkit.getPlayer(p.playerId());
            String name = (participantPlayer != null) ? participantPlayer.getName() : "오프라인";

            content = content.append(Component.text()
                    .append(Component.text("-" + name + " (" + p.team().teamName() + ")\n", p.team().color()))
                    .append(Component.text("  가한 피해: " + (int) s.getDamageDealt() + "\n", NamedTextColor.DARK_GRAY))
                    .append(Component.text("  받은 피해: " + (int) s.getDamageTaken() + "\n", NamedTextColor.DARK_GRAY))
                    .append(Component.text("  킬/데스: " + s.getKills() + "/" + s.getDeaths() + "\n\n", NamedTextColor.DARK_GRAY))
                    .build());
        }

        meta.addPages(content);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        player.sendMessage(Component.text("전투 기록 일지를 지급했습니다.", NamedTextColor.GREEN));
    }
}
