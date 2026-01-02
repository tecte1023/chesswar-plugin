package dev.tecte.chessWar.piece.infrastructure.mythicmobs;

import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.piece.application.port.PieceIdResolver;
import dev.tecte.chessWar.piece.application.port.PieceLayoutLoader;
import dev.tecte.chessWar.piece.domain.model.PieceLayout;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.piece.infrastructure.mythicmobs.exception.MythicMobSpawnException;
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
 * MythicMobs API를 활용하여 체스판의 초기 기물 배치를 로드하는 클래스입니다.
 * 이 구현체는 표준 체스 규칙에 따른 기물 배치를 MythicMobs의 몹 정보와 매핑하여 생성합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MythicMobsPieceLayoutLoader implements PieceLayoutLoader {
    private final MobManager mobManager;
    private final PieceIdResolver pieceIdResolver;

    /**
     * 표준 체스 규칙에 따라 초기 기물 배치를 생성하고 반환합니다.
     * 백팀은 0, 1행에, 흑팀은 6, 7행에 배치됩니다.
     *
     * @return 초기 기물 배치 정보가 담긴 {@link PieceLayout}
     */
    @NonNull
    @Override
    public PieceLayout load() {
        log.info("Loading initial piece layout from MythicMobs...");

        Map<Coordinate, PieceSpec> pieces = new HashMap<>();
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
            pieces.put(new Coordinate(0, col), createPieceFromMythicMob(TeamColor.WHITE, backRow[col]));
            pieces.put(new Coordinate(1, col), createPieceFromMythicMob(TeamColor.WHITE, PieceType.PAWN));
            pieces.put(new Coordinate(6, col), createPieceFromMythicMob(TeamColor.BLACK, PieceType.PAWN));
            pieces.put(new Coordinate(7, col), createPieceFromMythicMob(TeamColor.BLACK, backRow[col]));
        }

        return PieceLayout.of(pieces);
    }

    @NonNull
    private PieceSpec createPieceFromMythicMob(@NonNull TeamColor teamColor, @NonNull PieceType pieceType) {
        String templateId = pieceIdResolver.resolveId(teamColor, pieceType);
        PieceSpec spec = PieceSpec.of(pieceType, teamColor);

        return mobManager.getMythicMob(templateId)
                .map(mythicMob -> spec)
                .orElseThrow(() -> MythicMobSpawnException.notFound(templateId));
    }
}
