package dev.tecte.chesswar.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Messages {
    private static final Component PREFIX = Component.text("[ChessWar] ", NamedTextColor.GRAY);

    public static void reply(final Audience recipient, final Component message) {
        recipient.sendMessage(PREFIX.append(message));
    }
}
