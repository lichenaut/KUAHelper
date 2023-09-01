package com.lichenaut.kuahelper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KHTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> options = new ArrayList<>();
        if (sender instanceof Player) {
            if (strings.length == 0) {
                options.add("kuareload");
                options.add("map");
                options.add("send");
                options.add("store");
                options.add("vegan");
                options.add("verify");
            } else if (strings.length == 1) {
                options.add("off");
                options.add("on");
            }
        }
        return options;
    }
}
