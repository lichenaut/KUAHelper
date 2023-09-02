package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import com.lichenaut.kuahelper.util.KHCommandUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class KHRememberMe extends KHCommandUtil implements CommandExecutor {

    private final LuckPerms lp;
    private final Node n;

    public KHRememberMe(KUAHelper plugin, LuckPerms lp) {
        super(plugin);
        this.lp=lp;
        n = Node.builder("kuahelper.eighthours").build();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {messageSender(sender, ChatColor.RED + "This command can only be run by a player.");return false;}
        if (strings.length != 1) {messageSender(sender, "/rememberme <on|off>"
                + ChatColor.GRAY + " - Turn on/off whether your verifications are remembered for eight hours.");return false;}
        if (!strings[0].equals("on") && !strings[0].equals("off")) {messageSender(sender, ChatColor.GRAY + "Invalid argument. Please use either \"" + ChatColor.WHITE + "on" +
                ChatColor.GRAY + "\" or \"" + ChatColor.WHITE + "off" + ChatColor.GRAY + "\".");return false;}

        User user = lp.getUserManager().getUser(sender.getName());
        if (user == null) {messageSender(sender, ChatColor.RED + "An error occurred while retrieving your user data. Please contact an administrator.");return false;}

        if (strings[0].equals("on")) {
            if (user.data().add(n).wasSuccessful()) {
                messageSender(sender, ChatColor.GREEN + "Future verifications will now be remembered for eight hours.");
            } else messageSender(sender, ChatColor.GRAY + "Nothing changed. It was already enabled.");
        } else {
            if (user.data().remove(n).wasSuccessful()) {
                plugin.getVerifiedCache().remove(user.getUniqueId());
                messageSender(sender, ChatColor.GREEN + "Future verifications will no longer be remembered for eight hours.");
            } else messageSender(sender, ChatColor.GRAY + "Nothing changed. It was already disabled.");
        }
        lp.getUserManager().saveUser(user);
        return true;
    }
}