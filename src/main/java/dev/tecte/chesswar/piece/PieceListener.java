package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.Messages;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public final class PieceListener implements Listener {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!board.isSetup()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.getWorld().getUID().equals(board.worldId())) {
            return;
        }

        Entity piece = pieceLifecycle.pieceOf(player.getUniqueId());
        if (piece == null) {
            return;
        }

        GridPoint targetGridPos = board.toGridPos(player.getLocation().toVector());
        if (!board.isInBounds(targetGridPos)) {
            Messages.reply(player, Component.text("체스판 밖으로 이동할 수 없습니다.", NamedTextColor.RED));
            return;
        }

        MoveResult result = pieceLifecycle.move(piece, targetGridPos);
        Component message = switch (result) {
            case SUCCESS -> Component.text(
                    "(" + targetGridPos.x() + ", " + targetGridPos.y() + ") 칸으로 이동했습니다.",
                    NamedTextColor.GREEN
            );
            case ALREADY_OCCUPIED -> Component.text("해당 칸에 이미 다른 기물이 있습니다.", NamedTextColor.RED);
            case INVALID_PATTERN -> Component.text("해당 기물의 이동 범위가 아닙니다.", NamedTextColor.RED);
            case PATH_BLOCKED -> Component.text("이동 경로가 가로막혀 있습니다.", NamedTextColor.RED);
        };
        Messages.reply(player, message);
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        if (!board.isSetup()) {
            return;
        }

        org.bukkit.entity.Entity victim = event.getEntity();
        if (victim.isDead() || !victim.getWorld().getUID().equals(board.worldId())) {
            return;
        }

        Entity targetPiece = resolvePiece(victim);
        if (targetPiece == null) {
            return;
        }

        if (isInvalidAttack(event, victim)) {
            event.setCancelled(true);
            return;
        }

        if (!(event instanceof EntityDamageByEntityEvent byEntityEvent)
                || !(byEntityEvent.getDamager() instanceof Player attackerPlayer)) {
            event.setCancelled(true);
            return;
        }

        Entity attackerPiece = pieceLifecycle.pieceOf(attackerPlayer.getUniqueId());
        if (attackerPiece == null) {
            event.setCancelled(true);
            return;
        }

        AttackResult result = pieceLifecycle.attack(targetPiece, attackerPiece);
        if (result instanceof AttackResult.Failure failure) {
            event.setCancelled(true);
            Component message = switch (failure) {
                case FRIENDLY_FIRE -> Component.text("아군 기물은 공격할 수 없습니다.", NamedTextColor.RED);
                case INVALID_PATTERN -> Component.text("해당 기물의 공격 범위가 아닙니다.", NamedTextColor.RED);
                case PATH_BLOCKED -> Component.text("공격 경로가 가로막혀 있습니다.", NamedTextColor.RED);
            };
            Messages.reply(attackerPlayer, message);
            return;
        }

        int damage = ((AttackResult.Success) result).damage();
        var targetCombat = targetPiece.component(CombatComponent.class);
        if (targetCombat.isDead()) {
            if (victim instanceof Player) {
                event.setDamage(0);
            } else if (victim instanceof LivingEntity livingVictim) {
                event.setDamage(Math.max(damage, livingVictim.getHealth()));
            }

            Messages.reply(attackerPlayer, Component.text("적 기물을 처치하고 칸을 점령했습니다.", NamedTextColor.GREEN));
            return;
        }

        event.setDamage(damage);
        String message = "적 기물을 타격했습니다. (남은 체력: " + targetCombat.hp() + ")";
        Messages.reply(attackerPlayer, Component.text(message, NamedTextColor.GREEN));
    }

    private Entity resolvePiece(final org.bukkit.entity.Entity entity) {
        if (entity.isDead()) {
            return null;
        }

        if (entity instanceof Player player) {
            return pieceLifecycle.pieceOf(player.getUniqueId());
        }

        GridPoint gridPos = board.toGridPos(entity.getLocation().toVector());
        return pieceLifecycle.pieceAt(gridPos);
    }

    private boolean isInvalidAttack(final EntityDamageEvent event, final org.bukkit.entity.Entity victim) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return true;
        }

        return victim instanceof LivingEntity livingVictim
                && livingVictim.getNoDamageTicks() > livingVictim.getMaximumNoDamageTicks() / 2.0;
    }
}
