package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public ReplyCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getChatManager().translateColorCodes(
                plugin.getConfigManager().getMessage("must-be-player")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                "&c使用方法: /" + label + " <メッセージ>"));
            return true;
        }

        UUID lastMessaged = plugin.getMessageManager().getLastMessaged(player.getUniqueId());
        if (lastMessaged == null) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                "&c返信する相手がいません"));
            return true;
        }

        Player target = Bukkit.getPlayer(lastMessaged);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                "&cそのプレイヤーはオフラインです"));
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

        plugin.getMessageManager().sendPrivateMessage(player, target, messageBuilder.toString());
        return true;
    }
}
