package com.github.cactuspuppy.gameapi;

import com.github.cactuspuppy.gameapi.game.Game;
import com.github.cactuspuppy.gameapi.game.GameRegistrar;
import com.github.cactuspuppy.gameapi.utils.FileConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameAPI extends JavaPlugin {
    @Getter private static final String saveFile = "gameSave.dat";
    @Getter private static File saveLocation;
    @Getter private static JavaPlugin plugin;
    @Getter private static FileConfig configFile;
    /**
     * What the current lobby state of the server is.
     */
    @Getter @Setter(AccessLevel.PACKAGE)
    private static LobbyState lobbyState = LobbyState.LOBBY;
    /**
     * Currently loaded game
     */
    @Getter @Setter(AccessLevel.PROTECTED)
    private static Game game = null;

    private final static Logger tempLogger = Logger.getLogger("FYFStartupLogger");

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        plugin = this;
        getLogger().info(ChatColor.GREEN + "Loading GameAPI...");
        if (!initBase()) {
            getLogger().severe("Failure to initiate base GameAPI, disabling...");
            plugin = null;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //TODO: load game
        String gameName = configFile.getOrDefault("game", null);
        if (gameName != null) {
            Game gameImpl = GameRegistrar.getGame(gameName);
            if (gameImpl == null) {
                getLogger().warning(String.format("Game \"%s\" saved in config, but has no registered implementation.", gameName));
            } else if (!loadGame(gameName)){
                getLogger().warning(String.format("Game \"%s\" failed to load", gameName));
            }
        }
        getLogger().info(ChatColor.AQUA + "GameAPI startup complete.");
        getLogger().info(ChatColor.GRAY + "Took " + ChatColor.WHITE + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.YELLOW + "GameAPI shutting down...");
        saveGame();
        getLogger().info(ChatColor.GREEN + "Shutdown complete. Goodbye.");
    }

    @Override
    public void saveDefaultConfig() {
        configFile.save();
    }

    /**
     * Attempts to load the specified game, replacing
     * the currently loaded game.
     * Will fail if the game is not in lobby.
     * @param gameName Registered name of {@link Game} under {@link GameRegistrar}
     * @return Whether the game was successfully loaded or not.
     */
    public static boolean loadGame(String gameName) {
        gameLobbyFSM(LobbyEvents.LOAD, gameName);
        return getGame() != null && getGame().name().equals(gameName);
    }

    /**
     * Starts the currently loaded game. Has no effect if the game is not in lobby.
     * @return Whether the game was successfully started or not. Will return true
     * and have no effect if the game is running.
     */
    public static boolean startGame() {
        gameLobbyFSM(LobbyEvents.START);
        return getLobbyState() == LobbyState.GAME;
    }

    /**
     * Brings a currently running game back to lobby state. Has no effect if not in game.
     * Will automatically succeed if there is no loaded game.
     * @return Whether game was successfully brought back to lobby. Will return false if
     * already in lobby.
     */
    public static boolean resetGame() {
        gameLobbyFSM(LobbyEvents.RESET);
        return getLobbyState() == LobbyState.LOBBY;
    }

    /**
     * Save the currently loaded game if it is running.
     */
    public static void saveGame() {
        gameLobbyFSM(LobbyEvents.SAVE);
    }

    public static Logger getPluginLogger() {
        return plugin == null ? tempLogger : plugin.getLogger();
    }

    /*
    HELPER FUNCTIONS
     */
    private boolean initBase() {
        saveLocation = new File(getDataFolder(), saveFile);
        if (!initConfig()) {
            return false;
        }
        PluginCommand gapi = getCommand("gapi");
        if (gapi == null) {
            GameAPI.getPluginLogger().severe("No gapi command?!");
            return false;
        }
        //TODO: Set command executor
        //TODO: Register listeners
        return true;
    }

    private boolean initConfig() {
        File dataFolder = getDataFolder();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            GameAPI.getPluginLogger().severe("Could not find or create data folder.");
            return false;
        }
        File config = new File(getDataFolder(), "config.yml");
        //Create config if it does not exist
        if (!config.isFile()) {
            InputStream inputStream = getResource("config.yml");
            if (inputStream == null) {
                GameAPI.getPluginLogger().severe("No packaged config.yml?!");
                return false;
            }
            try {
                FileUtils.copyToFile(inputStream, config);
            } catch (IOException e) {
                GameAPI.getPluginLogger().severe("Error while creating new config");
                return false;
            }
        }
        //Set config
        GameAPI.configFile = new FileConfig(config);
        return true;
    }

    private static void _loadGame(String gameName) {
        Game game = GameRegistrar.getGame(gameName);
        if (game == null) {
            return;
        }

        if (GameAPI.getGame() != null) {
            GameAPI.getGame().cleanup();
        }
        GameAPI.setGame(null);
        boolean success = game.load();
        if (!success) {
            GameAPI.getPluginLogger().warning(String.format("Unable to load game \"%s\"", game.name()));
            return;
        }
        GameAPI.setGame(game);
    }

    private static void _saveGame() {
        if (!Files.isWritable(saveLocation.toPath())) {
            GameAPI.getPluginLogger().severe("Unable to write game save data to disk");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveLocation))) {
            // Write metadata
            writer.write(game.name());
            writer.newLine();
            writer.write(getLobbyState().toString());
            writer.newLine();

            game.save(writer);
        } catch (IOException e) {
            GameAPI.getPluginLogger().log(Level.WARNING, "Problem saving current game to disk", e);
        }
    }

    /**
     * Transitions lobby to different states via FSM.
     * @param event Event type to handle
     */
    private static void gameLobbyFSM(LobbyEvents event, Object... args) {
        LobbyState newState = getLobbyState();
        switch (getLobbyState()) {
            case LOBBY:
                switch (event) {
                    case START:
                        if (getGame() != null) {
                            getGame().start();
                            newState = LobbyState.GAME;
                        }
                        break;
                    case LOAD:
                        String game = (String) args[0];
                        _loadGame(game);
                        break;
                }
                break;
            case GAME:
                if (getGame() == null) {
                    GameAPI.getPluginLogger().warning("Lobby state was GAME with no loaded game?! Returning to lobby...");
                    newState = LobbyState.LOBBY;
                    break;
                }
                switch (event) {
                    case RESET:
                        getGame().reset();
                        newState = LobbyState.LOBBY;
                        break;
                    case SAVE:
                        _saveGame();
                        break;
                }
                break;
        }
        setLobbyState(newState);
    }

    private enum LobbyEvents {
        START,
        RESET,
        LOAD,
        SAVE
    }

    /*
    MOCKBUKKIT CONSTRUCTS
     */
    public GameAPI() { super(); }

    protected GameAPI(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }
}
