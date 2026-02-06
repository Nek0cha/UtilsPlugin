package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MuteChatCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public MuteChatCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("utilsplugin.mutechat")) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        boolean currentlyMuted = plugin.getChatManager().isChatMuted();
        plugin.getChatManager().setChatMuted(!currentlyMuted);

        String messageKey = currentlyMuted ? "mutechat.unmute-message" : "mutechat.mute-message";
        String message = plugin.getConfig().getString(messageKey,
            currentlyMuted ? "&aチャットが解除されました" : "&cチャットがミュートされました");
        message = message.replace("{player}", sender.getName());

        plugin.getServer().broadcast(plugin.getChatManager().translateToComponent(message));
        return true;
    }
}
