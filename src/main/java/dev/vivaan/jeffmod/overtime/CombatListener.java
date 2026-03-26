package dev.vivaan.jeffmod.overtime;
import org.bukkit.event.Listener;

public class CombatListener implements Listener{

    private final Overtime plugin;
    public CombatListener(Overtime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker) {
            double damage = event.getDamage();
            int timeToTake = (int) (damage / 2); // 1 heart = 1 second
            
            if (timeToTake < 1) {
                return;
            }

            int victimTime = plugin.getTime(victim.getUniqueId());
            int attackerTime = plugin.getTime(attacker.getUniqueId());

            plugin.setTime(victim.getUniqueId(), victimTime - timeToTake);
            plugin.setTime(attacker.getUniqueId(), Math.min(attackerTime + timeToTake, 300));
        }
    }
}
