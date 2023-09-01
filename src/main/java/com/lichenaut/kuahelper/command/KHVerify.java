package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class KHVerify implements CommandExecutor {

    private final KUAHelper plugin;

    public KHVerify(KUAHelper plugin) {this.plugin = plugin;}

    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) sender.sendMessage(message); else plugin.getLog().info(ChatColor.stripColor(message));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {messageSender(sender, ChatColor.RED + "This command can only be run by a player.");return false;}
        return true;
    }
}