package dev.tecte.chessWar.game.application.port;

import dev.tecte.chessWar.game.domain.model.PieceSpec;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * 체스 기물을 월드에 스폰하는 역할을 정의하는 포트 인터페이스입니다.
 */
public interface PieceSpawner {
    /**
     * 주어진 명세와 위치에 따라 기물을 스폰합니다.
     *
     * @param spec     스폰할 기물의 명세
     * @param location 스폰할 위치
     * @return 스폰된 Bukkit 엔티티
     */
    @NonNull
    Entity spawnPiece(@NonNull PieceSpec spec, @NonNull Location location);
}
