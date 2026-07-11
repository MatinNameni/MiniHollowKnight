package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.Input;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registry of every cheat code in the game.
 */
public final class CheatCodeRegistry {

    /** Context passed to each cheat so it can reach the game state it needs. */
    public static final class Context {
        public final Knight knight;
        public final GameData gameData;
        public final TiledGameMap gameMap;
        public final List<Enemy> enemies;
        public final Runnable requestBossTeleport;

        public Context(Knight knight, GameData gameData, TiledGameMap gameMap,
                       List<Enemy> enemies, Runnable requestBossTeleport) {
            this.knight = knight;
            this.gameData = gameData;
            this.gameMap = gameMap;
            this.enemies = enemies;
            this.requestBossTeleport = requestBossTeleport;
        }
    }

    /** A single cheat code definition. */
    public static final class CheatCode {
        public final String name;
        public final int keyCode; // Ctrl + keyCode
        private final Consumer<Context> activator;

        public CheatCode(String name, int keyCode, Consumer<Context> activator) {
            this.name = name;
            this.keyCode = keyCode;
            this.activator = activator;
        }

        /** Runs the cheat's effect. */
        public void activate(Context ctx) {
            activator.accept(ctx);
        }
    }

    private final List<CheatCode> cheats = new ArrayList<>();

    public CheatCodeRegistry() {
        registerDefaults();
    }

    /** Registers every cheat required by the spec. */
    private void registerDefaults() {
        // 1. Boss Arena Teleport — Ctrl+B
        add("Boss Arena Teleport",
            Input.Keys.B,
            ctx -> ctx.requestBossTeleport.run());

        // 2. Noclip / Spectator Mode — Ctrl+N (toggle)
        add("Noclip",
            Input.Keys.N,
            ctx -> {
                boolean on = !ctx.knight.isNoclip();
                ctx.knight.setNoclip(on);
            });

        // 3. Emergency Heal — Ctrl+H
        add("Emergency Heal",
            Input.Keys.H,
            ctx -> {
                ctx.knight.heal();
                EventBus.getInstance().publish(GameEvent.PLAYER_HEALED);
            });

        // 4. Refill Soul Vessel — Ctrl+S
        add("Refill Soul Vessel",
            Input.Keys.Q,
            ctx -> {
                ctx.knight.refillSoul();
                EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_GAINED, GameData.MAX_SOUL);
            });

        // 5. God Mode — Ctrl+G (toggle)
        add("God Mode",
            Input.Keys.G,
            ctx -> {
                boolean on = !ctx.knight.isGodMode();
                ctx.knight.setGodMode(on);
            });

        // 6. Insta-Kill (bonus) — Ctrl+K
        add("Insta-Kill",
            Input.Keys.K,
            ctx -> {
                for (Enemy enemy : ctx.enemies) {
                    if (!enemy.isDead()) {
                        enemy.takeDamage(Float.MAX_VALUE, Direction.LEFT, 1f);
                    }
                }
            });
    }

    /** Adds a cheat to the registry. */
    public void add(String name, int keyCode, Consumer<Context> activator) {
        cheats.add(new CheatCode(name, keyCode, activator));
    }

    /** @return every registered cheat. */
    public List<CheatCode> all() {
        return cheats;
    }

    /**
     * Finds the cheat bound to {@code keyCode}, or {@code null} if none.
     */
    public CheatCode forKey(int keyCode) {
        for (CheatCode cheat : cheats) {
            if (cheat.keyCode == keyCode) return cheat;
        }
        return null;
    }
}
