package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.ChessFormation;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
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
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
        
        enforceMandatoryKing(gameManager);
        assignRandomRemainingPieces(gameManager);
        gameManager.spawnAllPiecesOnMainBoard();
        
        final int whiteCount = (int) gameManager.participants().values().stream().filter(p -> p.team() == Team.WHITE).count();
        final int blackCount = (int) gameManager.participants().values().stream().filter(p -> p.team() == Team.BLACK).count();
        plugin.boardManager().setupTurnOrderChests(whiteCount, blackCount);
        
        plugin.timerManager().startTurnTimer(gameManager.timerSettings().turnOrderSelectionTime());
    }

    private void enforceMandatoryKing(final GameManager gameManager) {
        final Set<Team> teamsWithKings = new HashSet<>();
        gameManager.participants().values().forEach(p -> {
            if (p.initialCoordinate() != null && ChessFormation.getInitialPieceType(p.initialCoordinate()) == PieceType.KING) {
                teamsWithKings.add(p.team());
            }
        });

        for (final Team team : Team.values()) {
            if (teamsWithKings.contains(team)) continue;

            final List<Participant> teamMembers = gameManager.participants().values().stream()
                    .filter(p -> p.team() == team)
                    .toList();

            if (teamMembers.isEmpty()) continue;

            final Participant luckyMember = teamMembers.get((int) (Math.random() * teamMembers.size()));
            final Coordinate kingCoord = ChessFormation.getKingCoordinate(team);

            luckyMember.initialCoordinate(kingCoord);

            final Player player = Bukkit.getPlayer(luckyMember.playerId());
            if (player != null) {
                player.sendMessage(Component.text("팀에 킹이 없어 당신이 국왕으로 추대되었습니다!", NamedTextColor.GOLD, TextDecoration.BOLD));
                plugin.pieceManager().applyStats(player, PieceType.KING);

                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    final ItemStack item = player.getInventory().getItem(i);
                    if (PieceItemUtils.isPieceItem(item)) {
                        player.getInventory().setItem(i, null);
                    }
                }

                player.getInventory().addItem(PieceItemUtils.createPieceItem(PieceType.KING));
            }
        }
    }

    private void assignRandomRemainingPieces(final GameManager gameManager) {
        for (final Team team : Team.values()) {
            final List<Participant> teamMembersWithoutPiece = gameManager.participants().values().stream()
                    .filter(p -> p.team() == team && p.initialCoordinate() == null)
                    .toList();

            if (teamMembersWithoutPiece.isEmpty()) continue;

            final Set<Coordinate> takenCoordinates = gameManager.participants().values().stream()
                    .filter(p -> p.team() == team && p.initialCoordinate() != null)
                    .map(Participant::initialCoordinate)
                    .collect(Collectors.toSet());

            final List<Coordinate> availableCoordinates = new ArrayList<>();
            final int backRank = (team == Team.WHITE) ? ChessFormation.WHITE_BACK_RANK : ChessFormation.BLACK_BACK_RANK;
            final int pawnRank = (team == Team.WHITE) ? ChessFormation.WHITE_PAWN_RANK : ChessFormation.BLACK_PAWN_RANK;

            for (int x = 0; x < ChessFormation.BOARD_SIZE; x++) {
                final Coordinate backCoord = Coordinate.of(x, backRank);
                if (!takenCoordinates.contains(backCoord)) {
                    availableCoordinates.add(backCoord);
                }
                final Coordinate pawnCoord = Coordinate.of(x, pawnRank);
                if (!takenCoordinates.contains(pawnCoord)) {
                    availableCoordinates.add(pawnCoord);
                }
            }

            java.util.Collections.shuffle(availableCoordinates);

            for (int i = 0; i < teamMembersWithoutPiece.size() && i < availableCoordinates.size(); i++) {
                final Participant p = teamMembersWithoutPiece.get(i);
                final Coordinate randomCoord = availableCoordinates.get(i);
                p.initialCoordinate(randomCoord);

                final Player player = Bukkit.getPlayer(p.playerId());
                if (player != null) {
                    final PieceType type = ChessFormation.getInitialPieceType(randomCoord);
                    plugin.pieceManager().applyStats(player, type);
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
    public GamePhase phase() {
        return GamePhase.TURN_ORDER;
    }

    @Override
    public String displayName() {
        return "순서 조율";
    }

    @Override
    public void handleReadyUp(final GameManager gameManager, final Player player, final org.bukkit.Location location) {
        if (gameManager.isReady(player.getUniqueId())) {
            player.sendMessage(Component.text("이미 준비 완료 상태입니다.", NamedTextColor.YELLOW));
            return;
        }

        if (location != null) {
            final boolean isMyChest = gameManager.findParticipant(player.getUniqueId())
                    .map(p -> plugin.boardManager().isTeamChest(location, p.team()))
                    .orElse(false);

            if (!isMyChest) {
                player.sendMessage(Component.text("자신의 팀 막사에 있는 상자에서만 준비를 완료할 수 있습니다!", NamedTextColor.RED));
                return;
            }
        }

        gameManager.toggleReady(player.getUniqueId(), true);
        player.sendMessage(Component.text("준비 완료! 모든 인원이 준비되면 게임이 시작됩니다.", NamedTextColor.GREEN));

        final java.util.Optional<Participant> participant = gameManager.findParticipant(player.getUniqueId());
        participant.ifPresent(p -> {
            final Team team = p.team();
            final Component msg = Component.text(player.getName() + "님이 준비되었습니다! ", NamedTextColor.GRAY)
                    .append(Component.text("(" + gameManager.countReady(team) + "/" + gameManager.countTeam(team) + ")", NamedTextColor.AQUA));

            for (final Participant other : gameManager.participants().values()) {
                if (other.team() == team) {
                    final Player online = player.getServer().getPlayer(other.playerId());
                    if (online != null) {
                        online.sendMessage(msg);
                    }
                }
            }
        });

        if (gameManager.areAllParticipantsReady()) {
            Bukkit.broadcast(Component.text()
                    .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("모든 플레이어가 준비를 마쳤습니다! " + gameManager.timerSettings().readyAccelerateTime() + "초 후 전투가 시작됩니다.", NamedTextColor.GREEN, TextDecoration.BOLD))
                    .build());
            
            // TODO: 타이머 가속 로직이 여기에 올 수 있음
        }
    }
}
