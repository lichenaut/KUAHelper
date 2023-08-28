package com.lichenaut.kuahelper.listening;

import com.lichenaut.kuahelper.KUAHelper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
                "            \"text\": \"You've been granted a one-time freebie! On your first death, teleport to spawn instead.\"\n" +
                "        },\n" +
                "        \"background\": \"minecraft:textures/gui/advancements/backgrounds/adventure.png\",\n" +
                "        \"frame\": \"challenge\",\n" +
                "        \"announce_to_chat\": true,\n" +
                "        \"show_toast\": true,\n" +
                "        \"hidden\": true\n" +
                "    }\n" +
                "}");
    }

    @EventHandler
    public void onFirstDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        AdvancementProgress progress = p.getAdvancementProgress(advancement);
        if (progress.isDone()) return;

        e.setCancelled(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.teleport(p.getWorld().getSpawnLocation());
            AttributeInstance health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (health != null) p.setHealth(health.getValue());
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        }, 1L);
        p.getAdvancementProgress(advancement).awardCriteria("first_death");
    }
}
