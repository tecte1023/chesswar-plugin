package dev.tecte.chessWar.infrastructure.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.tecte.chessWar.infrastructure.file.YmlFileManager;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 영속성 계층에서 공통으로 사용되는 인프라 서비스들을 설정하고 제공하는 Guice 모듈입니다.
 * 비동기 파일 I/O의 안전성과 순차적 처리를 보장하기 위한 실행 환경을 구성합니다.
 */
@SuppressWarnings("unused")
public class PersistenceModule extends AbstractModule {
    /**
     * 사용자 데이터 파일(data.yml)을 관리하는 {@link YmlFileManager}의 싱글턴 인스턴스를 생성하여 DI 컨테이너에 제공합니다.
     *
     * @param plugin 메인 플러그인 인스턴스
     * @return {@link YmlFileManager}의 싱글턴 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public YmlFileManager provideDataFileManager(@NonNull JavaPlugin plugin) {
        return new YmlFileManager(plugin, "data.yml");
    }

    /**
     * 영속성 작업을 처리할 전용 단일 스레드 실행기를 제공합니다.
     * 파일 쓰기 작업의 순서를 보장하고, 멀티스레드 환경에서의 데이터 경합을 방지합니다.
     *
     * @return 영속성 전용 {@link ExecutorService}의 싱글턴 인스턴스
     */
    @NonNull
    @Provides
    @Singleton
    public ExecutorService providePersistenceExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            var thread = new Thread(r, "ChessWar-Persistence-Thread");

            // 안전장치: Graceful Shutdown 실패 시 프로세스가 종료되지 않는 것을 방지하기 위해 데몬으로 설정
            thread.setDaemon(true);

            return thread;
        });
    }
}
