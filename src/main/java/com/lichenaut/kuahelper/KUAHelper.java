package com.lichenaut.kuahelper;

import com.lichenaut.kuahelper.listening.KHTp;
import com.lichenaut.kuahelper.util.KHFilter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.apache.logging.log4j.LogManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public final class KUAHelper extends JavaPlugin {

    private final Logger log = getLogger();
    private HashSet<UUID> verifiedCache;
    private HashMap<UUID, String> mailCache; //One e-mail should not be used for multiple accounts

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        LuckPerms lp = LuckPermsProvider.get();

        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHEmailVerifier(this, lp), this);
        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHFirstJoin(), this);
        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHFreebie(this), this);
        pm.registerEvents(new com.lichenaut.kuahelper.listening.KHPvP(), this);
        pm.registerEvents(new KHTp(lp), this);

        Objects.requireNonNull(this.getCommand("map")).setExecutor(new com.lichenaut.kuahelper.command.KHMap(this));
        Objects.requireNonNull(this.getCommand("kuareload")).setExecutor(new com.lichenaut.kuahelper.command.KHReload(this));
        Objects.requireNonNull(this.getCommand("rememberme")).setExecutor(new com.lichenaut.kuahelper.command.KHRememberMe(this, lp));
        Objects.requireNonNull(this.getCommand("send")).setExecutor(new com.lichenaut.kuahelper.command.KHSend(this));
        Objects.requireNonNull(this.getCommand("store")).setExecutor(new com.lichenaut.kuahelper.command.KHStore(this));
        Objects.requireNonNull(this.getCommand("vegan")).setExecutor(new com.lichenaut.kuahelper.command.KHVeganToggle(this, lp));
        updateMails();

        Objects.requireNonNull(this.getCommand("rememberme")).setTabCompleter(new com.lichenaut.kuahelper.command.KHTabCompleter());
        Objects.requireNonNull(this.getCommand("vegan")).setTabCompleter(new com.lichenaut.kuahelper.command.KHTabCompleter());

        try {Class.forName("org.apache.logging.log4j.core.filter.AbstractFilter");
            org.apache.logging.log4j.core.Logger rootLogger;
            rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
            rootLogger.addFilter(new KHFilter(this));
        } catch (ClassNotFoundException e) {throw new RuntimeException(e);}

        verifiedCache = new HashSet<>();
        mailCache = new HashMap<>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDisable() {for (Player p : getServer().getOnlinePlayers()) p.kickPlayer("Server closed");}

    public Logger getLog() {return log;}
    public void updateMails() {Objects.requireNonNull(this.getCommand("verify")).setExecutor(new com.lichenaut.kuahelper.command.KHVerify(this));}
    public HashSet<UUID> getVerifiedCache() {return verifiedCache;}
    public HashMap<UUID, String> getMailCache() {return mailCache;}
}
