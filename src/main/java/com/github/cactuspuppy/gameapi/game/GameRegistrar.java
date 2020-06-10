package com.github.cactuspuppy.gameapi.game;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameRegistrar {
    private static final Map<String, Game> gamesMap = new HashMap<>();

    /**
     * Register game under its self-supplied name.
     * Will not replace existing game if there is a name collision
     * @param game Game type to register
     * @return Whether the game was successfully registered
     */
    public static boolean registerGame(Game game) {
        return gamesMap.putIfAbsent(game.name(), game) == null;
    }

    /**
     * Return the game instance registered under the supplied name.
     * @param name Name of the game to return
     * @return Game instance if a game instance with that name exists, null otherwise.
     */
    @Nullable
    public static Game getGame(String name) {
        return gamesMap.get(name);
    }

    /**
     * Remove all registered games from the registrar.
     */
    public static void clearGames() {
        gamesMap.clear();
    }
}
