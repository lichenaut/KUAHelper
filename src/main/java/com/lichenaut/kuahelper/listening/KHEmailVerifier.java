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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("deprecation")
public class KHEmailVerifier implements Listener {

    private final KUAHelper plugin;
    private final LuckPerms lp;
    private final Essentials essentials;
    private final HashSet<String> validMails;
    private final HashMap<String, InheritanceNode> uniMap;
    private final HashMap<UUID, GameMode> preVerificationPlayers;
    private final HashMap<UUID, String> verificationCodes;
    private final HashMap<UUID, HashSet<BukkitTask>> playerTasks;
    private final PotionEffect BLINDNESS;
    private final String password;

    public KHEmailVerifier(KUAHelper plugin, LuckPerms lp) {
        this.plugin = plugin;
        this.lp = lp;
        essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        validMails = new HashSet<>();
        validMails.add("ccd.edu");
        validMails.add("ch.university");
        validMails.add("colostate.edu");
        validMails.add("denvercollegeofnursing.edu");
        validMails.add("du.edu");
        validMails.add("emilygriffith.edu");
        validMails.add("msudenver.edu");
        validMails.add("pmi.edu");
        validMails.add("regis.edu");
        validMails.add("archden.org");
        validMails.add("ucdenver.edu");
        uniMap = new HashMap<>();
        uniMap.put("ccd.edu", InheritanceNode.builder("ccd").build());
        uniMap.put("ch.university", InheritanceNode.builder("chu").build());
        uniMap.put("colostate.edu", InheritanceNode.builder("csu").build());
        uniMap.put("denvercollegeofnursing.edu", InheritanceNode.builder("dcn").build());
        uniMap.put("du.edu", InheritanceNode.builder("du").build());
        uniMap.put("emilygriffith.edu", InheritanceNode.builder("eg").build());
        uniMap.put("msudenver.edu", InheritanceNode.builder("msu").build());
        uniMap.put("pmi.edu", InheritanceNode.builder("pmi").build());
        uniMap.put("regis.edu", InheritanceNode.builder("ru").build());
        uniMap.put("archden.org", InheritanceNode.builder("sjv").build());
        uniMap.put("ucdenver.edu", InheritanceNode.builder("ucd").build());
        preVerificationPlayers = new HashMap<>();
        verificationCodes = new HashMap<>();
        playerTasks = new HashMap<>();
        BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1, false, false, false);
        try {password = Files.readString(Path.of(plugin.getDataFolder() + FileSystems.getDefault().getSeparator() + "error_id.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setOp(false);
        preVerificationPlayers.put(p.getUniqueId(), p.getGameMode());
        p.addPotionEffect(BLINDNESS);
        p.setGameMode(GameMode.SPECTATOR);
        helperMessage(p);

        UUID uuid = p.getUniqueId();
        playerTasks.put(uuid, new HashSet<>());
        playerTasks.get(uuid).add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (preVerificationPlayers.containsKey(p.getUniqueId())) p.kickPlayer(ChatColor.GRAY + "You did not verify your e-mail in time. Please try again.");
        }, 12000));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        User user = lp.getUserManager().getUser(p.getName());
        if (user != null) {
            uniMap.forEach((k, v) -> user.data().remove(v));
            lp.getUserManager().saveUser(user);
        }

        UUID uuid = p.getUniqueId();
        playerTasks.get(uuid).forEach(BukkitTask::cancel);
        preVerificationPlayers.remove(uuid);
        verificationCodes.remove(uuid);
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent e) {
        Player p = e.getPlayer();
        User user = lp.getUserManager().getUser(p.getName());
        if (user != null) {
            uniMap.forEach((k, v) -> user.data().remove(v));
            lp.getUserManager().saveUser(user);
        }

        UUID uuid = p.getUniqueId();
        playerTasks.get(uuid).forEach(BukkitTask::cancel);
        preVerificationPlayers.remove(uuid);
        verificationCodes.remove(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!preVerificationPlayers.containsKey(uuid)) return;

        String msg = e.getMessage();
        if (msg.startsWith("/send ")) {
            if (verificationCodes.containsKey(uuid)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "You have already sent a verification code. Please check your e-mail.\nYou will be able to send another in five minutes.");
                return;
            }

            User user = lp.getUserManager().getUser(p.getName());
            if (user == null) {p.sendMessage(ChatColor.RED + "An error occurred while retrieving your user data. Please contact an administrator.");return;}

            String email = e.getMessage().split(" ")[1];
            int length = email.length();
            if (email.contains(" ") || length < 8 || length > 50 || !email.contains("@") || email.endsWith("@") || !validMails.contains(email.split("@")[1])) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "Invalid e-mail. Please use your school e-mail.");
            } else {
                sendEmail(p, email);
                uniMap.forEach((k, v) -> user.data().remove(v));
                user.data().add(uniMap.get(email.split("@")[1]));
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
                p.setGameMode(preVerificationPlayers.get(uuid));
                preVerificationPlayers.remove(uuid);
                verificationCodes.remove(uuid);
                String nick = essentials.getUser(p).getNickname();
                if (!p.hasPlayedBefore()) Bukkit.broadcastMessage(ChatColor.GRAY + "§lWelcome new player " + nick + ChatColor.GRAY + " §lto the server!");
                if (!p.hasPermission("essentials.silentjoin")) Bukkit.broadcastMessage(nick + ChatColor.GRAY + " [" + ChatColor.GREEN + "+" + ChatColor.GRAY + "]");
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
        if (preVerificationPlayers.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
            helperMessage(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (preVerificationPlayers.containsKey(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    private void sendEmail(Player p, String email) {
        Properties properties  = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        String serverEmail = "5kua.verifier@gmail.com";
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {return new PasswordAuthentication(serverEmail, password);}
        });

        String code = String.format("%06d", new Random().nextInt(999999));
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(serverEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Verification Code");
            message.setText(code);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            return;
        }

        UUID uuid = p.getUniqueId();
        verificationCodes.put(uuid, code);
        playerTasks.get(uuid).add(Bukkit.getScheduler().runTaskLater(plugin, () -> verificationCodes.remove(uuid), 6000));
        p.sendMessage(ChatColor.GREEN + "Sent! The code is valid for five minutes.");
    }

    private void helperMessage(Player p) {
        p.sendMessage(ChatColor.GRAY + "Welcome! Please verify your e-mail to continue.\n \n"
                + ChatColor.WHITE + "1. " + ChatColor.GRAY + "Type '" + ChatColor.WHITE + "/send <your-email>" + ChatColor.GRAY
                + "' where '" + ChatColor.WHITE + "<your-email>" + ChatColor.GRAY + "' is your school e-mail address.\n"
                + ChatColor.WHITE + "2. " + ChatColor.GRAY + "You will receive a 6-digit code to that e-mail. Type '"
                + ChatColor.WHITE + "/verify <code>" + ChatColor.GRAY + "' to verify your e-mail.\n"
                + ChatColor.WHITE + "3. " + ChatColor.GRAY + "You are now able to play!\n \n"
                + "After your e-mail is sent a code, your e-mail will be discarded from the server.\n"
                + "If you need help, please visit the 5kUA Discord server: " + ChatColor.WHITE + "https://discord.gg/mJz8sRwpwv");
    }
}
