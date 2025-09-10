package dev.tecte.chessWar.infrastructure.persistence.exception;

/**
 * YML 데이터 매핑 과정에서 발생하는 예외를 나타냅니다.
 * 데이터 파일의 값이 없거나, 타입이 유효하지 않은 경우 발생합니다.
 */
public class YmlMappingException extends RuntimeException {
    public YmlMappingException(String message) {
        super(message);
    }
}
