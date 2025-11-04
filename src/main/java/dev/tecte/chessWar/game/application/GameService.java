package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.board.domain.model.Board;
import dev.tecte.chessWar.board.domain.model.Coordinate;
import dev.tecte.chessWar.board.domain.model.Square;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.game.domain.exception.GameStartConditionException;
import dev.tecte.chessWar.game.domain.model.Game;
import dev.tecte.chessWar.game.domain.model.Piece;
import dev.tecte.chessWar.game.domain.model.PieceLayout;
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

    private final PieceLayout pieceLayout;
    private final BoardService boardService;
    private final TeamService teamService;
    private final MobManager mobManager;

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
        for (Map.Entry<Coordinate, Piece> entry : game.pieces().entrySet()) {
            Coordinate coordinate = entry.getKey();
            Piece piece = entry.getValue();
            MythicMob mythicMob = mobManager.getMythicMob(piece.mobId())
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("게임 시작 실패: 필수 몹 '%s'를 찾을 수 없습니다.", piece.mobId())
                    ));
            Square square = game.board().squareGrid().squareAt(coordinate);
            Location spawnLocation = square.boundingBox().getCenter().toLocation(world).add(0, 1, 0);

            mythicMob.spawn(BukkitAdapter.adapt(spawnLocation), MOB_LEVEL);
        }
    }
}
