package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.BoardState;
import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.View;
import dev.tecte.chesswar.game.GameStartEvent;
import dev.tecte.chesswar.game.GameWinEvent;
import dev.tecte.chesswar.game.TurnStartEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TurnBorderView implements View {
    private final BoardContext board;
    private final List<Entity> battlePieces = new ArrayList<>();
    private final Set<UUID> borderedPlayers = new HashSet<>();
    private Entity currentTurnPiece;

    public static TurnBorderView create(final BoardContext board, final SimulationEventBus eventBus) {
        var view = new TurnBorderView(board);

        eventBus.subscribe(GameStartEvent.class, view::onGameStart);
        eventBus.subscribe(TurnStartEvent.class, view::onTurnStart);
        eventBus.subscribe(PieceDeathEvent.class, view::onPieceDeath);
        eventBus.subscribe(GameWinEvent.class, view::onGameWin);
        eventBus.subscribe(PieceClearEvent.class, view::onPieceClear);

        return view;
    }

    public void cleanup() {
        for (UUID playerId : borderedPlayers) {
            clearPlayerBorder(playerId);
        }

        borderedPlayers.clear();
        battlePieces.clear();
        currentTurnPiece = null;
    }

    public void onPlayerQuit(final Player player) {
        if (borderedPlayers.remove(player.getUniqueId())) {
            player.setWorldBorder(null);
        }
    }

    public void onPlayerJoin(final Player player, final Entity boundPiece, final boolean isTurnOwner) {
        if (boundPiece == null || boundPiece.component(CombatComponent.class).isDead() || isTurnOwner) {
            return;
        }

        applyTileBorder(player, boundPiece);
    }

    private void onGameStart(final GameStartEvent event) {
        battlePieces.clear();
        battlePieces.addAll(event.pieces());
        currentTurnPiece = null;
    }

    private void onTurnStart(final TurnStartEvent event) {
        Entity nextPiece = event.piece();
        if (currentTurnPiece == null) {
            for (Entity piece : battlePieces) {
                if (piece.equals(nextPiece)) {
                    clearTileBorder(piece);
                } else if (!piece.component(CombatComponent.class).isDead()) {
                    applyTileBorder(piece);
                }
            }

            battlePieces.clear();
            currentTurnPiece = nextPiece;
            return;
        }

        if (!currentTurnPiece.equals(nextPiece)) {
            if (!currentTurnPiece.component(CombatComponent.class).isDead()) {
                applyTileBorder(currentTurnPiece);
            }

            clearTileBorder(nextPiece);
        }

        currentTurnPiece = nextPiece;
    }

    private void onPieceDeath(final PieceDeathEvent event) {
        Entity piece = event.piece();
        if (piece.hasComponent(PlayerComponent.class)) {
            clearTileBorder(piece);
        }
    }

    private void onGameWin(final GameWinEvent event) {
        cleanup();
    }

    private void onPieceClear(final PieceClearEvent event) {
        cleanup();
    }

    private void applyTileBorder(final Entity piece) {
        UUID playerId = piece.component(PlayerComponent.class).playerId();
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }

        applyTileBorder(player, piece);
    }

    private void applyTileBorder(final Player player, final Entity piece) {
        GridPoint gridPos = piece.component(GridComponent.class).gridPos();
        Vector center = board.toWorldPos(gridPos);
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(center.getX(), center.getZ());
        border.setSize(BoardState.TILE_SIZE);
        border.setWarningDistance(0);
        border.setWarningTime(0);
        border.setDamageAmount(0);
        player.setWorldBorder(border);
        borderedPlayers.add(player.getUniqueId());
    }

    private void clearTileBorder(final Entity piece) {
        UUID playerId = piece.component(PlayerComponent.class).playerId();
        clearPlayerBorder(playerId);
        borderedPlayers.remove(playerId);
    }

    private void clearPlayerBorder(final UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.setWorldBorder(null);
        }
    }
}
