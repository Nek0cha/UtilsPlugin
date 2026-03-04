package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {

    private final Utilsplugin plugin;
    private boolean chatMuted = false;

    // URLを検出する正規表現
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]+)",
        Pattern.CASE_INSENSITIVE
    );

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

    /**
     * テキストをComponentに変換し、URLを自動的にクリック可能にする
     */
    public Component translateToComponent(String message) {
        Component base = LegacyComponentSerializer.legacySection().deserialize(translateColorCodes(message));
        return makeUrlsClickable(base);
    }

    /**
     * ComponentのテキストからURLを検出してクリックイベントを付与する
     */
    private Component makeUrlsClickable(Component component) {
        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            Matcher matcher = URL_PATTERN.matcher(content);

            if (!matcher.find()) {
                // URLが含まれない場合は子要素だけ再帰処理
                List<Component> newChildren = new ArrayList<>();
                for (Component child : component.children()) {
                    newChildren.add(makeUrlsClickable(child));
                }
                return component.children(newChildren);
            }

            // URLを含むテキストを分割してComponentのリストを作る
            List<Component> parts = new ArrayList<>();
            int lastEnd = 0;
            matcher.reset();

            while (matcher.find()) {
                // URL前のテキスト
                if (matcher.start() > lastEnd) {
                    String before = content.substring(lastEnd, matcher.start());
                    parts.add(Component.text(before).style(textComponent.style()));
                }

                // URLパーツ（クリック・ホバーイベント付き）
                String url = matcher.group(1);
                parts.add(Component.text(url)
                    .style(textComponent.style())
                    .clickEvent(ClickEvent.openUrl(url))
                    .hoverEvent(HoverEvent.showText(Component.text("§aクリックして開く: §f" + url))));

                lastEnd = matcher.end();
            }

            // URL後のテキスト
            if (lastEnd < content.length()) {
                parts.add(Component.text(content.substring(lastEnd)).style(textComponent.style()));
            }

            // 子要素も再帰処理
            List<Component> newChildren = new ArrayList<>();
            for (Component child : textComponent.children()) {
                newChildren.add(makeUrlsClickable(child));
            }

            if (parts.size() == 1 && newChildren.isEmpty()) {
                return parts.get(0);
            }

            // 最初のパーツをベースに残りを子要素として結合
            Component result = parts.get(0);
            for (int i = 1; i < parts.size(); i++) {
                result = result.append(parts.get(i));
            }
            for (Component child : newChildren) {
                result = result.append(child);
            }
            return result;
        }

        // TextComponent以外は子要素だけ再帰処理
        List<Component> newChildren = new ArrayList<>();
        for (Component child : component.children()) {
            newChildren.add(makeUrlsClickable(child));
        }
        return component.children(newChildren);
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean muted) {
        this.chatMuted = muted;
    }
}

