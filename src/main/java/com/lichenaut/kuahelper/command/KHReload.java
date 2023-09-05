package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import com.lichenaut.kuahelper.util.KHCommandUtil;
import net.luckperms.api.LuckPerms;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import javax.mail.MessagingException;

@SuppressWarnings("deprecation")
public class KHReload extends KHCommandUtil implements CommandExecutor {

    private final PluginManager pm;
    private final LuckPerms lp;

    public KHReload(KUAHelper plugin, PluginManager pm, LuckPerms lp) {
        super(plugin);
        this.pm = pm;
        this.lp = lp;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!sender.hasPermission("kuahelper.reload")) {
            messageSender(sender, ChatColor.GRAY + "You do not have permission to use this command.");
            return false;
        }
        try {plugin.updateMails(pm, lp);} catch (MessagingException e) {throw new RuntimeException(e);}
        return true;
    }
}
