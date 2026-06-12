package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class KnightAbility implements PieceAbility {
    private final PieceState pieceState;
    private final MoveValidator moveValidator = new MoveValidator();

    public KnightAbility(final PieceState pieceState) {
        this.pieceState = pieceState;
    }

    @Override
    public double onAttack(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate attackerCoord,
            final Coordinate victimCoord,
            final Piece attackingPiece,
            final Piece victimPiece,
            final double damage
    ) {
        if (!moveValidator.canMove(pieceState, victimCoord, attackerCoord, false)) {
            attacker.sendMessage(Component.text("비선제 공격! 추가 피해를 입혔습니다.", NamedTextColor.LIGHT_PURPLE));
            return damage + 15.0;
        }

        return damage;
    }
}
