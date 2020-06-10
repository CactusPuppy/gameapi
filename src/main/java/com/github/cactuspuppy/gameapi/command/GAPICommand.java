package com.github.cactuspuppy.gameapi.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface GAPICommand extends CommandExecutor {
    /**
     * Unique identifier for this command. Will be used in {@code /game [name]}
     * @return Unique identifier for this command.
     */
    @NotNull
    String name();

    boolean hasPermission(CommandSender sender, String label, String[] args);
}
