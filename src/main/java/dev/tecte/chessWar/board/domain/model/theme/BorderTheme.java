package dev.tecte.chessWar.board.domain.model.theme;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

/**
 * 체스판 테두리의 블록 재질을 정의하는 불변 객체입니다.
 *
 * @param inner 내부 테두리의 재질
 * @param frame 외부 프레임의 재질
 */
public record BorderTheme(Material inner, Material frame) {
    public BorderTheme {
        Objects.requireNonNull(inner, "Inner material cannot be null.");
        Objects.requireNonNull(frame, "Frame material cannot be null.");
    }

    /**
     * 기본 테두리 테마를 생성합니다.
     *
     * @return 기본 테두리 테마
     */
    @NonNull
    public static BorderTheme defaultTheme() {
        return new BorderTheme(Material.STRIPPED_OAK_WOOD, Material.DARK_OAK_WOOD);
    }
}
