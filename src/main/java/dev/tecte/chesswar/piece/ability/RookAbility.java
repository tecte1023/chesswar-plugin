package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.piece.Piece;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class RookAbility implements PieceAbility {
    @Override
    public void onTurnStart(final Player player, final Piece piece, final Participant participant) {
        final AttributeInstance maxAbsorption = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (maxAbsorption != null) {
            maxAbsorption.setBaseValue(40.0);
        }

        player.setAbsorptionAmount(40.0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
    }
}
