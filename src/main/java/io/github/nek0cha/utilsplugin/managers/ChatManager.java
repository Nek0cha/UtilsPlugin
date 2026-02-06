package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class ChatManager {

    private final Utilsplugin plugin;
    private boolean chatMuted = false;

    public ChatManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public String formatChatMessage(Player player, String message) {
        if (!plugin.getConfig().getBoolean("chat.enabled", true)) {
            return null;
        }

        String format = plugin.getConfig().getString("chat.format",
            "&7[&e{world}&7] &b{player}&7: &f{message}");

        // カラーコードの処理
        if (shouldAllowColorCodes(player)) {
            message = translateColorCodes(message);
        }

        // メンション機能
        message = plugin.getMentionManager().processMentions(message, player);

        // メッセージをフォーマットに埋め込む
        format = format.replace("{message}", message);

        // プレースホルダーの置き換え（フォーマット部分）
        format = plugin.getPlaceholderManager().replacePlaceholders(format, player);

        // フォーマット自体のカラーコードを変換
        format = translateColorCodes(format);

        return format;
    }

    public boolean shouldAllowColorCodes(Player player) {
        if (!plugin.getConfig().getBoolean("chat.color-codes.enabled", true)) {
            return false;
        }

        if (!plugin.getConfig().getBoolean("chat.color-codes.allow-players", true)) {
            return false;
        }

        boolean requirePermission = plugin.getConfig().getBoolean("chat.color-codes.require-permission", false);
        if (requirePermission) {
            return player.hasPermission("utilsplugin.chat.color");
        }

        return true;
    }

    public String translateColorCodes(String message) {
        if (message == null) return "";

        // HEXカラーコードの変換
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        message = sb.toString();

        return message.replace("&0", "§0")
                     .replace("&1", "§1")
                     .replace("&2", "§2")
                     .replace("&3", "§3")
                     .replace("&4", "§4")
                     .replace("&5", "§5")
                     .replace("&6", "§6")
                     .replace("&7", "§7")
                     .replace("&8", "§8")
                     .replace("&9", "§9")
                     .replace("&a", "§a")
                     .replace("&b", "§b")
                     .replace("&c", "§c")
                     .replace("&d", "§d")
                     .replace("&e", "§e")
                     .replace("&f", "§f")
                     .replace("&k", "§k")
                     .replace("&l", "§l")
                     .replace("&m", "§m")
                     .replace("&n", "§n")
                     .replace("&o", "§o")
                     .replace("&r", "§r");
    }

    public Component translateToComponent(String message) {
        return LegacyComponentSerializer.legacySection().deserialize(translateColorCodes(message));
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean muted) {
        this.chatMuted = muted;
    }
}
