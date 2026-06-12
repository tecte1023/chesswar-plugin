package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.Statistics;
import dev.tecte.chesswar.piece.PieceType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 게임 내 모든 프레젠테이션(텍스트 UI, 액션바, 홀로그램, 타이틀) 로직을 전담하는 출력 유틸리티 클래스입니다.
 */
public class GameAnnouncer {

    public static final Component MSG_COUNTDOWN_SUBTITLE = Component.text("잠시 후 기물 선택이 시작됩니다.", NamedTextColor.YELLOW);
    public static final Component MSG_READY_COMPLETE = Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN);
    public static final Component MSG_RESET_COMPLETE = Component.text("게임이 초기화되었습니다.", NamedTextColor.GREEN);
    public static final Component MSG_ELIMINATED = Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED);
    private static final Component MSG_HOVER_TAKEN = Component.text("이미 참전 중인 기물입니다.", NamedTextColor.RED);
    private static final Component MSG_HOVER_SELECT = Component.text("클릭하여 해당 기물로 참전합니다.", NamedTextColor.GREEN);

    public static final Component MSG_BOARD_SETUP_COMPLETE = Component.text("체스판이 설정되었습니다! (3x3 배율)", NamedTextColor.GREEN);
    public static final Component MSG_RECORD_BOOK_GIVEN = Component.text("전투 기록 일지를 지급했습니다.", NamedTextColor.GREEN);
    public static final Component MSG_TURN_ORDER_DECISION = Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("팀 전술 회의 및 순서 결정 단계입니다. 상자를 열어 순서를 정하세요!", NamedTextColor.AQUA, TextDecoration.BOLD));
    public static final Component MSG_WAITING_PREPARATION = Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("모든 플레이어가 기물을 선택했습니다! 10초 후 준비 단계로 넘어갑니다.", NamedTextColor.AQUA, TextDecoration.BOLD));

    private static final Component UI_DECORATION_LINE = Component.text("━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH);

    private static final Component UI_BTN_BASE = Component.text("[ ⚔ ")
            .append(Component.text("참전하기").decorate(TextDecoration.BOLD))
            .append(Component.text(" ]"));
    private static final Component UI_BTN_TAKEN = UI_BTN_BASE.color(NamedTextColor.GRAY);
    private static final Component UI_BTN_SELECT = UI_BTN_BASE.color(NamedTextColor.GREEN);

    private static final Component UI_STATS_TITLE = Component.text(" [ 전투 결과 통계 ] ", NamedTextColor.GOLD, TextDecoration.BOLD);

    public static final String RECORD_BOOK_TITLE = "전투 기록 일지";
    public static final String RECORD_BOOK_AUTHOR = "ChessWar System";
    public static final Component RECORD_BOOK_HEADER = Component.text("=== 전투 기록 리포트 ===\n\n", NamedTextColor.GOLD, TextDecoration.BOLD);

    public static final Component MSG_LEAVE_TEAM = Component.text(" 팀에서 퇴장했습니다.", NamedTextColor.YELLOW);
    private static final Component MSG_SELECTION_GUIDE = Component.text("기물을 우클릭하여 참전할 기물을 선택해주세요.", NamedTextColor.RED);
    private static final Component MSG_SELECTION_WAITING = Component.text("기물 선택 완료! 다른 플레이어를 기다리는 중...", NamedTextColor.GREEN);
    private static final Component MSG_SELECTION_COMPLETED = Component.text("기물 선택이 모두 완료되었습니다!", NamedTextColor.AQUA);
    public static final Component MSG_START_BIND_PROMPT = Component.text("설정할 버튼을 좌클릭(타격) 하세요.", NamedTextColor.YELLOW);
    public static final Component MSG_START_BIND_SUCCESS = Component.text("게임 시작 버튼이 성공적으로 등록되었습니다!", NamedTextColor.GREEN);
    public static final Component MSG_START_REMOVE_SUCCESS = Component.text("게임 시작 버튼이 제거되었습니다.", NamedTextColor.YELLOW);
    public static final Component MSG_START_BUTTON_HOLOGRAM = Component.text("게임 시작", NamedTextColor.GOLD, TextDecoration.BOLD);

    private static final Component MSG_TURN_ORDER_GUIDE = Component.text("상자에서 순서 아이템을 가져가 턴 순서를 정하세요!", NamedTextColor.YELLOW);
    private static final Component MSG_TURN_ORDER_APPLIED = Component.text("순서 확정: ", NamedTextColor.GREEN);

    private static final float SOUND_VOLUME_DEFAULT = 1.0f;
    private static final float SOUND_PITCH_DEFAULT = 1.0f;

    private final GameContext context;

    public GameAnnouncer(final GameContext context) {
        this.context = context;
    }

    public void sendSelectionConfirmation(final Player player, final PieceType type, final Coordinate coord, final boolean isTaken) {
        final Component titlePanel = Component.text()
                .append(UI_DECORATION_LINE)
                .appendSpace()
                .append(Component.text("[ " + type.symbol() + " " + type.displayName() + " ]", NamedTextColor.GOLD, TextDecoration.BOLD))
                .appendSpace()
                .append(UI_DECORATION_LINE)
                .build();

        final Component statPanel = Component.text()
                .append(Component.text("체력: ", NamedTextColor.GRAY))
                .append(Component.text("♥ " + (int) type.baseHealth(), NamedTextColor.DARK_GREEN))
                .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                .append(Component.text("공격력: ", NamedTextColor.GRAY))
                .append(Component.text("⚔ " + (int) type.baseDamage(), NamedTextColor.RED))
                .build();

        final Component rangePanel = Component.text()
                .append(Component.text("이동 및 공격 범위: ", NamedTextColor.GRAY))
                .append(Component.text(type.rangeDescription(), NamedTextColor.AQUA))
                .build();

        final Component buttonPanel = isTaken ?
                UI_BTN_TAKEN.hoverEvent(HoverEvent.showText(MSG_HOVER_TAKEN)) :
                UI_BTN_SELECT
                        .clickEvent(ClickEvent.runCommand("/cw select " + coord.x() + " " + coord.y()))
                        .hoverEvent(HoverEvent.showText(MSG_HOVER_SELECT));

        final Component message = Component.join(
                JoinConfiguration.newlines(),
                Component.empty(),
                titlePanel,
                Component.empty(),
                Component.text(type.description(), NamedTextColor.WHITE),
                Component.empty(),
                statPanel,
                rangePanel,
                Component.empty(),
                Component.text().append(Component.text("               ")).append(buttonPanel).build(),
                Component.empty()
        );

        player.sendMessage(message);
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
    }

    public void broadcastSelectionCountdown(final int seconds) {
        final Title title = Title.title(
                Component.text(seconds, NamedTextColor.RED, TextDecoration.BOLD),
                MSG_COUNTDOWN_SUBTITLE,
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofMillis(250))
        );

        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                player.showTitle(title);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
            }
        }
    }

    public void sendSelectionActionBarGuidance(final boolean allSelected) {
        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player != null) {
                final Component guide = allSelected ? MSG_SELECTION_COMPLETED :
                        (participant.hasPiece() ? MSG_SELECTION_WAITING : MSG_SELECTION_GUIDE);
                player.sendActionBar(guide);
            }
        }
    }

    public void sendTurnOrderActionBarGuidance(final PlayerInventoryAdapter inventoryAdapter) {
        for (final Participant participant : context.participants().values()) {
            final Player player = Bukkit.getPlayer(participant.playerId());
            if (player == null) continue;

            final Optional<Integer> order = inventoryAdapter.extractTurnOrder(player);
            if (order.isPresent()) {
                player.sendActionBar(MSG_TURN_ORDER_APPLIED.append(Component.text(order.get() + "번", NamedTextColor.WHITE, TextDecoration.BOLD)));
            } else {
                player.sendActionBar(MSG_TURN_ORDER_GUIDE);
            }
        }
    }

    public void displayStatisticsHologram() {
        final List<Component> lines = new ArrayList<>();
        lines.add(UI_STATS_TITLE);
        lines.add(Component.empty());

        context.participants().values().forEach(p -> {
            final Statistics s = p.statistics();
            final Player player = Bukkit.getPlayer(p.playerId());
            final String name = (player != null) ? player.getName() : "오프라인";

            lines.add(Component.text()
                    .append(Component.text(name, p.team().color()))
                    .append(Component.text(" | ", NamedTextColor.GRAY))
                    .append(Component.text("⚔" + (int) s.getDamageDealt(), NamedTextColor.RED))
                    .append(Component.text(" 🛡" + (int) s.getDamageTaken(), NamedTextColor.BLUE))
                    .append(Component.text(" ➕" + (int) s.getHealingDone(), NamedTextColor.GREEN))
                    .append(Component.text(" ☠" + s.getKills() + "/" + s.getDeaths(), NamedTextColor.DARK_RED))
                    .build());
        });

        Bukkit.broadcast(Component.join(JoinConfiguration.newlines(), lines));
    }

    public void broadcast(final Component message) {
        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public void playCountdownTickSound() {
        for (final UUID playerId : context.participants().keySet()) {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
            }
        }
    }

    public void announceShopSuccess(final Player player, final Component message) {
        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    public void announceShopItemPurchase(final Player player, final Component message) {
        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }

    public void announceShopRepair(final Player player, final Component message) {
        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    public void announceShopFailure(final Player player, final Component message) {
        player.sendMessage(message);
    }

    public void announceLeapUsage(final Player player) {
        player.sendMessage(Component.text("도약 아이템을 사용하여 이번 턴에 아군을 뛰어넘을 수 있습니다!", NamedTextColor.AQUA));
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.2f);
        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 20, 0.3, 0.3, 0.3, 0.1);
    }

    public void announceAlreadyLeaping(final Player player) {
        player.sendMessage(Component.text("이미 도약 효과가 활성화되어 있습니다!", NamedTextColor.YELLOW));
    }

    public void announceHeal(final Player healer, final org.bukkit.entity.LivingEntity victim, final dev.tecte.chesswar.piece.Piece targetPiece) {
        victim.getWorld().spawnParticle(
                org.bukkit.Particle.HEART,
                victim.getLocation().add(0, 1, 0),
                10, 0.5, 0.5, 0.5, 0.1
        );
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        healer.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text("의 체력을 회복시켰습니다!", NamedTextColor.GREEN))
                .build());
    }

    public void announceKnightPreemptiveStrike(final Player attacker) {
        attacker.sendMessage(Component.text("비선제 공격! 추가 피해를 입혔습니다.", NamedTextColor.LIGHT_PURPLE));
    }

    public void announceKingCommanderSelect(final Player player, final dev.tecte.chesswar.piece.Piece targetPiece) {
        player.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text(" 기물에 지휘권을 발동했습니다.", NamedTextColor.AQUA))
                .build());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public void announceKingCommanderDeselect(final Player player, final dev.tecte.chesswar.piece.Piece targetPiece) {
        player.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text(" 기물 지휘를 취소했습니다.", NamedTextColor.YELLOW))
                .build());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f);
    }

    public void announceRookRepair(final org.bukkit.entity.LivingEntity entity) {
        entity.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, entity.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
    }

    public void announceCombatError(final Player player, final Component message) {
        player.sendMessage(message);
    }

    public void announceMoveSuccess(final Player player, final dev.tecte.chesswar.piece.Piece movingPiece, final Coordinate to, final boolean isCommander) {
        if (isCommander) {
            player.sendMessage(Component.text()
                    .append(Component.text(movingPiece.type().displayName(), NamedTextColor.GOLD))
                    .append(Component.text(" 기물을 " + to.x() + ", " + to.y() + " 좌표로 이동시켰습니다.", NamedTextColor.GREEN))
                    .build());
        } else {
            player.sendMessage(Component.text(to.x() + ", " + to.y() + " 좌표로 이동했습니다.", NamedTextColor.GREEN));
        }
    }

    public void announceTurnStart(final Player player, final PieceType pieceType) {
        player.sendMessage(Component.text(pieceType.displayName() + " 기물을 선택했습니다!", NamedTextColor.GOLD));
    }

    public void announcePieceSelection(final Player player, final Component message) {
        player.sendMessage(message);
    }

    public void announceAttackResult(final Player attacker, final org.bukkit.entity.LivingEntity victim, final dev.tecte.chesswar.piece.Piece victimPiece, final double damage) {
        final Component baseMessage = Component.text()
                .append(Component.text("[⚔] ", NamedTextColor.RED))
                .append(Component.text(attacker.getName(), attacker.getUniqueId().equals(context.currentTurnPlayerId()) ? NamedTextColor.GOLD : NamedTextColor.WHITE))
                .append(Component.text("님이 ", NamedTextColor.GRAY))
                .append(Component.text(victimPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text(" 기물을 공격했습니다!", NamedTextColor.GRAY))
                .build();

        final Component hoverDetail = Component.text()
                .append(Component.text("전투 세부 정보", NamedTextColor.GOLD, TextDecoration.BOLD))
                .appendNewline()
                .append(Component.text("----------------", NamedTextColor.DARK_GRAY))
                .appendNewline()
                .append(Component.text("가한 피해: ", NamedTextColor.GRAY))
                .append(Component.text((int) damage + " ATK", NamedTextColor.RED))
                .appendNewline()
                .append(Component.text("대상 잔여 체력: ", NamedTextColor.GRAY))
                .append(Component.text((int) victim.getHealth() + " / " + (int) victim.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue(), NamedTextColor.GREEN))
                .build();

        final Component finalMessage = baseMessage.hoverEvent(HoverEvent.showText(hoverDetail));

        broadcast(finalMessage);
    }

    public void announceAttack(final org.bukkit.entity.LivingEntity victim) {
        victim.getWorld().spawnParticle(
                org.bukkit.Particle.CRIT,
                victim.getLocation().add(0, 1, 0),
                10, 0.5, 0.5, 0.5, 0.1
        );
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, SOUND_VOLUME_DEFAULT, SOUND_PITCH_DEFAULT);
    }

    public void announceKill(final org.bukkit.entity.LivingEntity victim) {
        victim.getWorld().spawnParticle(
                org.bukkit.Particle.EXPLOSION,
                victim.getLocation().add(0, 1, 0),
                1, 0, 0, 0, 0
        );
    }

    public void announceAdminMessage(final Player admin, final Component message) {
        admin.sendMessage(message);
    }

    public void announceBoardOutline(final Player player, final double x, final double y, final double z, final org.bukkit.Particle.DustOptions options) {
        player.spawnParticle(
                org.bukkit.Particle.DUST,
                x, y, z,
                1,
                options
        );
    }

    public void announceGoldEarned(final Player player, final int amount, final String sourceDescription) {
        final Component message = Component.text()
                .append(Component.text("+ " + amount + " Gold", NamedTextColor.GOLD))
                .append(Component.text(" (" + sourceDescription + ")", NamedTextColor.GRAY))
                .build();
        player.sendActionBar(message);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public void announceInsufficientGold(final Player player) {
        player.sendMessage(Component.text("골드가 부족합니다!", NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    public void announceGoldSpent(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.2f);
    }

}
