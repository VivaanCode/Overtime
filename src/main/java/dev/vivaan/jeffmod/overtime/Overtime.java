package dev.vivaan.jeffmod.overtime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

public final class Overtime extends JavaPlugin {

    private final HashMap<UUID, Integer> playerTime = new HashMap<>();
    private final HashSet<UUID> playersInGame = new HashSet<>();
    private boolean gameRunning = false;
    private Scoreboard scoreboard;
    private Objective objective;

    @Override
    public void onEnable() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("overtime", "dummy", ChatColor.GOLD + "OVERTIME");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        getCommand("start").setExecutor(this);
        getCommand("reset").setExecutor(this);

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PotionListener(this), this);
        //getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getLogger().info("Overtime running!");
        new BukkitRunnable() {
            @Override
            public void run() {
                    if (!gameRunning) {
                        return;
                    }

                    for (UUID id : new HashSet<>(playersInGame)) {
                        Player player = Bukkit.getPlayer(id);
                        if (player == null || !player.isOnline()) {
                            removePlayer(id);
                            if (!player.isOp()) {
                                player.setGameMode(GameMode.SPECTATOR);
                            }
                            continue;
                        }

                    if (player.getGameMode() != GameMode.SURVIVAL) {
                        removePlayer(id);
                        player.setGlowing(false);
                        player.sendMessage(ChatColor.RED + "You left Survival mode and have been removed from the game.");
                        continue;
                    }

                    int secondsLeft = getTime(id) - 1;
                    setTime(id, secondsLeft);

                    if (secondsLeft <= 0) {
                        player.setHealth(Math.max(0, player.getHealth() - 1)); // -0.5 heart every second in negatives, + wither effect
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
                    }

                    updateTimeInterface(player, secondsLeft);

                    if (playersInGame.size() == 1) {
                        UUID winnerId = playersInGame.iterator().next();
                        Player winner = Bukkit.getPlayer(winnerId);
                        if (winner == null) return;

                        String winnerName = winner.getName();
                        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + winnerName + " IS THE WINNER!");

                        for (Player player2 : Bukkit.getOnlinePlayers()) {
                            player2.sendTitle(ChatColor.GOLD + winnerName, "is the winner!", 10, 70, 20);
                            player2.setGlowing(false);
                        }

                        gameRunning = false;
                        playersInGame.clear();
                    }
                }
                updateLeaderboard();
            }
        }.runTaskTimer(this, 0L, 20L); // 0 delay, every 20 ticks / 1 second
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()){
            return true;
        }

        if (label.equalsIgnoreCase("start")) {
            long count = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL).count();

            if (count < 2) {
                sender.sendMessage(ChatColor.RED + "You need at least 2 players in Survival to start.");
                return true;
            }

            gameRunning = true;
            playersInGame.clear();
            playerTime.clear();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    playersInGame.add(player.getUniqueId());
                    playerTime.put(player.getUniqueId(), 300);
                }
            }

            Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "THE CLOCK HAS STARTED. YOU HAVE 2 MINUTES TO GATHER RESOURCES BEFORE PVP IS ENABLED.");
            Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.ITALIC + "If you leave the server, you will be removed from the game.");
            Bukkit.broadcastMessage(ChatColor.GREEN + "How to play: Deal damage and kill players to gain more time. Whoever lives the longest wins.");
            return true;
        }

        if (label.equalsIgnoreCase("reset")) {
            gameRunning = false;
            playerTime.clear();
            playersInGame.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setGlowing(false);
            }

            Bukkit.broadcastMessage(ChatColor.GREEN + "The game has been reset.");
            return true;
        }

        if (label.equalsIgnoreCase("givepotion")) {
            if (!(sender instanceof Player player)) return true;

            ItemStack potion = new ItemStack(org.bukkit.Material.POTION);
            org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) potion.getItemMeta();

            meta.setDisplayName(ChatColor.GREEN + "Potion of Time");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Adds 1 minute of time");
            lore.add(ChatColor.RED + "Capped at 5 minutes!");
            meta.setLore(lore);

            meta.setColor(org.bukkit.Color.LIME);
            potion.setItemMeta(meta);

            player.getInventory().addItem(potion);
            player.sendMessage(ChatColor.GREEN + "You received a Potion of Time.");
            return true;
        }

    return false;
    }

    private void updateLeaderboard() {
        List<UUID> players = new ArrayList<>(playersInGame);

        players.sort((a, b) -> playerTime.getOrDefault(b, 0) - playerTime.getOrDefault(a, 0));

        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        for (int i = 0; i < Math.min(5, players.size()); i++) {
            UUID id = players.get(i);
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                continue;
            }
            if (i == 0) {
                player.setGlowing(true);
            } else {
                player.setGlowing(false);
            }

            int time = getTime(id);
            int absSeconds = Math.abs(time);
            int mins = absSeconds / 60;
            int secs = absSeconds % 60;

            String prefix;

            if (time < 0) {
                prefix = "-";
            } else {
                prefix = "";
            }

            String timeFormatted = prefix + mins + "m " + secs + "s";
            String name = player.getName();

            objective.getScore(ChatColor.GREEN + name + ": " + ChatColor.YELLOW + timeFormatted).setScore(5 - i);
            
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }

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

    public void removePlayer(UUID playerId) {
        playersInGame.remove(playerId);
        playerTime.remove(playerId);
        Player p = Bukkit.getPlayer(playerId);
        if (p != null) p.setGlowing(false);
    }

    public boolean isInGame(UUID playerId) {
        return playersInGame.contains(playerId);
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    @Override
    public void onDisable() {
        getLogger().info("Overtime shutting down!");
    }
}
