package com.lichenaut.kuahelper.listening;

import com.earth2me.essentials.Essentials;
import com.lichenaut.kuahelper.KUAHelper;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class KHEmailVerifier implements Listener {

    private final KUAHelper plugin;
    private final LuckPerms lp;
    private final Essentials essentials;
    private final HashMap<String, InheritanceNode> validUnis;
    private final HashSet<UUID> preVerificationPlayers;
    private final HashMap<UUID, String> verificationCodes;
    private final HashMap<UUID, HashSet<BukkitTask>> playerTasks;
    private final PotionEffect BLINDNESS;
    private final String password;
    private final String serverEmail;
    private final Message message;

    public KHEmailVerifier(KUAHelper plugin, LuckPerms lp) throws MessagingException {
        this.plugin = plugin;
        this.lp = lp;
        essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        try {validUnis = (HashMap<String, InheritanceNode>) Files.readAllLines(Path.of(plugin.getDataFolder() + File.separator + "valid_mails.txt"), StandardCharsets.UTF_8)
                    .stream()
                    .map(line -> line.split(",", 2))
                    .collect(Collectors.toMap(parts -> parts[0], parts -> InheritanceNode.builder(parts[1]).build()));
        } catch (IOException e) {throw new RuntimeException(e);}
        preVerificationPlayers = new HashSet<>();
        verificationCodes = new HashMap<>();
        playerTasks = new HashMap<>();
        BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1, false, false, false);
        try {password = Files.readString(Path.of(plugin.getDataFolder() + FileSystems.getDefault().getSeparator() + "error_id.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {throw new RuntimeException(e);}

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        serverEmail = "5kua.verifier@gmail.com";
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(serverEmail, password);
            }
        });
        message = new MimeMessage(session);
        message.setFrom(new InternetAddress(serverEmail));
        message.setSubject("Verification Code");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.getName().equals("boogle_beast")) return;

        UUID uuid = p.getUniqueId();
        if (plugin.getVerifiedCache().contains(uuid)) {
            if (!p.hasPermission("essentials.silentjoin")) Bukkit.broadcastMessage(essentials.getUser(p).getNickname() + ChatColor.GRAY + " [" + ChatColor.GREEN + "+" + ChatColor.GRAY + "]");
            return;
        }

        p.setOp(false);
        preVerificationPlayers.add(p.getUniqueId());
        p.addPotionEffect(BLINDNESS);
        p.setGameMode(GameMode.SPECTATOR);
        helperMessage(p);

        playerTasks.put(uuid, new HashSet<>());
        playerTasks.get(uuid).add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (preVerificationPlayers.contains(p.getUniqueId())) p.kickPlayer(ChatColor.GRAY + "You did not verify your e-mail in time. Please try again.");}, 12000));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!plugin.getVerifiedCache().contains(uuid)) {
            User user = lp.getUserManager().getUser(p.getName());
            if (user != null) {
                validUnis.forEach((k, v) -> user.data().remove(v));
                lp.getUserManager().saveUser(user);
            }
            plugin.getMailCache().remove(uuid);
        }

        if (playerTasks.containsKey(uuid)) playerTasks.get(uuid).forEach(BukkitTask::cancel);
        preVerificationPlayers.remove(uuid);
        verificationCodes.remove(uuid);
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!plugin.getVerifiedCache().contains(uuid)) {
            User user = lp.getUserManager().getUser(p.getName());
            if (user != null) {
                validUnis.forEach((k, v) -> user.data().remove(v));
                lp.getUserManager().saveUser(user);
            }
            plugin.getMailCache().remove(uuid);
        }

        if (playerTasks.containsKey(uuid)) playerTasks.get(uuid).forEach(BukkitTask::cancel);
        preVerificationPlayers.remove(uuid);
        verificationCodes.remove(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) throws MessagingException {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!preVerificationPlayers.contains(uuid)) return;

        String msg = e.getMessage();
        if (msg.startsWith("/send ")) {
            if (verificationCodes.containsKey(uuid)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "You have already sent a verification code. Please check your e-mail. You will be able to send another in five minutes.");
                return;
            }

            User user = lp.getUserManager().getUser(p.getName());
            if (user == null) {
                p.sendMessage(ChatColor.RED + "An error occurred while retrieving your user data. Please contact an administrator.");
                return;
            }

            String email = e.getMessage().split(" ")[1];
            int length = email.length();
            if (email.contains(" ") || length < 8 || length > 50 || !email.contains("@") || email.endsWith("@") || !validUnis.containsKey(email.split("@")[1])) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "Invalid e-mail. Please use your school e-mail.");
            } else if (plugin.getMailCache().containsValue(email)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "This e-mail is already associated with a player.");
            } else {
                sendEmail(p, email);
                validUnis.forEach((k, v) -> user.data().remove(v));
                user.data().add(validUnis.get(email.split("@")[1]));
                lp.getUserManager().saveUser(user);
            }
        } else if (msg.startsWith("/verify ")) {
            if (!verificationCodes.containsKey(uuid)) {
                e.setCancelled(true);
                helperMessage(p);
                return;
            }

            String code = msg.split(" ")[1];
            if (code.equals(verificationCodes.get(uuid))) {
                p.removePotionEffect(PotionEffectType.BLINDNESS);
                if (!p.hasPermission("essentials.silentjoin")) p.setGameMode(GameMode.SURVIVAL);
                preVerificationPlayers.remove(uuid);
                verificationCodes.remove(uuid);
                if (p.hasPermission("kuahelper.eighthours")) {
                    plugin.getVerifiedCache().add(uuid);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.getVerifiedCache().remove(uuid);
                            if (!p.isOnline()) {
                                User user = lp.getUserManager().getUser(p.getName());
                                if (user != null) {
                                    validUnis.forEach((k, v) -> user.data().remove(v));
                                    lp.getUserManager().saveUser(user);
                                }
                                plugin.getMailCache().remove(uuid);
                            }}, 576000);
                }
                String nick = p.getName();
                if (!p.hasPlayedBefore()) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "§lWelcome new player " + ChatColor.RESET + nick + ChatColor.GREEN + " §lto the server!");
                    p.sendMessage(ChatColor.GRAY + "Welcome, leave spawn's protection when you're ready!");
                }
                if (!p.hasPermission("essentials.silentjoin")) {
                    Bukkit.broadcastMessage(nick + ChatColor.GRAY + " [" + ChatColor.GREEN + "+" + ChatColor.GRAY + "]");
                    p.sendMessage(ChatColor.GRAY + "Check out the '" + ChatColor.WHITE + "/rules" + ChatColor.GRAY + "', our '" + ChatColor.WHITE + "/discord"
                            + ChatColor.GRAY + "', and the '" + ChatColor.WHITE + "/help" + ChatColor.GRAY + "' command!");
                }
            } else {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "Invalid code. Please try again.");
            }
        } else {
            e.setCancelled(true);
            helperMessage(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (preVerificationPlayers.contains(p.getUniqueId())) {
            e.setCancelled(true);
            helperMessage(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (preVerificationPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    private void sendEmail(Player p, String email) throws MessagingException {
        String code = String.format("%06d", new Random().nextInt(999999));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setText(code + "\n \nSent by Minecraft server '5kUA: Denver Collegiate Minecraft'. If you did not request this code, please ignore this e-mail.");
        Transport.send(message);

        UUID uuid = p.getUniqueId();
        plugin.getMailCache().put(uuid, email);
        verificationCodes.put(uuid, code);
        playerTasks.get(uuid).add(Bukkit.getScheduler().runTaskLater(plugin, () -> verificationCodes.remove(uuid), 6000));
        p.sendMessage(ChatColor.GREEN + "Sent! Please also check your \"Junk\" folder.");
    }

    private void helperMessage(Player p) {
        p.sendMessage(ChatColor.GRAY + "Welcome! Please verify your e-mail to continue.\n \n"
                + ChatColor.WHITE + "1. " + ChatColor.GRAY + "Type '" + ChatColor.WHITE + "/send your-email" + ChatColor.GRAY
                + "' where '" + ChatColor.WHITE + "your-email" + ChatColor.GRAY + "' is your school e-mail address.\n"
                + ChatColor.WHITE + "2. " + ChatColor.GRAY + "You will receive a 6-digit code to that e-mail. Type '"
                + ChatColor.WHITE + "/verify your-code" + ChatColor.GRAY + "' to verify your e-mail. You are now able to play!\n \n"
                + "The server will never save your e-mail to disk.\n"
                + "If you need help, please visit the 5kUA Discord server: " + ChatColor.WHITE + "https://discord.gg/mJz8sRwpwv");
    }
}
