package dev.tecte.chessWar.infrastructure.command;

import dev.tecte.chessWar.common.identity.ProjectIdentity;
import lombok.experimental.UtilityClass;

/**
 * 명령어 라우팅 정보를 관리합니다.
 */
@UtilityClass
public class CommandRouting {
    public final String ROOT_ALIAS = ProjectIdentity.NAMESPACE + "|cw";
}
