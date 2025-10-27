package dev.tecte.chessWar.board.domain.model.spec;

import lombok.NonNull;

/**
 * 체스판의 각 칸의 크기(너비와 높이)를 정의하는 레코드입니다.
 *
 * @param width  칸의 너비
 * @param height 칸의 높이
 */
public record SquareSpec(int width, int height) {
    private static final int WIDTH = 3;
    private static final int HEIGHT = 3;

    public SquareSpec {
        if (width <= 0) {
            throw new IllegalArgumentException("Square width must be positive.");
        }

        if (height <= 0) {
            throw new IllegalArgumentException("Square height must be positive.");
        }
    }

    /**
     * 기본값으로 {@link SquareSpec} 인스턴스를 생성합니다.
     *
     * @return 기본 칸 명세
     */
    @NonNull
    public static SquareSpec defaultSpec() {
        return new SquareSpec(WIDTH, HEIGHT);
    }
}
