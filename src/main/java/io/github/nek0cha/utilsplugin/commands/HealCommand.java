package io.github.nek0cha.utilsplugin.commands;

import io.github.nek0cha.utilsplugin.Utilsplugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HealCommand implements CommandExecutor {

    private final Utilsplugin plugin;

    public HealCommand(Utilsplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("utilsplugin.heal")) {
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
            if (!sender.hasPermission("utilsplugin.heal.others")) {
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

        target.setHealth(Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue());
        target.setFoodLevel(20);
        target.setSaturation(20);
        target.setFireTicks(0);

        if (target.equals(sender)) {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                "&a体力と満腹度を回復しました"));
        } else {
            sender.sendMessage(plugin.getChatManager().translateToComponent(
                "&a" + target.getName() + " の体力と満腹度を回復しました"));
            target.sendMessage(plugin.getChatManager().translateToComponent(
                "&a体力と満腹度が回復されました"));
        }

        return true;
    }
}
