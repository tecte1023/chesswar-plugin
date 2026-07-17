package dev.tecte.chesswar.game;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@AllArgsConstructor
public final class StartTriggerUIComponent {
    @Nullable
    private Location startTriggerLocation;

    @Nullable
    private StartTriggerIds activeTriggerIds;
}
