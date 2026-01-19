package dev.tecte.chessWar.piece.infrastructure.mythicmobs;

import dev.tecte.chessWar.piece.application.port.PieceIdResolver;
import dev.tecte.chessWar.piece.application.port.PieceStatProvider;
import dev.tecte.chessWar.piece.application.port.dto.PieceStatsDto;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import io.lumine.mythic.api.mobs.MobManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * MythicMobs를 통해 기물의 스탯 정보를 제공하는 어댑터 구현체입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MythicMobsPieceStatProvider implements PieceStatProvider {
    private final PieceIdResolver pieceIdResolver;
    private final MobManager mobManager;

    @NonNull
    @Override
    public PieceStatsDto getStats(@NonNull PieceSpec spec) {
        String mobId = pieceIdResolver.resolveId(spec.teamColor(), spec.type());

        return mobManager.getMythicMob(mobId)
                .map(mob -> new PieceStatsDto(mob.getHealth().get(), mob.getDamage().get()))
                .orElse(PieceStatsDto.DEFAULT);
    }
}
