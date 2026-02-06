package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AutoBroadcastManager {

    private final Utilsplugin plugin;
    private int currentMessageIndex = 0;

    public AutoBroadcastManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("auto-broadcast.enabled", true)) {
            return;
        }

        int interval = plugin.getConfig().getInt("auto-broadcast.interval", 5);
        List<String> messages = plugin.getConfig().getStringList("auto-broadcast.messages");

        if (messages.isEmpty()) {
            plugin.getLogger().warning("定期メッセージが設定されていません");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().getBoolean("auto-broadcast.enabled", true)) {
                    cancel();
                    return;
                }

                List<String> currentMessages = plugin.getConfig().getStringList("auto-broadcast.messages");
                if (currentMessages.isEmpty()) {
                    return;
                }

                String prefix = plugin.getConfig().getString("auto-broadcast.prefix", "&6[自動お知らせ] &e");
                String message = currentMessages.get(currentMessageIndex);

                // プレースホルダーの置き換え
                message = plugin.getPlaceholderManager().replacePlaceholders(prefix + message, null);

                // 全プレイヤーに送信
                plugin.getServer().broadcast(plugin.getChatManager().translateToComponent(message));

                // 次のメッセージへ
                currentMessageIndex = (currentMessageIndex + 1) % currentMessages.size();
            }
        }.runTaskTimer(plugin, 20L * 60 * interval, 20L * 60 * interval);
    }
}
