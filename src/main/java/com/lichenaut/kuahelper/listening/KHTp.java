package com.lichenaut.kuahelper.listening;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class KHTp implements Listener {

    private final LuckPerms lp;

    public KHTp(LuckPerms lp) {this.lp = lp;}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTp(PlayerTeleportEvent e) {
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) return;

        Player p = e.getPlayer();
        if (p.hasPermission("minecraft.command.tp")) return; // Technically unfair if a mod is also a player, but it's not a big deal

        User user = lp.getUserManager().getUser(p.getName());
        if (user == null || user.getCachedData().getPermissionData(QueryOptions.defaultContextualOptions()).checkPermission("kua.tp.bypass") == Tristate.TRUE) return;
        Location from = e.getFrom(), to = e.getTo();
        if (from.getWorld().equals(to.getWorld()) && from.distance(to) < 1000) {
            e.setCancelled(true);
            p.sendMessage("§7Teleportation cancelled, as it covers a distance less than one thousand blocks.");
        }
    }
}
