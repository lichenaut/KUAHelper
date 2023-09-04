package com.lichenaut.kuahelper.listening;

import com.lichenaut.kuahelper.KUAHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public class KHFreebie implements Listener {

    private final KUAHelper plugin;
    private final NamespacedKey key;

    public KHFreebie(KUAHelper plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "freebiekey");
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onFirstDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.getKiller() != null) return; // Freebie doesn't apply if you were killed by another player
        if (p.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        e.setCancelled(true);
        p.getPersistentDataContainer().set(key, PersistentDataType.STRING, "freebie");

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Location destination = p.getBedSpawnLocation();
            if (destination == null) destination = p.getWorld().getSpawnLocation();
            p.teleport(destination);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20);
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
            plugin.getServer().broadcastMessage(p.getName() + " has made the advancement " + ChatColor.GREEN + "[Freebie!]");
            p.sendMessage(ChatColor.GREEN + "You've been granted a one-time freebie! On your first death, you keep your stuff!");
        }, 1L);
    }
}
