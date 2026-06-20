package dev.tecte.chesswar.piece;

/**
 * 상태 효과의 종류를 나타내는 열거형.
 * <ul>
 *   <li>{@link #BUFF}  – 긍정적 효과 (예: 도약)</li>
 *   <li>{@link #DEBUFF} – 부정적 효과 (예: 기절)</li>
 * </ul>
 */
public enum EffectType {
    BUFF,
    DEBUFF
}
