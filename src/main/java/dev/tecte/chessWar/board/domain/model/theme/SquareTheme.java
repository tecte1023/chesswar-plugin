package dev.tecte.chessWar.board.domain.model.theme;

import lombok.NonNull;
import org.bukkit.Material;

import java.util.Objects;

/**
 * 체스판의 각 칸을 구성하는 블록의 재질을 정의하는 레코드입니다.
 *
 * @param white 흰색 칸의 재질
 * @param black 검은색 칸의 재질
 */
public record SquareTheme(Material white, Material black) {
    private static final Material WHITE_MATERIAL = Material.STRIPPED_PALE_OAK_WOOD;
    private static final Material BLACK_MATERIAL = Material.STRIPPED_DARK_OAK_WOOD;

    public SquareTheme {
        Objects.requireNonNull(white, "White material cannot be null.");
        Objects.requireNonNull(black, "Black material cannot be null.");
    }

    /**
     * 기본값으로 {@link SquareTheme} 인스턴스를 생성합니다.
     *
     * @return 기본 칸 테마
     */
    @NonNull
    public static SquareTheme defaultTheme() {
        return new SquareTheme(WHITE_MATERIAL, BLACK_MATERIAL);
    }
}
