package dev.tecte.chessWar.infrastructure.persistence;

import java.util.Map;

public interface YmlMapper<K, V> {
    Map<String, Object> toMap(V entity);

    V fromMap(K key, Map<String, Object> map);
}
