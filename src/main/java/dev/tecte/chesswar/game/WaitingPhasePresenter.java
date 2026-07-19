package dev.tecte.chesswar.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class WaitingPhasePresenter {
    private static final Consumer<TextDisplay> TEXT_SETUP = entity -> {
        entity.text(Component.text("게임 시작", NamedTextColor.GOLD, TextDecoration.BOLD));
        entity.setBillboard(Billboard.CENTER);
        entity.setInvulnerable(true);
        entity.setPersistent(false);
    };
    private static final Consumer<Interaction> INTERACTION_SETUP = entity -> {
        entity.setInteractionWidth(0.8f);
        entity.setInteractionHeight(0.3f);
        entity.setInvulnerable(true);
        entity.setPersistent(false);
    };

    private void removeEntity(@NotNull final UUID entityId) {
        final Entity entity = Bukkit.getEntity(entityId);

        if (entity == null) {
            return;
        }

        entity.remove();
    }

    public void hideStartTrigger(@NotNull final UUID interactionId, @NotNull final UUID textId) {
        removeEntity(interactionId);
        removeEntity(textId);
    }

    @NotNull
    public StartTriggerIds showStartTrigger(@NotNull final Location location) {
        final World world = Objects.requireNonNull(
                location.getWorld(),
                "StartTrigger: Location.getWorld() is null"
        );
        final Location eyeLocation = location.clone().add(0, 0.5, 0);
        final TextDisplay textDisplay = world.spawn(eyeLocation, TextDisplay.class, TEXT_SETUP);
        final Interaction interaction = world.spawn(eyeLocation, Interaction.class, INTERACTION_SETUP);

        return new StartTriggerIds(interaction.getUniqueId(), textDisplay.getUniqueId());
    }

    public void showGameStartFeedback(@NotNull final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public void showGameStopFeedback(@NotNull final Player player) {
        final var message = Component.text("관리자에 의해 게임이 강제 중단되었습니다.", NamedTextColor.RED);

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}
