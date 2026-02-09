package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionManager {

    private final Utilsplugin plugin;

    public MentionManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public String processMentions(String message, Player sender) {
        if (!plugin.getConfig().getBoolean("mention.enabled", true)) {
            return message;
        }

        String prefix = plugin.getConfig().getString("mention.prefix", "@");
        String color = plugin.getConfig().getString("mention.color", "&e");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            String playerName = player.getName();

            // @プレイヤー名の形式
            String mentionPattern = Pattern.quote(prefix + playerName);
            Pattern pattern = Pattern.compile("(?i)" + mentionPattern);
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                message = matcher.replaceAll(plugin.getChatManager().translateColorCodes(color + prefix + playerName + "&r"));
                if (!player.equals(sender)) {
                    notifyPlayer(player);
                }
            }

            // プレイヤー名のみの形式（単語境界を考慮）
            String namePattern = "\\b" + Pattern.quote(playerName) + "\\b";
            Pattern namePatternCompiled = Pattern.compile("(?i)" + namePattern);
            Matcher nameMatcher = namePatternCompiled.matcher(message);

            if (nameMatcher.find() && !message.contains(prefix + playerName)) {
                message = nameMatcher.replaceAll(plugin.getChatManager().translateColorCodes(color + playerName + "&r"));
                if (!player.equals(sender)) {
                    notifyPlayer(player);
                }
            }
        }

        return message;
    }

    private void notifyPlayer(Player mentioned) {
        if (!plugin.getConfig().getBoolean("mention.sound.enabled", true)) {
            return;
        }

        try {
            String soundType = plugin.getConfig().getString("mention.sound.type", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float volume = (float) plugin.getConfig().getDouble("mention.sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("mention.sound.pitch", 2.0);
            Sound sound;
            try {
                sound = Sound.valueOf(soundType.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("無効なサウンド名: " + soundType);
                return;
            }
            mentioned.playSound(mentioned.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無効なサウンド名: " + e.getMessage());
        }
    }
}
