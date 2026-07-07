package dev.tecte.chesswar.board;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class BoardManager {
    @NotNull
    private final BoardComponent boardComponent;

    @NotNull
    private final BoardUIComponent uiComponent;

    @NotNull
    private final BoardPresenter presenter;

    public void toggleBoardVisibility(@NotNull final Player player) {
        applyVisibility(player, !uiComponent.adminViewers().contains(player.getUniqueId()));
    }

    public void setBoardVisibility(@NotNull final Player player, final boolean state) {
        if (uiComponent.adminViewers().contains(player.getUniqueId()) == state) {
            return;
        }

        applyVisibility(player, state);
    }

    private void applyVisibility(@NotNull final Player player, final boolean state) {
        final Board board = boardComponent.board();

        if (board == null) {
            return;
        }

        final List<UUID> viewers = uiComponent.adminViewers();

        if (state) {
            viewers.add(player.getUniqueId());
            presenter.showGrid(player, board);
        } else {
            viewers.remove(player.getUniqueId());
            presenter.hideGrid(player);
        }
    }
}
