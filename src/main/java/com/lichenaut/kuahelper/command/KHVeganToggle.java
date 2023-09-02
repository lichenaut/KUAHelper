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

public class KHVeganToggle extends KHCommandUtil implements CommandExecutor {

    private final LuckPerms lp;
    private final Node n1;
    private final Node n2;
    private final Node n3;

    public KHVeganToggle(KUAHelper plugin, LuckPerms lp) {
        super(plugin);
        this.lp = lp;
        n1 = Node.builder("vegalts.fishing").build();
        n2 = Node.builder("vegalts.archaeology").build();
        n3 = Node.builder("vegalts.infested").build();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {messageSender(sender, ChatColor.RED + "This command can only be run by a player.");return false;}
        if (strings.length != 1) {messageSender(sender, "/" + ChatColor.GREEN + "vegan " + ChatColor.WHITE + "<" + ChatColor.GREEN + "on" + ChatColor.WHITE + "|"
                + ChatColor.GREEN + "off" + ChatColor.WHITE + ">" + ChatColor.GRAY
                + " - Turn on/off vegan fishing, archaeology, and infested block spawning.\n" + ChatColor.WHITE + "/" + ChatColor.GREEN + "va help" + ChatColor.GRAY
                + " - Links to the vegan crafting recipes and mechanics guide.");return false;}
        if (!strings[0].equals("on") && !strings[0].equals("off")) {messageSender(sender, ChatColor.GRAY + "Invalid argument. Please use either \"" + ChatColor.WHITE + "on" +
                ChatColor.GRAY + "\" or \"" + ChatColor.WHITE + "off" + ChatColor.GRAY + "\".");return false;}

        User user = lp.getUserManager().getUser(sender.getName());
        if (user == null) {messageSender(sender, ChatColor.RED + "An error occurred while retrieving your user data. Please contact an administrator.");return false;}

        if (strings[0].equals("on")) {
            if (user.data().add(n1).wasSuccessful() && user.data().add(n2).wasSuccessful() && user.data().add(n3).wasSuccessful()) {
                messageSender(sender, ChatColor.GREEN + "Vegan mode enabled!");
            } else messageSender(sender, ChatColor.GRAY + "Nothing changed. Vegan mode was already enabled.");
        } else {
            if (user.data().remove(n1).wasSuccessful() && user.data().remove(n2).wasSuccessful() && user.data().remove(n3).wasSuccessful()) {
                messageSender(sender, ChatColor.GREEN + "Vegan mode disabled.");
            } else messageSender(sender, ChatColor.GRAY + "Nothing changed. Vegan mode was already disabled.");
        }
        lp.getUserManager().saveUser(user);
        return true;
    }
}