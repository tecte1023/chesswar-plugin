package dev.tecte.chesswar.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Accessors(fluent = true)
public class BoardManager {
    private ChessBoard currentBoard;

    public boolean hasBoard() {
        return currentBoard != null;
    }

    public void currentBoard(ChessBoard board) {
        if (currentBoard != null) {
            log.info("새로운 체스판이 설정되어 기존 체스판의 데이터를 덮어씁니다.");
        }

        currentBoard = board;
    }
}
