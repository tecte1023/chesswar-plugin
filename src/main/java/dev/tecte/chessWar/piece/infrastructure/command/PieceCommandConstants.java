package dev.tecte.chessWar.piece.infrastructure.command;

import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * 기물 명령어 상수 집합입니다.
 */
@UtilityClass
public class PieceCommandConstants {
    public final String PIECE = "piece";
    public final String SELECT = "select";

    /**
     * 기물 선택 명령어를 생성합니다.
     *
     * @param pieceId 선택할 기물의 식별자
     * @return 완성된 명령어
     */
    @NonNull
    public String buildSelectCommand(@NonNull UUID pieceId) {
        return String.format("/%s %s %s %s", CommandConstants.ROOT, PIECE, SELECT, pieceId);
    }
}
