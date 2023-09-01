package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import com.lichenaut.kuahelper.util.KHCommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class KHReload extends KHCommandUtil implements CommandExecutor {

    public KHReload(KUAHelper plugin) {super(plugin);}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!sender.hasPermission("kuahelper.reload")) {
            messageSender(sender, ChatColor.GRAY + "You do not have permission to use this command.");
            return false;
        }
        plugin.updateMails();
        return true;
    }
}
