package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import com.lichenaut.kuahelper.util.KHCommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class KHStore extends KHCommandUtil implements CommandExecutor {

    public KHStore(KUAHelper plugin) {super(plugin);}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        messageSender(sender, ChatColor.GRAY + "Store: " + ChatColor.GREEN + "https://5kua-store.tebex.io/");
        return true;
    }
}
