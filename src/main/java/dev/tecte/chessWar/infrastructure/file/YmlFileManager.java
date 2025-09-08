package dev.tecte.chessWar.infrastructure.file;

import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceInitializationException;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceWriteException;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * YML 파일의 생성, 로드, 저장을 관리하는 클래스입니다.
 * 플러그인의 데이터 폴더 내에서 특정 YML 파일을 안전하게 다루는 기본 I/O 작업을 캡슐화합니다.
 */
public class YmlFileManager {
    private final File file;
    @Getter
    private final FileConfiguration config;

    /**
     * {@link YmlFileManager}를 생성하고, 대상 파일이 없을 경우 리소스에서 복사하거나 파일을 생성합니다.
     *
     * @param plugin   JavaPlugin 인스턴스
     * @param fileName 데이터 폴더 내의 YML 파일 이름
     * @throws PersistenceInitializationException 파일 또는 디렉토리 생성에 실패할 경우
     */
    @Inject
    public YmlFileManager(@NonNull JavaPlugin plugin, @NonNull String fileName) {
        file = new File(plugin.getDataFolder(), fileName);

        File parentDir = file.getParentFile();

        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new PersistenceInitializationException("Failed to create parent directories for: " + file.getPath());
        }

        if (!file.exists()) {
            /*
             * 파일 생성 전략:
             * 1. 가장 좋은 방법으로 JAR에 내장된 기본 템플릿 파일 복사를 시도
             * 2. 템플릿이 없는 경우 IllegalArgumentException 던짐
             * 3. 최후의 보루로 비어있는 새 파일을 생성
             */
            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                try {
                    if (!file.createNewFile()) {
                        plugin.getLogger().warning("File was created by another process between checks: " + fileName);
                    }
                } catch (IOException ex) {
                    throw new PersistenceInitializationException("Could not create new file: " + fileName, ex);
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 파일의 특정 경로에 값을 설정합니다. (메모리상 변경)
     *
     * @param path  값을 설정할 경로
     * @param value 설정할 값
     */
    public synchronized void set(@NonNull String path, Object value) {
        config.set(path, value);
    }

    /**
     * 현재 메모리에 있는 설정 값을 파일에 동기적으로 저장합니다.
     *
     * @throws PersistenceWriteException 파일 저장에 실패할 경우
     */
    public synchronized void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new PersistenceWriteException("Could not save to " + file.getName(), e);
        }
    }
}
