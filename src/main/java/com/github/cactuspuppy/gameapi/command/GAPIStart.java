package com.github.cactuspuppy.gameapi.command;

import com.github.cactuspuppy.gameapi.GameAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class GAPIStart implements GAPICommand {
    private StartCountdown startCountdown = null;

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
        // Cancel countdown
        if (args.length > 0 && args[0].equals("cancel")) {
            cancelStartCountdown();
            sender.sendMessage(ChatColor.YELLOW + "Start countdown cancelled.");
            return true;
        }
        int countdown;
        // Set requested countdown length or use default
        if (args.length == 0) {
            try {
                countdown = Integer.parseInt(GameAPI.getConfigFile().getOrDefault("start.countdown.default-delay", "not an integer"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Configuration error: No default delay. Please supply your own countdown duration.");
                return true;
            }
        } else {
            try {
                countdown = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + String.format("%s is not a valid integer", args[0]));
                return true;
            }
        }

        synchronized (this) {
            if (startCountdown != null) {
                if (args.length > 0) {
                    startCountdown.setSecsToStart(countdown);
                } else {
                    String overrideBehavior = GameAPI.getConfigFile().getOrDefault("start.override-behavior", "").toUpperCase();
                    switch (overrideBehavior) {
                        case "IMMEDIATE-START":
                            startCountdown.setSecsToStart(0);
                            break;
                        case "DEFAULT-DELAY":
                            startCountdown.setSecsToStart(countdown);
                            break;
                        case "CANCEL-COUNTDOWN":
                            cancelStartCountdown();
                            sender.sendMessage(ChatColor.YELLOW + "Start countdown cancelled.");
                            break;
                        case "SHORTEN-COOLDOWN":
                        default:
                            if (startCountdown.getSecondsLeft() >= 10) {
                                startCountdown.setSecsToStart(10);
                            } else if (startCountdown.getSecondsLeft() >= 5) {
                                startCountdown.setSecsToStart(5);
                            }
                            break;
                    }
                }
            } else {
                startCountdown = new StartCountdown(countdown);
                startCountdown.setTask(Bukkit.getScheduler().runTaskTimer(GameAPI.getPlugin(), startCountdown, 0, 5));
            }
        }
        return true;
    }

    private void cancelStartCountdown() {
        startCountdown.task.cancel();
        startCountdown = null;
    }

    private class StartCountdown implements Runnable {
        private long timeToStart;
        private long lastSecond;
        @Getter @Setter
        private BukkitTask task;

        public StartCountdown(int secondsToStart) {
            setSecsToStart(secondsToStart);
        }

        public void setSecsToStart(int secs) {
            timeToStart = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(secs);
            lastSecond = getSecondsLeft() + 1;
        }

        /**
         * Returns the number of seconds left in this countdown, rounded down to the nearest second.
         * Clamps to a minimum of 0 (will not return negative values)
         * @return The number of seconds left (clamped to greater than zero)
         */
        public long getSecondsLeft() {
            long timeTo = timeToStart - System.currentTimeMillis();
            return Math.max((long) Math.ceil(timeTo / 1000D), 0);
        }

        @Override
        public void run() {
            //TODO
            if (lastSecond == getSecondsLeft()) {
                return;
            }
            // Halt countdown when time is up
            if (System.currentTimeMillis() >= timeToStart) {
                //TODO: announce game start
                GameAPI.startGame();
                cancelStartCountdown();
                return;
            }
            long thisSecond = getSecondsLeft();
            if (lastSecond == thisSecond) {
                return;
            }
            //TODO: broadcast title
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                String message = String.format("%1$sGame starts in %2$s%3$d %1$s%4$s...", ChatColor.GREEN, ChatColor.WHITE, thisSecond, thisSecond == 1 ? "second" : "seconds");
                player.sendMessage(message);
            });
            lastSecond = thisSecond;
        }
    }
}
