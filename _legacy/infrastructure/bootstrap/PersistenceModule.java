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
 * 영속성 관련 인프라 서비스를 설정하는 Guice 모듈입니다.
 */
@SuppressWarnings("unused")
public class PersistenceModule extends AbstractModule {
    /**
     * 데이터 파일 관리자를 생성하여 제공합니다.
     *
     * @param plugin 메인 플러그인
     * @return 데이터 파일 관리자
     */
    @NonNull
    @Provides
    @Singleton
    public YmlFileManager provideDataFileManager(@NonNull JavaPlugin plugin) {
        return new YmlFileManager(plugin, "data.yml");
    }

    /**
     * 영속성 작업을 처리하는 전용 스레드 실행기를 제공합니다.
     * 파일 쓰기 작업의 순서를 보장하고, 멀티스레드 환경에서의 데이터 경합을 방지합니다.
     *
     * @return 영속성 전용 실행기
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
