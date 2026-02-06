package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public ReloadCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("utilsplugin.reload")) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        plugin.getConfigManager().reloadConfig();

        // タイムゾーンの再読み込み
        plugin.getPlaceholderManager().updateTimeZone();

        String message = plugin.getConfigManager().getMessage("reload-success");
        sender.sendMessage(plugin.getChatManager().translateToComponent(message));
        return true;
    }
}
