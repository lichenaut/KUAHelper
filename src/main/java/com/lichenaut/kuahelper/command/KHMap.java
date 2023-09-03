package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import com.lichenaut.kuahelper.util.KHCommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class KHMap extends KHCommandUtil implements CommandExecutor {

    public KHMap(KUAHelper plugin) {super(plugin);}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        messageSender(sender, ChatColor.GRAY + "Map: " + ChatColor.WHITE + "http://5kua.us.to:8100");
        return true;
    }
}
