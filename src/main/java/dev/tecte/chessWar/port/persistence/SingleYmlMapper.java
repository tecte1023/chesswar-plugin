package dev.tecte.chessWar.port.persistence;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * YML 파일 데이터와 단일 도메인 객체 간의 변환을 담당하는 매퍼 인터페이스입니다.
 * 이 매퍼는 키가 없는 단일 객체의 영속성을 처리하는 데 사용됩니다.
 *
 * @param <V> 엔티티의 타입
 */
public interface SingleYmlMapper<V> {
    /**
     * 엔티티 객체를 YML에 저장할 수 있는 {@link Map} 형태로 변환합니다.
     *
     * @param entity 변환할 엔티티
     * @return YML 데이터 맵
     */
    @NonNull
    Map<String, Object> toMap(@NonNull V entity);

    /**
     * YML의 {@link ConfigurationSection}에서 엔티티 객체를 복원합니다.
     *
     * @param section YML 데이터 섹션
     * @return 복원된 엔티티 객체
     */
    @NonNull
    V fromSection(@NonNull ConfigurationSection section);
}
