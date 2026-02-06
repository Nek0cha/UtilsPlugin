package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TabListManager {

    private final Utilsplugin plugin;

    public TabListManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) {
            return;
        }

        int updateInterval = plugin.getConfig().getInt("tablist.update-interval", 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().getBoolean("tablist.enabled", true)) {
                    cancel();
                    return;
                }

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateTabList(player);
                }
            }
        }.runTaskTimer(plugin, 20L, updateInterval);
    }

    private void updateTabList(Player player) {
        List<String> headerLines = plugin.getConfig().getStringList("tablist.header");
        List<String> footerLines = plugin.getConfig().getStringList("tablist.footer");

        // ヘッダーの作成
        StringBuilder headerBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(headerLines.size(), 5); i++) {
            String line = headerLines.get(i);
            line = plugin.getPlaceholderManager().replacePlaceholders(line, player);
            line = plugin.getChatManager().translateColorCodes(line);
            headerBuilder.append(line).append("\n");
        }

        // フッターの作成
        StringBuilder footerBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(footerLines.size(), 5); i++) {
            String line = footerLines.get(i);
            line = plugin.getPlaceholderManager().replacePlaceholders(line, player);
            line = plugin.getChatManager().translateColorCodes(line);
            footerBuilder.append(line).append("\n");
        }

        // タブリストの更新
        player.sendPlayerListHeaderAndFooter(
            plugin.getChatManager().translateToComponent(headerBuilder.toString().trim()),
            plugin.getChatManager().translateToComponent(footerBuilder.toString().trim())
        );
    }
}
