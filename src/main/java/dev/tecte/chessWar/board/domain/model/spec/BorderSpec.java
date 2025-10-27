package dev.tecte.chessWar.board.domain.model.spec;

import dev.tecte.chessWar.board.domain.model.BorderType;
import lombok.NonNull;

import java.util.Map;

/**
 * 체스판 테두리의 두께 명세를 정의하는 레코드입니다.
 *
 * @param innerThickness 내부 테두리의 두께
 * @param frameThickness 외부 프레임의 두께
 */
public record BorderSpec(int innerThickness, int frameThickness) {
    private static final int DEFAULT_INNER_THICKNESS = 1;
    private static final int DEFAULT_FRAME_THICKNESS = 2;
    private static final BorderType INNER_BORDER_TYPE = BorderType.INNER_BORDER;
    private static final BorderType FRAME_BORDER_TYPE = BorderType.FRAME;
    private static final Map<BorderType, Integer> DEFAULT_THICKNESSES = Map.of(
            INNER_BORDER_TYPE, DEFAULT_INNER_THICKNESS,
            FRAME_BORDER_TYPE, DEFAULT_FRAME_THICKNESS
    );

    public BorderSpec {
        if (innerThickness < 0) {
            throw new IllegalArgumentException("Inner thicknesses cannot be negative.");
        }

        if (frameThickness < 0) {
            throw new IllegalArgumentException("Frame thicknesses cannot be negative.");
        }
    }

    /**
     * 기본값으로 {@link BorderSpec} 인스턴스를 생성합니다.
     *
     * @return 기본 테두리 명세
     */
    @NonNull
    public static BorderSpec defaultSpec() {
        return new BorderSpec(DEFAULT_INNER_THICKNESS, DEFAULT_FRAME_THICKNESS);
    }

    /**
     * 내부 테두리의 {@link BorderType}을 반환합니다.
     *
     * @return 내부 테두리 타입
     */
    @NonNull
    public BorderType innerBorderType() {
        return INNER_BORDER_TYPE;
    }

    /**
     * 외부 프레임의 {@link BorderType}을 반환합니다.
     *
     * @return 외부 프레임 타입
     */
    @NonNull
    public BorderType frameBorderType() {
        return FRAME_BORDER_TYPE;
    }

    /**
     * 주어진 {@link BorderType}에 해당하는 두께를 반환합니다.
     *
     * @param borderType 두께를 조회할 테두리 타입
     * @return 해당 테두리의 두께
     */
    public int thickness(@NonNull BorderType borderType) {
        return DEFAULT_THICKNESSES.get(borderType);
    }
}
