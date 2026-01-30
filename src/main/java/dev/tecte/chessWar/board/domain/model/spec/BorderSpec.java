package dev.tecte.chessWar.board.domain.model.spec;

import dev.tecte.chessWar.board.domain.model.BorderType;
import lombok.NonNull;

/**
 * 체스판 테두리의 두께 명세를 정의하는 불변 객체입니다.
 *
 * @param innerThickness 내부 테두리의 두께
 * @param frameThickness 외부 프레임의 두께
 */
public record BorderSpec(int innerThickness, int frameThickness) {
    private static final int DEFAULT_INNER_THICKNESS = 1;
    private static final int DEFAULT_FRAME_THICKNESS = 2;

    public BorderSpec {
        if (innerThickness < 0) {
            throw new IllegalArgumentException("Inner thicknesses cannot be negative.");
        }

        if (frameThickness < 0) {
            throw new IllegalArgumentException("Frame thicknesses cannot be negative.");
        }
    }

    /**
     * 기본 테두리 명세를 생성합니다.
     *
     * @return 기본 테두리 명세
     */
    @NonNull
    public static BorderSpec defaultSpec() {
        return new BorderSpec(DEFAULT_INNER_THICKNESS, DEFAULT_FRAME_THICKNESS);
    }

    /**
     * 주어진 타입의 두께를 반환합니다.
     *
     * @param borderType 대상 테두리 타입
     * @return 해당 테두리의 두께
     */
    public int thickness(@NonNull BorderType borderType) {
        return switch (borderType) {
            case INNER_BORDER -> innerThickness;
            case FRAME -> frameThickness;
        };
    }
}
