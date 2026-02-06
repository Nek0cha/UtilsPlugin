package io.github.nek0cha.utilsplugin.managers;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Utilsplugin plugin;

    public ConfigManager(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getMessage(String path) {
        return plugin.getConfig().getString("messages." + path, "&cメッセージが見つかりません: " + path);
    }
}
