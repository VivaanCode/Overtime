package dev.vivaan.jeffmod.overtime;

import org.bukkit.plugin.java.JavaPlugin;

public final class Overtime extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Overtime running!");

    }

    @Override
    public void onDisable() {
        getLogger().info("Overtime shutting down!");
    }
}
