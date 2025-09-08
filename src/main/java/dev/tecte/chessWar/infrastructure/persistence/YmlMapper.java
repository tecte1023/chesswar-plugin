package dev.tecte.chessWar.infrastructure.persistence;

import java.util.Map;

/**
 * 도메인 엔티티와 YML 파일에 저장될 Map 객체 간의 변환을 정의하는 인터페이스입니다.
 *
 * @param <K> 엔티티의 키 타입
 * @param <V> 엔티티의 타입
 */
public interface YmlMapper<K, V> {
    /**
     * 엔티티 객체를 YML에 저장 가능한 Map 형태로 변환합니다.
     *
     * @param entity 변환할 엔티티
     * @return YML에 저장될 Map 객체
     */
    Map<String, Object> toMap(V entity);

    /**
     * YML에서 읽어온 Map 객체를 엔티티 객체로 변환합니다.
     *
     * @param key 변환할 엔티티의 키
     * @param map YML에서 읽어온 Map 객체
     * @return 변환된 엔티티 객체
     */
    V fromMap(K key, Map<String, Object> map);
}
