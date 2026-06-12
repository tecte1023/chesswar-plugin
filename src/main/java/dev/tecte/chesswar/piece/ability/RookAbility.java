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

    @Override
    public void onTurnStart(final Player player, final Piece piece, final Participant participant) {
        final AttributeInstance maxAbsorption = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (maxAbsorption != null) {
            maxAbsorption.setBaseValue(40.0);
        }

        player.setAbsorptionAmount(40.0);
        announcer.announceRookRepair(player);
    }
}
