package dev.tecte.chessWar.game.application;

import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.application.port.GameTaskManager;
import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.port.UserNotifier;
import dev.tecte.chessWar.team.application.TeamService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * 게임 진행 중 발생하는 사건을 알립니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameAnnouncer {
    private final GameRepository gameRepository;
    private final GameTaskManager gameTaskManager;
    private final TeamService teamService;
    private final UserNotifier userNotifier;

    /**
     * 기물 선택 시작을 알립니다.
     *
     * @param targets 대상 참가자 목록
     */
    public void announceSelectionStart(@NonNull Set<Player> targets) {
        targets.forEach(player -> userNotifier.displayTitle(player, GameMessage.SELECTION_TITLE.content()));
    }

    /**
     * 현재 게임 상태에 맞게 기물 선택 가이드를 재개합니다.
     */
    public void restoreSelectionGuidance() {
        gameRepository.find().ifPresent(game -> {
            if (!game.isInSelectionPhase()) {
                return;
            }

            if (game.hasSelectedPiece(teamService.findAllParticipantIds())) {
                announceSelectionCompletion();
            } else {
                startSelectionGuidance();
            }
        });
    }

    /**
     * 기물 선택 가이드를 시작합니다.
     */
    public void startSelectionGuidance() {
        long initialDelay = 0L;
        long intervalTicks = 2 * 20L;

        gameTaskManager.runRepeating(
                GameTaskType.GUIDANCE,
                this::refreshParticipantsStatus,
                initialDelay,
                intervalTicks
        );
    }

    /**
     * 기물 선택 가이드를 중단합니다.
     */
    public void stopGuidance() {
        gameTaskManager.cancel(GameTaskType.GUIDANCE);
    }

    /**
     * 기물 선택 상태를 반영하여 가이드를 즉시 새로고침합니다.
     *
     * @param player 대상 참가자
     */
    public void refreshSelectionStatus(@NonNull Player player) {
        gameRepository.find().ifPresent(game -> {
            if (!game.isInSelectionPhase()) {
                return;
            }

            if (game.hasSelectedPiece(teamService.findAllParticipantIds())) {
                userNotifier.displayActionBar(player, GameMessage.SELECTION_COMPLETED.content());

                return;
            }

            boolean hasSelected = game.hasSelectedPiece(player.getUniqueId());

            sendSelectionStatus(player, hasSelected);
        });
    }

    /**
     * 기물 선택 완료를 알립니다.
     *
     * @param player    대상 참가자
     * @param pieceType 선택된 기물 타입
     */
    public void notifyPieceSelection(@NonNull Player player, @NonNull PieceType pieceType) {
        userNotifier.informSuccess(player, GameMessage.pieceSelected(pieceType));
    }

    /**
     * 모든 참가자의 기물 선택 완료를 알립니다.
     */
    public void announceSelectionCompletion() {
        stopGuidance();
        gameTaskManager.runRepeating(
                GameTaskType.GUIDANCE,
                this::displayCompletionStatus,
                0L,
                2 * 20L
        );
    }

    /**
     * 게임 중단을 알립니다.
     *
     * @param requester 행위자
     */
    public void notifyGameStop(@NonNull CommandSender requester) {
        userNotifier.informSuccess(requester, GameMessage.GAME_STOPPED.content());
    }

    private void refreshParticipantsStatus() {
        gameRepository.find().ifPresent(game ->
                teamService.findAllOnlineParticipants().forEach(player -> {
                    boolean hasSelected = game.hasSelectedPiece(player.getUniqueId());

                    sendSelectionStatus(player, hasSelected);
                }));
    }

    private void sendSelectionStatus(@NonNull Player player, boolean hasSelected) {
        userNotifier.displayActionBar(
                player,
                hasSelected ? GameMessage.SELECTION_WAITING.content() : GameMessage.SELECTION_GUIDE.content()
        );
    }

    private void displayCompletionStatus() {
        teamService.findAllOnlineParticipants().forEach(player ->
                userNotifier.displayActionBar(player, GameMessage.SELECTION_COMPLETED.content()));
    }
}
