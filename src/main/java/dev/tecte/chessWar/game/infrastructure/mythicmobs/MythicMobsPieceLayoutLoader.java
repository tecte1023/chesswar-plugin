package dev.tecte.chessWar.game.infrastructure.mythicmobs;

import dev.tecte.chessWar.game.application.port.PieceLayoutLoader;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.game.domain.model.Piece;
import dev.tecte.chessWar.game.domain.model.PieceLayout;
import dev.tecte.chessWar.game.domain.model.PieceType;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import io.lumine.mythic.api.mobs.MobManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * MythicMobs API를 사용하여 기물 설계도를 로드하는 클래스입니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MythicMobsPieceLayoutLoader implements PieceLayoutLoader {
    private final MobManager mobManager;
    private final PieceMobMapper pieceMobMapper;

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public PieceLayout load() {
        log.info("Loading initial piece layout from MythicMobs...");

        Map<Coordinate, Piece> pieces = new HashMap<>();
        PieceType[] backRow = {
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };

        for (int col = 0; col < 8; col++) {
            pieces.put(new Coordinate(0, col), createPieceFromMythicMob(TeamColor.BLACK, backRow[col]));
            pieces.put(new Coordinate(1, col), createPieceFromMythicMob(TeamColor.BLACK, PieceType.PAWN));
            pieces.put(new Coordinate(6, col), createPieceFromMythicMob(TeamColor.WHITE, PieceType.PAWN));
            pieces.put(new Coordinate(7, col), createPieceFromMythicMob(TeamColor.WHITE, backRow[col]));
        }

        return new PieceLayout(pieces);
    }

    @NonNull
    private Piece createPieceFromMythicMob(@NonNull TeamColor teamColor, @NonNull PieceType pieceType) {
        String mobId = pieceMobMapper.getMobId(teamColor, pieceType);

        return mobManager.getMythicMob(mobId)
                .map(mythicMob -> Piece.builder()
                        .type(pieceType)
                        .teamColor(teamColor)
                        .mobId(mobId)
                        .build())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Required Mythic Mob '%s' is not defined in MythicMobs/mobs folder.", mobId)
                ));
    }
}
