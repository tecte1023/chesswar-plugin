package dev.tecte.chessWar.team.infrastructure.command;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import dev.tecte.chessWar.infrastructure.command.CommandConfigurer;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Team 도메인의 커맨드 설정을 수행합니다.
 * {@link CommandConfigurer} 인터페이스의 구현체로서, Team 관련 커맨드가 필요로 하는
 * 컨텍스트 리졸버(예: {@link TeamColor})나 자동완성 규칙을 {@link PaperCommandManager}에 등록합니다.
 * <p>
 * DDD 관점에서 이 클래스는 도메인 계층과 인프라스트럭처 계층 사이의 어댑터 역할을 수행합니다.
 */
@Singleton
public class TeamCommandConfigurer implements CommandConfigurer {
    /**
     * Team 도메인에 필요한 커맨드 컨텍스트와 자동완성 규칙을 ACF에 등록합니다.
     *
     * @param commandManager 설정을 적용할 ACF 커맨드 매니저
     */
    @Override
    public void configure(@NonNull PaperCommandManager commandManager) {
        commandManager.getCommandContexts().registerContext(TeamColor.class, c -> {
            String value = c.popFirstArg();

            return TeamColor.from(value)
                    .orElseThrow(() -> new InvalidCommandArgument("유효하지 않은 팀입니다: " + value));
        });

        commandManager.getCommandCompletions().registerCompletion("teamcolors", c ->
                Arrays.stream(TeamColor.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()));
    }
}
