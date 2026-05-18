package dev.tecte.chessWar.piece.infrastructure.command;

import dev.tecte.chessWar.common.identity.ProjectIdentity;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * 기물 관련 명령어 라우팅 정보를 관리합니다.
 */
@UtilityClass
public class PieceCommandRouting {
    public final String PIECE = "piece";
    public final String SELECT = "select";

    /**
     * 기물 선택 명령어를 생성합니다.
     *
     * @param pieceId 선택할 기물 ID
     * @return 완성된 명령어
     */
    @NonNull
    public String buildSelectCommand(@NonNull UUID pieceId) {
        return String.format("/%s %s %s %s", ProjectIdentity.NAMESPACE, PIECE, SELECT, pieceId);
    }
}
