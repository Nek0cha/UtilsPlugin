package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BroadcastCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public BroadcastCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("utilsplugin.broadcast")) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                "&c使用方法: /broadcast <メッセージ>"));
            return true;
        }

        // メッセージを結合
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }

        String format = plugin.getConfig().getString("broadcast.format", "&6&l[お知らせ] &e{message}");
        format = format.replace("{message}", messageBuilder.toString());

        plugin.getServer().broadcast(plugin.getChatManager().translateToComponent(format));
        return true;
    }
}
