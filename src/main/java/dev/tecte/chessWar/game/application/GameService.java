package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.domain.exception.GameStartConditionException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.Piece;
import dev.tecte.chessWar.game.domain.model.PieceLayout;
import dev.tecte.chessWar.game.domain.policy.TeamDirectionPolicy;
import dev.tecte.chessWar.team.application.TeamService;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;

/**
 * 게임의 생명주기와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameService {
    private static final int MIN_PLAYERS = 0;
    private static final int MOB_LEVEL = 1;
    private static final int SPAWN_AMOUNT_PER_TICK = 4;

    private final PieceLayout pieceLayout;
    private final TeamDirectionPolicy teamDirectionPolicy;
    private final BoardService boardService;
    private final TeamService teamService;
    private final MobManager mobManager;
    private final ChessWar plugin;

    /**
     * 게임을 시작합니다.
     * 게임 시작 조건을 확인한 후, 몹을 소환하고 게임을 시작 상태로 전환합니다.
     *
     * @param sender 명령어를 실행한 주체
     * @throws GameStartConditionException 게임 시작 조건을 충족하지 못했을 경우
     */
    @HandleException
    public void startGame(@NonNull CommandSender sender) {
        if (!teamService.areAllTeamsReadyToStart(MIN_PLAYERS)) {
            throw GameStartConditionException.forTeamsNotReady(MIN_PLAYERS);
        }

        Board board = boardService.getBoard().orElseThrow(GameStartConditionException::forBoardNotExists);
        World world = Bukkit.getWorld(board.worldName());
        Game game = new Game(board, pieceLayout.pieces());

        if (world == null) {
            throw GameStartConditionException.forWorldNotFound(board.worldName());
        }

        spawnPieces(game, world);
    }

    private void spawnPieces(@NonNull Game game, @NonNull World world) {
        var piecesToSpawn = new ArrayList<>(game.pieces().entrySet());

        new BukkitRunnable() {
            private int currentIndex = 0;

            @Override
            public void run() {
                for (int i = 0; i < SPAWN_AMOUNT_PER_TICK; i++) {
                    if (currentIndex >= piecesToSpawn.size()) {
                        cancel();

                        return;
                    }

                    spawnSinglePiece(game, world, piecesToSpawn.get(currentIndex));
                    currentIndex++;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnSinglePiece(
            @NonNull Game game,
            @NonNull World world,
            @NonNull Map.Entry<Coordinate, Piece> pieceEntry
    ) {
        Coordinate coordinate = pieceEntry.getKey();
        Piece piece = pieceEntry.getValue();
        MythicMob mythicMob = mobManager.getMythicMob(piece.mobId())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("게임 시작 실패: 필수 몹 '%s'를 찾을 수 없습니다.", piece.mobId())
                ));
        Board board = game.board();
        Location spawnLocation = board.spawnPositionVector(coordinate).toLocation(world);
        Vector direction = teamDirectionPolicy.calculateFacingVector(piece, board);

        spawnLocation.setDirection(direction);
        mythicMob.spawn(BukkitAdapter.adapt(spawnLocation), MOB_LEVEL);
    }
}
