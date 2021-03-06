package com.github.cactuspuppy.gameapi.utils;

import com.github.cactuspuppy.gameapi.GameAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@AllArgsConstructor
public class FileConfig extends Config {
    @Getter
    private File configFile;

    public void save() {
        try {
            save(configFile);
        } catch (IOException e) {
            GameAPI.getPlugin().getLogger().log(Level.SEVERE, "Problem saving file", e);
        }
    }

    public void reload() {
        try {
            load(configFile);
        } catch ( IOException e) {
            GameAPI.getPlugin().getLogger().log(Level.SEVERE, "Problem reloading config from file", e);
        }
    }

    public void set(String key, String value) {
        put(key, value);
    }
}
