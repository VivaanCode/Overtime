package dev.vivaan.jeffmod.overtime;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionListener implements Listener {
    private final Overtime plugin;

    public PotionListener(Overtime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.POTION && item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().contains("Potion of Time")) {

            Player player = event.getPlayer();

            if (!plugin.isInGame(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You aren't in the game!");
                return;
            }

            int currentTime = plugin.getTime(player.getUniqueId());

            int newTime = Math.min(currentTime + 60, 300);
            plugin.setTime(player.getUniqueId(), newTime);

            player.sendMessage(ChatColor.GREEN + "Time restored! You are now at " + (newTime / 60) + "m " + (newTime % 60) + "s.");
        }
    }
}