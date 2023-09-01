package com.lichenaut.kuahelper.util;

import com.lichenaut.kuahelper.KUAHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class KHCommandUtil {

    private final KUAHelper plugin;

    public KHCommandUtil(KUAHelper plugin) {this.plugin = plugin;}

    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) sender.sendMessage(message); else plugin.getLog().info(ChatColor.stripColor(message));
    }
}
