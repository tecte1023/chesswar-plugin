package dev.tecte.chessWar.board.domain.model.theme;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

/**
 * 체스판 칸의 블록 재질을 정의하는 불변 객체입니다.
 *
 * @param white 흰색 칸의 재질
 * @param black 검은색 칸의 재질
 */
public record SquareTheme(Material white, Material black) {
    public SquareTheme {
        Objects.requireNonNull(white, "White material cannot be null.");
        Objects.requireNonNull(black, "Black material cannot be null.");
    }

    /**
     * 기본 칸 테마를 생성합니다.
     *
     * @return 기본 칸 테마
     */
    @NonNull
    public static SquareTheme defaultTheme() {
        return new SquareTheme(Material.STRIPPED_PALE_OAK_WOOD, Material.STRIPPED_DARK_OAK_WOOD);
    }
}
