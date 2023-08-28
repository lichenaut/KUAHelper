package com.lichenaut.kuahelper.listening;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KHHitKill implements Listener {

    @EventHandler
    public void onHitKill(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;

        e.setKeepInventory(true);
        e.setKeepLevel(true);
        e.setDroppedExp(0);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }
}
