package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PrivateMessageCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public PrivateMessageCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getChatManager().translateColorCodes(
                plugin.getConfigManager().getMessage("must-be-player")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                "&c使用方法: /" + label + " <プレイヤー> <メッセージ>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            String message = plugin.getConfigManager().getMessage("player-not-found")
                .replace("{player}", args[0]);
            player.sendMessage(plugin.getChatManager().translateToComponent(message));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                "&c自分自身にメッセージを送ることはできません"));
            return true;
        }

        // メッセージを結合
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }

        plugin.getMessageManager().sendPrivateMessage(player, target, messageBuilder.toString());
        return true;
    }
}
