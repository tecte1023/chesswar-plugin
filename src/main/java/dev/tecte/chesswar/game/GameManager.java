package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class GameManager {
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<Coordinate, Piece> boardPieces = new HashMap<>();
    private final List<UUID> turnOrder = new ArrayList<>();

    @Setter
    private GamePhase phase = GamePhase.WAITING;
    private int currentTurnIndex = -1;

    public void join(Player player, Team team) {
        UUID playerId = player.getUniqueId();

        participants.put(playerId, Participant.of(playerId, team));
    }

    public void selectPiece(Player player, PieceType pieceType) {
        UUID playerId = player.getUniqueId();
        Participant participant = participants.get(playerId);

        if (participant == null) {
            return;
        }

        participants.put(playerId, Participant.of(playerId, participant.team(), pieceType));
        applyStats(player, pieceType);
    }

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
    }

    public boolean isParticipant(Player player) {
        return participants.containsKey(player.getUniqueId());
    }

    public Optional<Participant> findParticipant(UUID playerId) {
        return Optional.ofNullable(participants.get(playerId));
    }

    public void prepareTurnOrder() {
        turnOrder.clear();

        List<UUID> whiteTeam = new ArrayList<>();
        List<UUID> blackTeam = new ArrayList<>();

        for (Participant p : participants.values()) {
            if (p.team() == Team.WHITE) {
                whiteTeam.add(p.playerId());
            } else {
                blackTeam.add(p.playerId());
            }
        }

        int maxSize = Math.max(whiteTeam.size(), blackTeam.size());

        for (int i = 0; i < maxSize; i++) {
            if (i < whiteTeam.size()) {
                turnOrder.add(whiteTeam.get(i));
            }

            if (i < blackTeam.size()) {
                turnOrder.add(blackTeam.get(i));
            }
        }

        currentTurnIndex = 0;
    }

    public Optional<UUID> currentTurnPlayer() {
        if (currentTurnIndex < 0 || currentTurnIndex >= turnOrder.size()) {
            return Optional.empty();
        }

        return Optional.of(turnOrder.get(currentTurnIndex));
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) {
            return;
        }

        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
    }

    public void placePiece(Coordinate coordinate, Piece piece) {
        boardPieces.put(coordinate, piece);
    }

    public void removePiece(Coordinate coordinate) {
        boardPieces.remove(coordinate);
    }

    public Optional<Piece> findPieceAt(Coordinate coordinate) {
        return Optional.ofNullable(boardPieces.get(coordinate));
    }

    public void win(Team winner) {
        phase = GamePhase.ENDED;

        Component winMessage = Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(winner.displayName(), winner.textColor(), TextDecoration.BOLD))
                .append(Component.text("가 승리했습니다!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .build();

        Bukkit.broadcast(winMessage);
        
        // WHY: 게임 종료 후 데이터를 즉시 삭제하지 않고 잠시 유지하여 통계를 확인
        // TODO: 일정 시간 뒤 자동 초기화 로직 추가 필요
    }

    public void reset() {
        phase = GamePhase.WAITING;
        boardPieces.clear();
        turnOrder.clear();
        currentTurnIndex = -1;

        for (Participant p : participants.values()) {
            Player player = Bukkit.getPlayer(p.playerId());

            if (player != null) {
                resetStats(player);
            }
        }
    }

    private void applyStats(Player player, PieceType type) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(type.baseHp());
            player.setHealth(type.baseHp());
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(type.baseDamage());
        }
    }

    private void resetStats(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0);
            player.setHealth(20.0);
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(1.0);
        }
    }
}
