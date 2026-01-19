package dev.tecte.chessWar.infrastructure.file;

import dev.tecte.chessWar.infrastructure.exception.ConfigurationException;
import dev.tecte.chessWar.infrastructure.persistence.exception.PersistenceException;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.google.common.io.Files.touch;

/**
 * YML 파일의 생성, 로드, 저장을 관리하는 클래스입니다.
 * 플러그인의 데이터 폴더 내에서 특정 YML 파일을 안전하게 다루는 기본 I/O 작업을 캡슐화합니다.
 */
public class YmlFileManager {
    private final File file;
    @Getter
    @Accessors(fluent = true)
    private final FileConfiguration config;

    /**
     * 지정된 파일 이름으로 YML 파일 관리자를 생성하고 설정을 로드합니다.
     * 파일이 없으면 기본 템플릿을 복사하거나 빈 파일을 생성합니다.
     *
     * @param plugin   JavaPlugin 인스턴스
     * @param fileName 관리할 YML 파일 이름 (확장자 포함)
     * @throws ConfigurationException 파일 생성이나 로드에 실패했을 경우
     */
    @Inject
    public YmlFileManager(@NonNull JavaPlugin plugin, @NonNull String fileName) {
        this.file = new File(plugin.getDataFolder(), fileName);
        this.config = loadConfig(plugin, fileName);
    }

    /**
     * 파일의 특정 경로에 값을 설정합니다. (메모리상 변경)
     *
     * @param path  값을 설정할 경로
     * @param value 설정할 값
     */
    @Synchronized
    public void set(@NonNull String path, @Nullable Object value) {
        config.set(path, value);
    }

    /**
     * 현재 메모리에 있는 설정 값을 파일에 저장합니다.
     * 임시 파일에 먼저 저장한 후 원본 파일로 이동하여, 저장 중 오류 발생 시 파일 손상을 방지합니다.
     *
     * @throws PersistenceException 파일 저장에 실패할 경우
     */
    @Synchronized
    public void save() {
        File tmpFile = new File(file.getPath() + ".tmp");

        try {
            config.save(tmpFile);
            Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // 임시 파일 삭제 실패는 무시 (다음 저장 시 덮어쓰므로 무방함)
            try {
                Files.deleteIfExists(tmpFile.toPath());
            } catch (IOException ignored) {
            }

            throw PersistenceException.forSaveFailure(file.getName(), e);
        }
    }

    @NonNull
    private FileConfiguration loadConfig(@NonNull JavaPlugin plugin, @NonNull String fileName) {
        try {
            Files.createDirectories(file.getParentFile().toPath());
        } catch (IOException e) {
            throw ConfigurationException.forStorageSetupFailure(file.getPath(), e);
        }

        if (!file.exists()) {
            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                try {
                    touch(file);
                } catch (IOException ex) {
                    throw ConfigurationException.forCreationFailure(fileName, ex);
                }
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }
}
