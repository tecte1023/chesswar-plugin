package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.piece.Piece;
import lombok.RequiredArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class RookAbility implements PieceAbility {
    private final GameAnnouncer announcer;
    private final double efficiency;

    @Override
    public void onTurnStart(final Player player, final Piece piece, final Participant participant) {
        final double absorptionAmount = Math.round(40.0 * efficiency);
        final org.bukkit.attribute.AttributeInstance maxAbsorption = player.getAttribute(org.bukkit.attribute.Attribute.MAX_ABSORPTION);

        if (maxAbsorption != null) {
            maxAbsorption.setBaseValue(absorptionAmount);
        }

        player.setAbsorptionAmount(absorptionAmount);
        announcer.announceRookRepair(player);
    }
}
