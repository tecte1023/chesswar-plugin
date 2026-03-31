package dev.tecte.chessWar.common.identity;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * 프로젝트의 전역 정체성을 관리합니다.
 */
@UtilityClass
public class ProjectIdentity {
    public final String NAMESPACE = "chesswar";
    public final UUID SYSTEM_ID = new UUID(0L, 0L);
}
