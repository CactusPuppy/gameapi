package com.github.cactuspuppy.gameapi.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Public-facing interface defining
 */
public interface Game {
    /**
     * Perform necessary actions when game is loaded
     * @return Whether load was successfully completed
     */
    boolean load();

    /**
     * Save game state to passed in file location.
     * The file is guaranteed to not exist and be writable before
     * this function call.
     * Usually called before a server shutdown or restart.
     * A game may choose to ignore this call and simply return true.
     * @param outWriter {@link BufferedWriter} to save game state to
     * @return Whether the save successfully completed
     */
    boolean save(BufferedWriter outWriter);

    /**
     * Load game state from file. The file is guaranteed to
     * exist and be readable before this function call.
     * @param inReader {@link BufferedReader} to read game state from
     * @return Whether the load succeeded or not
     */
    boolean loadSave(BufferedReader inReader);

    /**
     * Perform cleanup actions for this game.
     * This action will only be called from a lobby state.
     */
    void cleanup();

    /**
     * Perform the necessary actions to start the game.
     */
    void start();

    /**
     * Perform the necessary actions to bring the game back to lobby.
     */
    void reset();

    /**
     * @return Name of this game
     */
    String name();
}
