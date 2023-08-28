package com.lichenaut.kuahelper.listening;

import com.lichenaut.kuahelper.KUAHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KHFreebie implements Listener {

    private final KUAHelper plugin;
    private Advancement advancement;

    @SuppressWarnings("deprecation")
    public KHFreebie(KUAHelper plugin) {
        this.plugin = plugin;
        NamespacedKey key = new NamespacedKey(plugin, "freebie");
        if (Bukkit.getAdvancement(key) != null) return;
        advancement =  Bukkit.getUnsafe().loadAdvancement(key, "{\n" +
                "    \"criteria\": {\n" +
                "        \"first_death\": {\n" +
                "            \"trigger\": \"minecraft:impossible\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"display\": {\n" +
                "        \"icon\": {\n" +
                "            \"item\": \"minecraft:totem_of_undying\"\n" +
                "        },\n" +
                "        \"title\": {\n" +
                "            \"text\": \"Freebie!\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "            \"text\": \"You've been granted a one-time freebie! On your first death, keep your stuff.\"\n" +
                "        },\n" +
                "        \"background\": \"minecraft:textures/gui/advancements/backgrounds/adventure.png\",\n" +
                "        \"frame\": \"challenge\",\n" +
                "        \"announce_to_chat\": true,\n" +
                "        \"show_toast\": true,\n" +
                "        \"hidden\": true\n" +
                "    }\n" +
                "}");
    }

    @EventHandler(priority = EventPriority.LOWEST)// Lowest priority to allow other things to cancel the event
    public void onFirstDeath(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() != null) return; // Freebie doesn't apply if you were killed by another player
        Player p = e.getEntity();
        AdvancementProgress progress = p.getAdvancementProgress(advancement);
        if (progress.isDone()) return;

        e.setCancelled(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Location destination = p.getBedSpawnLocation();
            if (destination == null) destination = p.getWorld().getSpawnLocation();
            p.teleport(destination);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20);
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        }, 1L);
        p.getAdvancementProgress(advancement).awardCriteria("first_death");
    }
}
