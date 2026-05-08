package dev.tecte.chessWar.infrastructure.exception;

import dev.tecte.chessWar.common.exception.SystemException;
import lombok.NonNull;

/**
 * 태스크 스케줄링 과정에서 발생하는 기술적 예외입니다.
 * <p>
 * 이 예외는 시스템 로그만 기록하며, 사용자에게는 알림을 보내지 않습니다.
 */
public class TaskSchedulingException extends SystemException {
    private TaskSchedulingException(@NonNull String internalMessage, @NonNull Throwable cause) {
        super(internalMessage, cause);
    }

    /**
     * 태스크 취소에 실패했을 때 발생합니다.
     *
     * @param taskId 태스크 ID
     * @param cause  실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static TaskSchedulingException cancelFailed(int taskId, @NonNull Throwable cause) {
        return new TaskSchedulingException("Failed to cancel Bukkit task [ID: %d]".formatted(taskId), cause);
    }
}
