package com.github.cactuspuppy.gameapi.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GAPIVersion implements GAPICommand {

    @Override
    public @NotNull String name() {
        return "version";
    }

    @Override
    public boolean hasPermission(CommandSender sender, String label, String[] args) {
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(String.format("%sGameAPI %sv1.0", ChatColor.YELLOW, ChatColor.GOLD));
        return true;
    }
}
