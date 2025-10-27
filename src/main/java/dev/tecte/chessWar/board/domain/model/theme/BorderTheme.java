package dev.tecte.chessWar.board.domain.model.theme;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

/**
 * 체스판 테두리를 구성하는 블록의 재질을 정의하는 레코드입니다.
 *
 * @param inner 내부 테두리의 재질
 * @param frame 외부 프레임의 재질
 */
public record BorderTheme(Material inner, Material frame) {
    private static final Material INNER_MATERIAL = Material.STRIPPED_OAK_WOOD;
    private static final Material FRAME_MATERIAL = Material.DARK_OAK_WOOD;

    public BorderTheme {
        Objects.requireNonNull(inner, "Inner material cannot be null.");
        Objects.requireNonNull(frame, "Frame material cannot be null.");
    }

    /**
     * 기본값으로 {@link BorderTheme} 인스턴스를 생성합니다.
     *
     * @return 기본 테두리 테마
     */
    @NonNull
    public static BorderTheme defaultTheme() {
        return new BorderTheme(INNER_MATERIAL, FRAME_MATERIAL);
    }
}
