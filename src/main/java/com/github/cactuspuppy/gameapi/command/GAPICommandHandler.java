package com.github.cactuspuppy.gameapi.command;

import com.github.cactuspuppy.gameapi.GameAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GAPICommandHandler implements CommandExecutor {
    private static final ConcurrentMap<String, GAPICommand> handlerMap = new ConcurrentHashMap<>();
    static {
        registerDefaultGAPICommands();
    }

    private static void registerDefaultGAPICommands() {
        registerGAPICommand(new GAPIVersion());
        registerGAPICommand(new GAPIStart());
    }

    /**
     * Register the provided {@link GAPICommand} command if no exisintg
     * command has the same name
     * @param cmd {@link GAPICommand} to register
     * @return Whether registration succeeded or not
     */
    public static boolean registerGAPICommand(GAPICommand cmd) {
        return handlerMap.putIfAbsent(cmd.name(), cmd) == null;
    }

    /**
     * Get the {@link GAPICommand} command registered under
     * the provided name, or null if there is none.
     * @param name Name of the {@link GAPICommand} to return
     * @return The {@link GAPICommand} requested, or null if none exists.
     */
    public static GAPICommand getGAPICommand(String name) {
        return handlerMap.get(name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        GAPICommand handler = args.length == 0 ?
            getGAPICommand("version") :
            getGAPICommand(args[0]);
        if (handler == null) {
            if (args.length > 0) {
                sender.sendMessage(ChatColor.RED + String.format("Unknown subcommand %s", ChatColor.UNDERLINE + args[0]));
            } else {
                GameAPI.getPluginLogger().severe("No version command handler?");
            }
            return true;
        }
        args = Arrays.copyOfRange(args, 1, args.length);
        if (!handler.hasPermission(sender, label, args)) {
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
            return true;
        }
        return handler.onCommand(sender, command, label, args);
    }
}
