package dev.tecte.chessWar.game.infrastructure.bootstrap;

import co.aikar.commands.BaseCommand;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.tecte.chessWar.game.infrastructure.command.GameCommand;

/**
 * 게임 도메인의 의존성 주입 설정을 담당하는 Guice 모듈입니다.
 * <p>
 * 이 모듈은 게임 시작, 종료 등 게임 플레이와 관련된 명령어({@link GameCommand})를 플러그인의 전체 명령어 시스템에 등록하는 역할을 합니다.
 */
public class GameModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), BaseCommand.class).addBinding().to(GameCommand.class);
    }
}
