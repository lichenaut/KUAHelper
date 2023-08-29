package com.lichenaut.kuahelper.listening;

import net.ess3.api.IUser;
import net.ess3.api.events.teleport.PreTeleportEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KHTp implements Listener {

    @EventHandler
    public void onTp(PreTeleportEvent e) {
        IUser p1 = e.getTeleporter();
        IUser p2 = e.getTeleportee();
        Location l1 = p1.getBase().getLocation();
        Location l2 = p2.getBase().getLocation();
        if (l1.getWorld() != l2.getWorld()) return;

        if (l1.distance(l2) < 2000) {
            e.setCancelled(true);
            if (p1 instanceof Player && p2 instanceof Player) {
                p1.sendMessage("§7You cannot teleport to " + p2.getDisplayName() + " because they are within 2000 blocks of you.");
                p2.sendMessage("§7" + p1.getDisplayName() + " tried to teleport to you, but you are within 2000 blocks of them.");
            } else {
                if (p1 instanceof Player) p1.sendMessage("§7You cannot teleport, as the destination is within 2000 blocks of you.");
                if (p2 instanceof Player) p2.sendMessage("§7Teleportation cancelled, as you are within 2000 blocks.");
            }
        }
    }
}
