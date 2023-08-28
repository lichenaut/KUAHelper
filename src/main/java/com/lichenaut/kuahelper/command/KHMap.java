package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class KHMap implements CommandExecutor {

    private final KUAHelper plugin;

    public KHMap(KUAHelper plugin) {this.plugin = plugin;}

    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) sender.sendMessage(message); else plugin.getLog().info(ChatColor.stripColor(message));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        messageSender(sender, ChatColor.GRAY + "Map: https://lichenaut.com/map");
        return true;
    }
}
