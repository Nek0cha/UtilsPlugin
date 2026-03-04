package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class AutoBroadcastManager {

    private final Utilsplugin plugin;
    private int currentMessageIndex = 0;
    private BukkitTask broadcastTask = null;

    public AutoBroadcastManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // 既存タスクをキャンセル
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
            broadcastTask = null;
        }

        if (!plugin.getConfig().getBoolean("auto-broadcast.enabled", true)) {
            return;
        }

        int interval = plugin.getConfig().getInt("auto-broadcast.interval", 5);
        List<String> messages = plugin.getConfig().getStringList("auto-broadcast.messages");

        if (messages.isEmpty()) {
            plugin.getLogger().warning("定期メッセージが設定されていません");
            return;
        }

        broadcastTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().getBoolean("auto-broadcast.enabled", true)) {
                    cancel();
                    broadcastTask = null;
                    return;
                }

                List<String> currentMessages = plugin.getConfig().getStringList("auto-broadcast.messages");
                if (currentMessages.isEmpty()) {
                    return;
                }

                // インデックスが範囲外になっていたらリセット
                if (currentMessageIndex >= currentMessages.size()) {
                    currentMessageIndex = 0;
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

    /** リロード時に再起動するためのメソッド */
    public void restart() {
        currentMessageIndex = 0;
        start();
    }
}
