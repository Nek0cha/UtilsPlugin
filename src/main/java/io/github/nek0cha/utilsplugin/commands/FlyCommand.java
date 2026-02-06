package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public FlyCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("utilsplugin.fly")) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getChatManager().translateToComponent(
                    plugin.getConfigManager().getMessage("must-be-player")));
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("utilsplugin.fly.others")) {
                sender.sendMessage(plugin.getChatManager().translateToComponent(
                    plugin.getConfigManager().getMessage("no-permission")));
                return true;
            }
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                String message = plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", args[0]);
                sender.sendMessage(plugin.getChatManager().translateToComponent(message));
                return true;
            }
        }

        boolean newFlyState = !target.getAllowFlight();
        target.setAllowFlight(newFlyState);
        if (newFlyState) {
            target.setFlying(true);
        }

        String status = newFlyState ? "有効" : "無効";
        if (target.equals(sender)) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                "&a飛行モードを" + status + "にしました"));
        } else {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                "&a" + target.getName() + " の飛行モードを" + status + "にしました"));
            target.sendMessage(plugin.getChatManager().translateToComponent(
                "&a飛行モードが" + status + "になりました"));
        }

        return true;
    }
}
