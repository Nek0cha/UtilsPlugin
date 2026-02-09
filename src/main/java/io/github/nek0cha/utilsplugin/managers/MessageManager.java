package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class MessageManager {

    private final Utilsplugin plugin;
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();
    private final Set<UUID> socialSpyEnabled = new HashSet<>();

    public MessageManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public void sendPrivateMessage(Player sender, Player receiver, String message) {
        if (!plugin.getConfig().getBoolean("private-message.enabled", true)) {
            return;
        }

        // カラーコードの処理
        if (plugin.getConfig().getBoolean("private-message.allow-color-codes", true)) {
            if (sender.hasPermission("utilsplugin.chat.color")) {
                message = plugin.getChatManager().translateColorCodes(message);
            }
        }

        // フォーマットの取得と適用
        String senderFormat = plugin.getConfig().getString("private-message.format-sender",
            "&d[あなた -> {receiver}] &f{message}");
        String receiverFormat = plugin.getConfig().getString("private-message.format-receiver",
            "&d[{sender} -> あなた] &f{message}");

        senderFormat = senderFormat.replace("{receiver}", receiver.getName())
                                   .replace("{sender}", sender.getName())
                                   .replace("{message}", message);
        receiverFormat = receiverFormat.replace("{receiver}", receiver.getName())
                                       .replace("{sender}", sender.getName())
                                       .replace("{message}", message);

        // メッセージ送信
        sender.sendMessage(plugin.getChatManager().translateToComponent(senderFormat));
        receiver.sendMessage(plugin.getChatManager().translateToComponent(receiverFormat));

        // 音の再生
        if (plugin.getConfig().getBoolean("private-message.sound.enabled", true)) {
            try {
                String soundType = plugin.getConfig().getString("private-message.sound.type", "ENTITY_EXPERIENCE_ORB_PICKUP");
                float volume = (float) plugin.getConfig().getDouble("private-message.sound.volume", 1.0);
                float pitch = (float) plugin.getConfig().getDouble("private-message.sound.pitch", 1.0);

                Sound sound;
                try {
                    sound = Sound.valueOf(soundType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("無効なサウンド名: " + soundType);
                    return;
                }
                receiver.playSound(receiver.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("無効なサウンド名: " + e.getMessage());
            }
        }

        // 最後にメッセージを送った相手を記録
        lastMessaged.put(sender.getUniqueId(), receiver.getUniqueId());
        lastMessaged.put(receiver.getUniqueId(), sender.getUniqueId());

        // ソーシャルスパイ
        notifySocialSpy(sender, receiver, message);
    }

    public UUID getLastMessaged(UUID playerUUID) {
        return lastMessaged.get(playerUUID);
    }

    public void toggleSocialSpy(UUID playerUUID) {
        if (socialSpyEnabled.contains(playerUUID)) {
            socialSpyEnabled.remove(playerUUID);
        } else {
            socialSpyEnabled.add(playerUUID);
        }
    }

    public boolean hasSocialSpyEnabled(UUID playerUUID) {
        return socialSpyEnabled.contains(playerUUID);
    }

    private void notifySocialSpy(Player sender, Player receiver, String message) {
        if (!plugin.getConfig().getBoolean("socialspy.enabled", true)) {
            return;
        }

        String format = plugin.getConfig().getString("socialspy.format",
            "&7[SPY] &d{sender} -> {receiver}: &f{message}");
        format = format.replace("{sender}", sender.getName())
                       .replace("{receiver}", receiver.getName())
                       .replace("{message}", message);

        String finalFormat = format;
        plugin.getServer().getOnlinePlayers().stream()
            .filter(player -> hasSocialSpyEnabled(player.getUniqueId()))
            .filter(player -> !player.equals(sender) && !player.equals(receiver))
            .filter(player -> player.hasPermission("utilsplugin.socialspy"))
            .forEach(player -> player.sendMessage(plugin.getChatManager().translateToComponent(finalFormat)));
    }
}
