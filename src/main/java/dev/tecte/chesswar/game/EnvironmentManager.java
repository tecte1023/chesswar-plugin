package dev.tecte.chesswar.game;

import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

public class EnvironmentManager {

    public void optimize(World world) {
        setGameRuleIfDifferent(world, GameRule.NATURAL_REGENERATION, false);
        setGameRuleIfDifferent(world, GameRule.DO_DAYLIGHT_CYCLE, false);
        setGameRuleIfDifferent(world, GameRule.DO_WEATHER_CYCLE, false);
        setGameRuleIfDifferent(world, GameRule.DO_MOB_SPAWNING, false);
        setGameRuleIfDifferent(world, GameRule.DO_TRADER_SPAWNING, false);
        setGameRuleIfDifferent(world, GameRule.DO_PATROL_SPAWNING, false);
        setGameRuleIfDifferent(world, GameRule.MOB_GRIEFING, false);
        setGameRuleIfDifferent(world, GameRule.KEEP_INVENTORY, true);
        setGameRuleIfDifferent(world, GameRule.DO_FIRE_TICK, false);
        setGameRuleIfDifferent(world, GameRule.DO_TILE_DROPS, false);
        setGameRuleIfDifferent(world, GameRule.ANNOUNCE_ADVANCEMENTS, false);
        setGameRuleIfDifferent(world, GameRule.SPAWN_RADIUS, 0);

        if (world.getDifficulty() != Difficulty.NORMAL) {
            world.setDifficulty(Difficulty.NORMAL);
        }

        if (!world.getPVP()) {
            world.setPVP(true);
        }
    }

    private <T> void setGameRuleIfDifferent(World world, GameRule<T> rule, T value) {
        T currentValue = world.getGameRuleValue(rule);

        if (currentValue != null && !currentValue.equals(value)) {
            world.setGameRule(rule, value);
        }
    }
}
