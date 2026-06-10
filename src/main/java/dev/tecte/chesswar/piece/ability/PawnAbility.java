package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.event.PiecePromotionEvent;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PawnAbility implements PieceAbility {
    @Override
    public boolean onMove(final Player player, final Piece piece, final Coordinate from, final Coordinate to) {
        final int promotionRank = (piece.team() == Team.WHITE) ? ChessFormation.BLACK_BACK_RANK : ChessFormation.WHITE_BACK_RANK;

        if (to.y() == promotionRank) {
            Bukkit.getPluginManager().callEvent(new PiecePromotionEvent(player, piece, to));
        }

        return true;
    }
}
