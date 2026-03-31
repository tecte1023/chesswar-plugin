package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 기물을 소환하거나 제거합니다.
 */
public interface PieceSpawner {
    /**
     * 기물을 소환합니다.
     *
     * @param spec     기물 명세
     * @param location 소환 위치
     * @return 소환된 기물
     */
    @NonNull
    Entity spawn(@NonNull PieceSpec spec, @NonNull Location location);

    /**
     * 모든 기물을 소환합니다.
     *
     * @param board       체스판 정보
     * @param pieceLayout 기물 배치 정보
     * @param sender      행위자
     * @return 소환된 기물 배치 현황
     */
    @NonNull
    CompletableFuture<Map<Coordinate, UnitPiece>> spawnAll(
            @NonNull Board board,
            @NonNull Map<Coordinate, PieceSpec> pieceLayout,
            @NonNull CommandSender sender
    );

    /**
     * 기물을 제거합니다.
     *
     * @param entityId 기물 ID
     */
    void despawn(@NonNull UUID entityId);
}
