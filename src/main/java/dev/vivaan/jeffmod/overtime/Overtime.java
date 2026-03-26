package dev.vivaan.jeffmod.overtime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public final class Overtime extends JavaPlugin {

    private final HashMap<UUID, Integer> playerTime = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Overtime running!");
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID id = player.getUniqueId();

                    int secondsLeft = playerTime.getOrDefault(id, 300); // 300s = 5m

                    secondsLeft--;
                    playerTime.put(id, secondsLeft);

                    updateTimeInterface(player, secondsLeft);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 0 delay, every 20 ticks / 1 second
    }

    private void updateTimeInterface(Player player, int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        String timeFormatted = mins + "m " + secs + "s";
        ChatColor decidedChatColor;

        if (mins > 1){
            decidedChatColor = ChatColor.GREEN; // yo i do not care if this is deprecated
        } else {                                          // literally no tutorial uses anything else
            decidedChatColor = ChatColor.RED;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(decidedChatColor + timeFormatted + " left"));
    }

    @Override
    public void onDisable() {
        getLogger().info("Overtime shutting down!");
    }
}
