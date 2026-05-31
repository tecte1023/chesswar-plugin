package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GameResetEvent;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnvironmentManager implements Listener {
    private static final Map<GameRule<?>, Object> ENVIRONMENT_SPEC = new LinkedHashMap<>();

    static {
        ENVIRONMENT_SPEC.put(GameRule.NATURAL_REGENERATION, false);
        ENVIRONMENT_SPEC.put(GameRule.DO_DAYLIGHT_CYCLE, false);
        ENVIRONMENT_SPEC.put(GameRule.DO_WEATHER_CYCLE, false);
        ENVIRONMENT_SPEC.put(GameRule.DO_MOB_SPAWNING, false);
        ENVIRONMENT_SPEC.put(GameRule.DO_TRADER_SPAWNING, false);
        ENVIRONMENT_SPEC.put(GameRule.DO_PATROL_SPAWNING, false);
        ENVIRONMENT_SPEC.put(GameRule.MOB_GRIEFING, false);
        ENVIRONMENT_SPEC.put(GameRule.KEEP_INVENTORY, true);
        ENVIRONMENT_SPEC.put(GameRule.DO_FIRE_TICK, false);
        ENVIRONMENT_SPEC.put(GameRule.DO_TILE_DROPS, false);
        ENVIRONMENT_SPEC.put(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        ENVIRONMENT_SPEC.put(GameRule.SPAWN_RADIUS, 0);
    }

    private final ChessWar plugin;

    public EnvironmentManager(final ChessWar plugin) {
        this.plugin = plugin;
    }

    public void configure(final World world) {
        if (world == null) {
            return;
        }

        applyGameRules(world);
        applyDifficulty(world);
        applyPvpSettings(world);
    }

    @EventHandler
    public void onGameReset(final GameResetEvent event) {
        if (plugin.boardManager().hasBoard()) {
            configure(plugin.boardManager().currentBoard().origin().getWorld());
        }
    }

    private void applyGameRules(final World world) {
        for (final Map.Entry<GameRule<?>, Object> entry : ENVIRONMENT_SPEC.entrySet()) {
            applyRule(world, entry.getKey(), entry.getValue());
        }
    }

    private void applyDifficulty(final World world) {
        if (world.getDifficulty() != Difficulty.NORMAL) {
            world.setDifficulty(Difficulty.NORMAL);
        }
    }

    private void applyPvpSettings(final World world) {
        if (!world.getPVP()) {
            world.setPVP(true);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void applyRule(final World world, final GameRule<T> rule, final Object value) {
        final T typedValue = (T) value;
        final T currentValue = world.getGameRuleValue(rule);

        if (currentValue != null && !currentValue.equals(typedValue)) {
            world.setGameRule(rule, typedValue);
        }
    }
}
