package dev.tecte.chessWar.board.application.port;

import dev.tecte.chessWar.board.domain.model.Board;
import lombok.NonNull;
import org.bukkit.World;

/**
 * 체스판의 논리적 모델을 마인크래프트 세계에 렌더링하는 역할을 정의하는 인터페이스입니다.
 */
public interface BoardRenderer {
    /**
     * 주어진 체스판 모델을 특정 월드에 실제 블록으로 렌더링합니다.
     *
     * @param board 렌더링할 체스판 모델
     * @param world 체스판이 렌더링될 월드
     */
    void render(@NonNull Board board, @NonNull World world);
}
