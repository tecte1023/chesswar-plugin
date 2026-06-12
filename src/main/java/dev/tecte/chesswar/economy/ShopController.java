package dev.tecte.chesswar.economy;

import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.CombatManager;
import dev.tecte.chesswar.piece.ConsumableItemUtils;
import dev.tecte.chesswar.piece.PieceType;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * [입력 컨트롤러] TriumphGui를 활용한 상점 및 강화 UI 제어기.
 */
@RequiredArgsConstructor
public class ShopController {
    private final GameContext gameContext;
    private final EconomyManager economyManager;
    private final CombatManager combatManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * 플레이어에게 메인 상점 UI를 엽니다.
     */
    private static final double UPGRADE_HEALTH_INC = 20.0;
    private static final double UPGRADE_DAMAGE_INC = 5.0;

    public void openMainShop(final Player player) {
        final Participant participant = gameContext.participants().get(player.getUniqueId());
        if (participant == null) {
            return;
        }

        final Gui gui = Gui.gui()
                .title(miniMessage.deserialize("<gold><bold>ChessWar Shop</bold></gold>"))
                .rows(3)
                .disableAllInteractions()
                .create();

        // 1. 기물 강화 (킹 전용)
        if (participant.selectedType() == PieceType.KING) {
            gui.setItem(10, ItemBuilder.from(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                    .name(miniMessage.deserialize("<aqua><bold>[ 기물 강화 ]</bold></aqua>"))
                    .lore(
                            miniMessage.deserialize("<gray>킹의 권한으로 특정 기물군 전체를 강화합니다.</gray>"),
                            Component.empty(),
                            miniMessage.deserialize("<yellow>클릭하여 강화 메뉴 열기</yellow>")
                    )
                    .asGuiItem(event -> openUpgradeShop(player)));
        } else {
            gui.setItem(10, ItemBuilder.from(Material.BARRIER)
                    .name(miniMessage.deserialize("<red>[ 기물 강화 ]</red>"))
                    .lore(miniMessage.deserialize("<gray>킹 클래스만 이용 가능합니다.</gray>"))
                    .asGuiItem());
        }

        // 2. 개인 강화 (누구나 이용 가능)
        gui.setItem(12, ItemBuilder.from(Material.EXPERIENCE_BOTTLE)
                .name(miniMessage.deserialize("<light_purple><bold>[ 개인 강화 ]</bold></light_purple>"))
                .lore(
                        miniMessage.deserialize("<gray>현재 조종 중인 기물의 성능을 영구적으로 강화합니다.</gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 강화 메뉴 열기</yellow>")
                )
                .asGuiItem(event -> openPersonalUpgradeShop(player)));

        // 3. 룩 수리 (누구나 이용 가능)
        gui.setItem(14, ItemBuilder.from(Material.GOLD_INGOT)
                .name(miniMessage.deserialize("<gold><bold>[ 룩 장갑 수리 ]</bold></gold>"))
                .lore(
                        miniMessage.deserialize("<gray>아군 룩(Rook) 기물의 황금 체력을 100% 리필합니다.</gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<white>비용: </white><gold>150G</gold>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 수리</yellow>")
                )
                .asGuiItem(event -> {
                    if (economyManager.spendGold(player.getUniqueId(), 150)) {
                        combatManager.repairRooks(participant.team());
                        player.sendMessage(miniMessage.deserialize("<green>아군 룩의 황금 체력이 리필되었습니다!</green>"));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                        player.closeInventory();
                    }
                }));

        // 4. 소모성 아이템 구매
        gui.setItem(16, ItemBuilder.from(Material.POTION)
                .name(miniMessage.deserialize("<green><bold>[ 아이템 구매 ]</bold></green>"))
                .lore(
                        miniMessage.deserialize("<gray>전투에 도움이 되는 특수 아이템을 구매합니다.</gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 상점 열기</yellow>")
                )
                .asGuiItem(event -> openConsumableShop(player)));

        // 배경 채우기
        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());

        gui.open(player);
    }

