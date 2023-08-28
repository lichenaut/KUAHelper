package com.lichenaut.kuahelper.command;

import com.lichenaut.kuahelper.KUAHelper;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KHVeganToggle implements CommandExecutor {

    private final KUAHelper plugin;
    private final LuckPerms lp;
    String[] VEGAN_PERMISSIONS = {"vegalts.fishing", "vegalts.archaeology", "vegalts.infested"};

    public KHVeganToggle(KUAHelper plugin, LuckPerms lp) {this.plugin = plugin;this.lp = lp;}

    @SuppressWarnings("deprecation")
    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) sender.sendMessage(message); else plugin.getLog().info(ChatColor.stripColor(message));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {messageSender(sender, ChatColor.RED + "This command can only be run by a player.");return true;}
        if (strings.length != 1) {messageSender(sender, "/" + ChatColor.GREEN + "vegan " + ChatColor.WHITE + "<" + ChatColor.GREEN + "on" + ChatColor.WHITE + "|"
                + ChatColor.GREEN + "off" + ChatColor.WHITE + ">" + ChatColor.GRAY
                + " - Turn on/off vegan fishing, archaeology, and infested block spawning.\n" + ChatColor.WHITE + "/" + ChatColor.GREEN + "va help" + ChatColor.GRAY
                + " - Links to the vegan crafting recipes and mechanics guide.");return true;}
        if (!strings[0].equals("on") && !strings[0].equals("off")) {messageSender(sender, ChatColor.GRAY + "Invalid argument. Please use either \"" + ChatColor.WHITE + "on" +
                ChatColor.GRAY + "\" or \"" + ChatColor.WHITE + "off" + ChatColor.GRAY + "\".");return true;}

        User user = lp.getUserManager().getUser(sender.getName());
        if (user == null) {messageSender(sender, ChatColor.RED + "An error occurred while retrieving your user data. Please contact an administrator.");return true;}

        Node n1 = Node.builder(VEGAN_PERMISSIONS[0]).build();
        Node n2 = Node.builder(VEGAN_PERMISSIONS[1]).build();
        Node n3 = Node.builder(VEGAN_PERMISSIONS[2]).build();
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
