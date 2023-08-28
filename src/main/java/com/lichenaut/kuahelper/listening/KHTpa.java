package com.lichenaut.kuahelper.listening;

import net.ess3.api.IUser;
import net.ess3.api.events.teleport.PreTeleportEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KHTpa implements Listener {

    @EventHandler
    public void onTpa(PreTeleportEvent e) {
        IUser p1 = e.getTeleporter();
        IUser p2 = e.getTeleportee();
        if (p1 == null || p2 == null) return;
        if (!(p1 instanceof Player && p2 instanceof Player)) return;

        if (p1.getBase().getLocation().distance(p2.getBase().getLocation()) < 2000) {
            e.setCancelled(true);
            p1.sendMessage("§7You cannot teleport to " + p2.getDisplayName() + " because they are within 2000 blocks of you.");
            p2.sendMessage("§7" + p1.getDisplayName() + " tried to teleport to you, but you are within 2000 blocks of them.");
        }
    }
}