    private void openConsumableShop(final Player player) {
        final Gui gui = Gui.gui()
                .title(miniMessage.deserialize("<green><bold>Consumable Items</bold></green>"))
                .rows(3)
                .disableAllInteractions()
                .create();

        // 1. 도약 (300G)
        gui.setItem(13, ItemBuilder.from(Material.FEATHER)
                .name(miniMessage.deserialize("<aqua><bold>도약</bold></aqua>"))
                .lore(
                        miniMessage.deserialize("<gray>아군 기물 하나를 뛰어넘어 이동할 수 있습니다.</gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<white>비용: </white><gold>300G</gold>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 구매</yellow>")
                )
                .asGuiItem(event -> {
                    if (economyManager.spendGold(player.getUniqueId(), 300)) {
                        player.getInventory().addItem(ConsumableItemUtils.createLeapItem());
                        player.sendMessage(miniMessage.deserialize("<green>도약 아이템을 구매했습니다!</green>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                        player.closeInventory();
                    }
                }));

        // 뒤로 가기
        gui.setItem(22, ItemBuilder.from(Material.ARROW)
                .name(Component.text("뒤로 가기", NamedTextColor.YELLOW))
                .asGuiItem(event -> openMainShop(player)));

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.open(player);
    }

    private void openPersonalUpgradeShop(final Player player) {
        final Gui gui = Gui.gui()
                .title(miniMessage.deserialize("<light_purple><bold>Personal Upgrades</bold></light_purple>"))
                .rows(3)
                .disableAllInteractions()
                .create();

        // 체력 강화 (+10 HP, 200G)
        gui.setItem(12, ItemBuilder.from(Material.APPLE)
                .name(miniMessage.deserialize("<red><bold>체력 강화</bold></red>"))
                .lore(
                        miniMessage.deserialize("<gray>현재 기물의 최대 체력을 상향합니다.</gray>"),
                        miniMessage.deserialize("<gray>효과: <red>+10 HP</red></gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<white>비용: </white><gold>200G</gold>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 구매</yellow>")
                )
                .asGuiItem(event -> {
                    if (economyManager.hasGold(player.getUniqueId(), 200)) {
                        if (combatManager.upgradeIndividualPiece(player, 10.0, 0.0)) {
                            economyManager.spendGold(player.getUniqueId(), 200);
                            player.sendMessage(miniMessage.deserialize("<green>최대 체력이 상승했습니다!</green>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                            player.closeInventory();
                        }
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>골드가 부족합니다!</red>"));
                    }
                }));

        // 공격력 강화 (+2 ATK, 250G)
        gui.setItem(14, ItemBuilder.from(Material.IRON_SWORD)
                .name(miniMessage.deserialize("<orange><bold>공격력 강화</bold></orange>"))
                .lore(
                        miniMessage.deserialize("<gray>현재 기물의 공격력을 상향합니다.</gray>"),
                        miniMessage.deserialize("<gray>효과: <orange>+2 ATK</orange></gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<white>비용: </white><gold>250G</gold>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 구매</yellow>")
                )
                .asGuiItem(event -> {
                    if (economyManager.hasGold(player.getUniqueId(), 250)) {
                        if (combatManager.upgradeIndividualPiece(player, 0.0, 2.0)) {
                            economyManager.spendGold(player.getUniqueId(), 250);
                            player.sendMessage(miniMessage.deserialize("<green>공격력이 상승했습니다!</green>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                            player.closeInventory();
                        }
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>골드가 부족합니다!</red>"));
                    }
                }));

        // 뒤로 가기
        gui.setItem(22, ItemBuilder.from(Material.ARROW)
                .name(Component.text("뒤로 가기", NamedTextColor.YELLOW))
                .asGuiItem(event -> openMainShop(player)));

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.open(player);
    }

    /**
     * 킹 전용 기물 강화 상점을 엽니다.
     */
    private void openUpgradeShop(final Player player) {
        final Gui gui = Gui.gui()
                .title(miniMessage.deserialize("<aqua><bold>Piece Upgrades</bold></aqua>"))
                .rows(3)
                .disableAllInteractions()
                .create();

        // 폰 강화 예시
        addUpgradeItem(gui, player, 10, PieceType.PAWN, Material.WHITE_WOOL, 300);
        addUpgradeItem(gui, player, 11, PieceType.KNIGHT, Material.IRON_HORSE_ARMOR, 500);
        addUpgradeItem(gui, player, 12, PieceType.BISHOP, Material.ENCHANTED_BOOK, 500);
        addUpgradeItem(gui, player, 14, PieceType.ROOK, Material.ANVIL, 600);
        addUpgradeItem(gui, player, 15, PieceType.QUEEN, Material.DIAMOND, 800);

        // 뒤로 가기
        gui.setItem(22, ItemBuilder.from(Material.ARROW)
                .name(Component.text("뒤로 가기", NamedTextColor.YELLOW))
                .asGuiItem(event -> openMainShop(player)));

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());
        gui.open(player);
    }

    private void addUpgradeItem(final Gui gui, final Player player, final int slot, final PieceType type, final Material icon, final int cost) {
        gui.setItem(slot, ItemBuilder.from(icon)
                .name(miniMessage.deserialize("<gold>" + type.displayName() + " 강화</gold>"))
                .lore(
                        miniMessage.deserialize("<gray>해당 기물군 전체의 능력치를 상향합니다.</gray>"),
                        miniMessage.deserialize("<gray>체력 +" + (int) UPGRADE_HEALTH_INC + " | 공격력 +" + (int) UPGRADE_DAMAGE_INC + "</gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<white>비용: </white><gold>" + cost + "G</gold>"),
                        Component.empty(),
                        miniMessage.deserialize("<yellow>클릭하여 구매</yellow>")
                )
                .asGuiItem(event -> {
                    final Participant participant = gameContext.participants().get(player.getUniqueId());
                    if (participant == null) {
                        return;
                    }

                    if (economyManager.spendGold(player.getUniqueId(), cost)) {
                        combatManager.upgradePieceClass(participant.team(), type, UPGRADE_HEALTH_INC, UPGRADE_DAMAGE_INC);

                        player.sendMessage(miniMessage.deserialize("<green>" + type.displayName() + " 클래스가 강화되었습니다!</green>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.closeInventory();
                    }
                }));
    }
}
