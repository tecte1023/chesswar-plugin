package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.piece.domain.model.PieceType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * 게임 진행 중 참가자에게 전달할 메시지 목록입니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GameMessage {
    SELECTION_TITLE(
            Component.text("기물 선택").decorate(TextDecoration.BOLD).color(NamedTextColor.AQUA)
    ),
    SELECTION_GUIDE(
            Component.text("기물을 우클릭하여 참전할 기물을 선택해주세요.").color(NamedTextColor.RED)
    ),
    SELECTION_WAITING(
            Component.text("기물 선택 완료! 다른 플레이어를 기다리는 중...").color(NamedTextColor.GREEN)
    ),
    SELECTION_COMPLETED(
            Component.text("기물 선택이 모두 완료되었습니다!").color(NamedTextColor.AQUA)
    ),
    GAME_STOPPED(
            Component.text("게임이 중단되었습니다.")
    );

    private final Component content;

    /**
     * 기물 선택 완료 메시지를 생성합니다.
     *
     * @param pieceType 선택된 기물 타입
     * @return 조립된 메시지
     */
    public static Component pieceSelected(@NonNull PieceType pieceType) {
        return Component.text()
                .append(pieceType.formattedName().color(NamedTextColor.GOLD))
                .append(Component.text(" 기물로 전장에 참전합니다."))
                .build();
    }
}

