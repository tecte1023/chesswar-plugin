package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TurnOrderState implements GameState {
    private final ChessWar plugin;

    @Override
    public void onEnter(Plugin plugin, GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
        
        enforceMandatoryKing(gameManager);
        assignRandomRemainingPieces(gameManager);
        gameManager.spawnAllPiecesOnMainBoard();
        
        int whiteCount = (int) gameManager.participants().values().stream().filter(p -> p.team() == Team.WHITE).count();
        int blackCount = (int) gameManager.participants().values().stream().filter(p -> p.team() == Team.BLACK).count();
        this.plugin.boardManager().setupTurnOrderChests(whiteCount, blackCount);
        
        this.plugin.timerManager().startTurnTimer(gameManager.timerSettings().turnOrderSelectionTime());
    }

    private void enforceMandatoryKing(GameManager gameManager) {
        Set<Team> teamsWithKings = new HashSet<>();
        gameManager.participants().values().forEach(p -> {
            if (p.initialCoordinate() != null && ChessFormation.getInitialPieceType(p.initialCoordinate()) == PieceType.KING) {
                teamsWithKings.add(p.team());
            }
        });

        for (Team team : Team.values()) {
            if (teamsWithKings.contains(team)) continue;

            List<Participant> teamMembers = gameManager.participants().values().stream()
                    .filter(p -> p.team() == team)
                    .toList();

            if (teamMembers.isEmpty()) continue;

            Participant luckyMember = teamMembers.get((int) (Math.random() * teamMembers.size()));
            Coordinate kingCoord = ChessFormation.getKingCoordinate(team);

            gameManager.participants().put(luckyMember.playerId(), Participant.of(luckyMember.playerId(), team, kingCoord));

            Player player = Bukkit.getPlayer(luckyMember.playerId());
            if (player != null) {
                player.sendMessage(Component.text("팀에 킹이 없어 당신이 국왕으로 추대되었습니다!", NamedTextColor.GOLD, TextDecoration.BOLD));
                this.plugin.pieceManager().applyStats(player, PieceType.KING);

                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (PieceItemUtils.isPieceItem(item)) {
                        player.getInventory().setItem(i, null);
                    }
                }

                player.getInventory().addItem(PieceItemUtils.createPieceItem(PieceType.KING));
            }
        }
    }

    private void assignRandomRemainingPieces(GameManager gameManager) {
        for (Team team : Team.values()) {
            List<Participant> teamMembersWithoutPiece = gameManager.participants().values().stream()
                    .filter(p -> p.team() == team && p.initialCoordinate() == null)
                    .toList();

            if (teamMembersWithoutPiece.isEmpty()) continue;

            Set<Coordinate> takenCoordinates = gameManager.participants().values().stream()
                    .filter(p -> p.team() == team && p.initialCoordinate() != null)
                    .map(Participant::initialCoordinate)
                    .collect(Collectors.toSet());

            List<Coordinate> availableCoordinates = new ArrayList<>();
            int backRank = (team == Team.WHITE) ? ChessFormation.WHITE_BACK_RANK : ChessFormation.BLACK_BACK_RANK;
            int pawnRank = (team == Team.WHITE) ? ChessFormation.WHITE_PAWN_RANK : ChessFormation.BLACK_PAWN_RANK;

            for (int x = 0; x < ChessFormation.BOARD_SIZE; x++) {
                Coordinate backCoord = Coordinate.of(x, backRank);
                if (!takenCoordinates.contains(backCoord)) {
                    availableCoordinates.add(backCoord);
                }
                Coordinate pawnCoord = Coordinate.of(x, pawnRank);
                if (!takenCoordinates.contains(pawnCoord)) {
                    availableCoordinates.add(pawnCoord);
                }
            }

            java.util.Collections.shuffle(availableCoordinates);

            for (int i = 0; i < teamMembersWithoutPiece.size() && i < availableCoordinates.size(); i++) {
                Participant p = teamMembersWithoutPiece.get(i);
                Coordinate randomCoord = availableCoordinates.get(i);
                gameManager.participants().put(p.playerId(), Participant.of(p.playerId(), team, randomCoord));

                Player player = Bukkit.getPlayer(p.playerId());
                if (player != null) {
                    PieceType type = ChessFormation.getInitialPieceType(randomCoord);
                    this.plugin.pieceManager().applyStats(player, type);
                    PieceItemUtils.replacePlayerPieceItem(player, type);
                    player.sendMessage(Component.text("기물을 선택하지 않아 무작위 기물(" + type.displayName() + ")이 배정되었습니다.", NamedTextColor.YELLOW));
                }
            }
        }
    }

    @Override
    public GameState nextState() {
        return new BattleState(plugin);
    }

    @Override
    public String displayName() {
        return "순서 정하기";
    }
}
