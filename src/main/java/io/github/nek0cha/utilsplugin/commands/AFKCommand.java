package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AFKCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public AFKCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getChatManager().translateColorCodes(
                plugin.getConfigManager().getMessage("must-be-player")));
            return true;
        }

        if (!player.hasPermission("utilsplugin.afk")) {
            player.sendMessage(plugin.getChatManager().translateToComponent(
                plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        boolean currentlyAFK = plugin.getAFKManager().isAFK(player.getUniqueId());
        plugin.getAFKManager().setAFK(player, !currentlyAFK);

        return true;
    }
}
