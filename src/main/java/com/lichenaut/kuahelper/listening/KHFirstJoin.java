package com.lichenaut.kuahelper.listening;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class KHFirstJoin implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onFirstJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPlayedBefore()) e.getPlayer().sendMessage(ChatColor.GRAY + "Welcome, leave spawn's protection when you're ready!");
    }
}
