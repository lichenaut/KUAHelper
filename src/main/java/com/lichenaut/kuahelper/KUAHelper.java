package com.lichenaut.kuahelper;

import com.lichenaut.kuahelper.listening.KHTp;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class KUAHelper extends JavaPlugin {

    private final Logger log = getLogger();

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        LuckPerms lp = LuckPermsProvider.get();

        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHEmailVerifier(this), this);
        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHFirstJoin(), this);
        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHFreebie(this), this);
        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHPvP(), this);
        pm.registerEvents(new KHTp(lp), this);

        Objects.requireNonNull(this.getCommand("map")).setExecutor(new com.lichenaut.kuahelper.command.KHMap(this));
        Objects.requireNonNull(this.getCommand("vegan")).setExecutor(new com.lichenaut.kuahelper.command.KHVeganToggle(this, lp));
        Objects.requireNonNull(this.getCommand("vegan")).setTabCompleter(new com.lichenaut.kuahelper.command.KHTabCompleter());
    }

    public Logger getLog() {return log;}
}
