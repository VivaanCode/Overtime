package dev.vivaan.jeffmod.overtime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public final class Overtime extends JavaPlugin {

    private final HashMap<UUID, Integer> playerTime = new HashMap<>();
    private boolean gameRunning = false;

    @Override
    public void onEnable() {
        getCommand("start").setExecutor(this);
        getCommand("reset").setExecutor(this);

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        getLogger().info("Overtime running!");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameRunning) {
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID id = player.getUniqueId();

                    int secondsLeft = getTime(id) - 1;
                    setTime(id, secondsLeft);

                    if (secondsLeft <= 0) {
                        player.setHealth(player.getHealth() - 1); // -0.5 heart every second in negatives, + wither effect
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
                    }

                    updateTimeInterface(player, secondsLeft);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 0 delay, every 20 ticks / 1 second
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()){
            return true;
        }

        if (label.equalsIgnoreCase("start")) {
            gameRunning = true;
            Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "THE CLOCK HAS STARTED. YOU HAVE 2 MINUTES TO GATHER RESOURCES BEFORE PVP IS ENABLED.");
            return true;
        }

        if (label.equalsIgnoreCase("reset")) {
            gameRunning = false;
            playerTime.clear();
            Bukkit.broadcastMessage(ChatColor.GREEN + "The game has been reset.");
            return true;
        }

    return false;
    }

    private void updateTimeInterface(Player player, int seconds) {
        int absSeconds = Math.abs(seconds);

        int mins = absSeconds / 60;
        int secs = absSeconds % 60;

        String prefix;

        if (seconds < 0) {
            prefix = "-";
        } else {
            prefix = "";
        }

        String timeFormatted = prefix + mins + "m " + secs + "s";
        ChatColor decidedChatColor;

        if (mins > 1){
            decidedChatColor = ChatColor.GREEN; // yo i do not care if this is deprecated
        } else {                                // literally no tutorial uses anything else
            decidedChatColor = ChatColor.RED;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(decidedChatColor + timeFormatted + " left"));
    }

    public int getTime(UUID playerId) {
        return playerTime.getOrDefault(playerId, 300);
    }

    public void setTime(UUID playerId, int time) {
        playerTime.put(playerId, time);
    }

    @Override
    public void onDisable() {
        getLogger().info("Overtime shutting down!");
    }
}
