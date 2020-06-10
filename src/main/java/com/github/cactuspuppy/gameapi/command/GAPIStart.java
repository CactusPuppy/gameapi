package com.github.cactuspuppy.gameapi.command;

import com.github.cactuspuppy.gameapi.GameAPI;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class GAPIStart implements GAPICommand {
    private BukkitTask startCountdown = null;

    @Override
    public @NotNull String name() {
        return "start";
    }

    @Override
    public boolean hasPermission(CommandSender sender, String label, String[] args) {
        return sender.hasPermission("gapi.cmd.start");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        int countdown;
        try {
            countdown = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + String.format("%s is not a valid integer", args[0]));
            return true;
        }
        synchronized (this) {
            if (startCountdown != null) {
                startCountdown.cancel();
            }
            startCountdown = Bukkit.getScheduler().runTaskTimer(GameAPI.getPlugin(), new StartCountdown(countdown), 0, 5);
        }
        return true;
    }

    private static class StartCountdown implements Runnable {
        private long timeToStart;

        public StartCountdown(int secondsToStart) {
            timeToStart = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(secondsToStart);
        }

        @Override
        public void run() {
            
        }
    }
}
