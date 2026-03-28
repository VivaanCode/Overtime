package dev.vivaan.jeffmod.overtime;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatListener implements Listener{

    private final Overtime plugin;
    public CombatListener(Overtime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj) {
            if (proj.getShooter() instanceof Player p) {
                attacker = p;
            }
        }

        if (attacker == null) return;

        if (!plugin.isGameRunning()) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.isInGame(victim.getUniqueId()) || !plugin.isInGame(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (plugin.isGracePeriod()) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "PVP is disabled for the first 2 minutes!");
            return;
        }

        double damage = event.getDamage();
        int timeToTake = (int) (damage / 2); // 1 heart = 1 second

        if (timeToTake < 1) return;

        int victimTime = plugin.getTime(victim.getUniqueId());
        int attackerTime = plugin.getTime(attacker.getUniqueId());

        plugin.setTime(victim.getUniqueId(), victimTime - timeToTake);
        plugin.setTime(attacker.getUniqueId(), Math.min(attackerTime + timeToTake, 300));
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!plugin.isGameRunning() || !plugin.isInGame(victim.getUniqueId())) return;
        Player attacker = victim.getKiller();

        if (attacker != null) {
            int victimTime = plugin.getTime(victim.getUniqueId());

            if (victimTime > 0) {
                int killerTime = plugin.getTime(attacker.getUniqueId());

                plugin.setTime(attacker.getUniqueId(), Math.min(killerTime + (victimTime / 2), 300));

                attacker.sendMessage(ChatColor.GOLD + "You stole " + (victimTime / 2) + "s from " + victim.getName() + "!");
                attacker.playSound(attacker.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }

        plugin.removePlayer(victim.getUniqueId());
        victim.setGameMode(org.bukkit.GameMode.SPECTATOR);
        victim.sendMessage(ChatColor.RED + "You have been killed. You may watch in spectator mode, until the host runs /reset.");
        if (attacker != null) victim.teleport(attacker.getLocation());
    }
}
