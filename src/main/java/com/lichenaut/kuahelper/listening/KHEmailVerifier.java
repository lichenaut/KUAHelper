package com.lichenaut.kuahelper.listening;

import com.lichenaut.kuahelper.KUAHelper;
import net.essentialsx.api.v2.events.chat.ChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class KHEmailVerifier implements Listener {

    private final KUAHelper plugin;
    private final HashSet<String> validMails;// This is the list of valid mail types
    private final HashSet<UUID> preVerificationPlayers;// This is the list of players who have not yet verified their email
    private final HashMap<UUID, String> verificationCodes;// This contains players' IDs and their associated codes // TODO: comment this file everywhere, runnable to remove after 5 minutes
    private final PotionEffect BLINDNESS;


    public KHEmailVerifier(KUAHelper plugin) {
        this.plugin = plugin;
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
        preVerificationPlayers = new HashSet<>();
        verificationCodes = new HashMap<>();
        BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1, false, false, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {// Put the player in the pre-verification process
        Player p = e.getPlayer();
        p.setOp(false);
        preVerificationPlayers.add(p.getUniqueId());
        p.addPotionEffect(BLINDNESS); // TODO: maybe do darkness? also how long does this last. also, how can this be exploited?
        p.setGameMode(GameMode.SPECTATOR);
        // TODO: helper message
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {// Command-related listener // TODO: if email command, send email. if verify command, verify. if else, cancel.
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!preVerificationPlayers.contains(uuid)) return;

        String msg = e.getMessage(); // TODO: inefficient to assign these when don't even know if it's valid, maybe remove?
        if (msg.startsWith("/send ")) {
            String email = e.getMessage().split(" ")[1];
            int length = email.length();
            if (email.contains(" ") || length < 8 || length > 50 || !email.contains("@") || !validMails.contains(email.split("@")[1])) {// Basic checks to see if the email is valid
                e.setCancelled(true);
                // TODO: send helper message
            } else {
                sendEmail(uuid, email);
                // TODO: send sent message to player in-game
            }
        } else if (msg.startsWith("/verify ")) {
            if (!verificationCodes.containsKey(uuid)) {// If the player is in the verification process
                // TODO: send helper message
                e.setCancelled(true);
                return;
            }

            String code = msg.split(" ")[1];
            if (code.equals(verificationCodes.get(uuid))) {
                preVerificationPlayers.remove(uuid);
                verificationCodes.remove(uuid);
                p.removePotionEffect(PotionEffectType.BLINDNESS);
                p.setGameMode(GameMode.SURVIVAL);
                // TODO: broadcast join message unless they have essentials.silentjoin
            }
        } else e.setCancelled(true);// Limit the player's commands to the verification process
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(ChatEvent e) {// This cancels player chatting if they haven't verified their email
        if (preVerificationPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
        // TODO: Send helper message
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) { // This cancels player movement if they haven't verified their email
        if (preVerificationPlayers.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    private void sendEmail(UUID uuid, String email) { // Send the e-mail, add the player's ID to the verification list, and remove it after 5 minutes
        Properties properties  = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        String serverEmail = "5kua.verifier@gmail.com";
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(serverEmail, "");
            }
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

        verificationCodes.put(uuid, code);
        Bukkit.getScheduler().runTaskLater(plugin, () -> verificationCodes.remove(uuid), 6000); // TODO: also remove on player log-out, tell the player it is sent and valid for 5 minutes
    }

    //private void helperMessage() {}

    // TODO: check that other plugins will not attempt to interact with the pre-verified player
    // TODO: kick someone if they are not verified after 10 minutes
    // TODO: make explicit you are dropping the mail address after verification
    // TODO: anti-UUID spoofing
    // TODO: add a cooldown so someone can't spam the command
    // TODO: rich error handling, max mail character limit
}
