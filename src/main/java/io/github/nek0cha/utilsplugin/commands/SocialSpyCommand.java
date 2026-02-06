package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SocialSpyCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public SocialSpyCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getChatManager().translateColorCodes(
                plugin.getConfigManager().getMessage("must-be-player")));
            return true;
        }

        if (!player.hasPermission("utilsplugin.socialspy")) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        plugin.getMessageManager().toggleSocialSpy(player.getUniqueId());

        boolean enabled = plugin.getMessageManager().hasSocialSpyEnabled(player.getUniqueId());
        String message = enabled ? "&aソーシャルスパイを有効にしました" : "&cソーシャルスパイを無効にしました";

        player.sendMessage(plugin.getChatManager().translateToComponent(message));
        return true;
    }
}
