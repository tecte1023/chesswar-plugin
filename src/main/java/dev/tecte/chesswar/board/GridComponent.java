package dev.tecte.chesswar.board;

import dev.tecte.chesswar.core.Component;
import dev.tecte.chesswar.core.Entity;
import lombok.Getter;

public final class GridComponent implements Component {
    private Entity entity;
    @Getter
    private GridPoint gridPos;

    public GridComponent(final GridPoint gridPos) {
        this.gridPos = gridPos;
    }

    @Override
    public void onAttach(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public Entity entity() {
        return entity;
    }
}
