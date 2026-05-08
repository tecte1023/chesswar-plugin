package dev.tecte.chessWar.piece.application;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.port.EntityResolver;
import dev.tecte.chessWar.piece.application.port.PieceSpawner;
import dev.tecte.chessWar.piece.domain.model.Piece;
import dev.tecte.chessWar.piece.domain.model.PieceLayout;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 기물의 생명주기와 비즈니스 로직을 처리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceService {
    private final PieceLayout pieceLayout;
    private final PieceSpawner pieceSpawner;
    private final EntityResolver entityResolver;
    private final JavaPlugin plugin;

    /**
     * 체스판 위에 기물을 비동기로 소환합니다.
     *
     * @param board   체스판
     * @param starter 행위자
     * @return 소환된 기물 배치를 포함한 미래
     */
    @NonNull
    public CompletableFuture<Map<Coordinate, UnitPiece>> spawnPieces(
            @NonNull Board board,
            @NonNull CommandSender starter
    ) {
        return pieceSpawner.spawnAll(board, pieceLayout.pieces(), starter);
    }

    /**
     * 기물들을 제거합니다.
     *
     * @param pieces 기물 목록
     */
    public void despawnPieces(@NonNull Collection<UnitPiece> pieces) {
        pieces.forEach(piece -> pieceSpawner.despawn(piece.id()));
    }

    /**
     * 적 팀 기물의 가시성을 업데이트합니다.
     *
     * @param participants 참여자 목록
     * @param unitPieces   기물 목록
     */
    public void updateVisibilityFor(
            @NonNull Map<Player, TeamColor> participants,
            @NonNull Collection<UnitPiece> unitPieces
    ) {
        participants.forEach((player, team) -> updateVisibilityFor(player, team, unitPieces));
    }

    /**
     * 특정 플레이어의 기물 가시성을 업데이트합니다.
     *
     * @param player     플레이어
     * @param playerTeam 소속 팀
     * @param unitPieces 기물 목록
     */
    public void updateVisibilityFor(
            @NonNull Player player,
            @NonNull TeamColor playerTeam,
            @NonNull Collection<UnitPiece> unitPieces
    ) {
        List<Entity> enemyEntities = findPieceEntities(unitPieces, playerTeam.opposite());

        setPieceVisibility(player, enemyEntities, false);
    }

    private void setPieceVisibility(
            @NonNull Player player,
            @NonNull List<Entity> entities,
            boolean visible
    ) {
        entities.forEach(entity -> {
            if (visible) {
                player.showEntity(plugin, entity);
            } else {
                player.hideEntity(plugin, entity);
            }
        });
    }

    @NonNull
    private List<Entity> findPieceEntities(@NonNull Collection<UnitPiece> pieces, @NonNull TeamColor teamColor) {
        return pieces.stream()
                .filter(piece -> piece.isTeam(teamColor))
                .map(Piece::id)
                .map(entityResolver::findEntity)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
